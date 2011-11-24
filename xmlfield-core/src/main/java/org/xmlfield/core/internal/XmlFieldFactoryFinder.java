/*
 * Copyright 2010 Capgemini
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */
package org.xmlfield.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlfield.core.impl.dom.DomNodeModifierFactory;
import org.xmlfield.core.impl.dom.DomNodeParserFactory;
import org.xmlfield.core.impl.dom.DomSelectorFactory;

/**
 * XmlFieldFactory finder instanciate an XmlFieldFactory instance. This finder lookup in the classpath a file named
 * xmlfield-factory.properties and use it. If more than one file is found, only the first file is loaded. If no file is
 * found or the configuration file has some errors, the finder return the default implementation.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 */
public class XmlFieldFactoryFinder {

    /**
     * Cached properties
     */
    private static Properties cachedProperties = new Properties();

    /**
     * Config file name
     */
    private static final String CONFIG_LOCATION = "xmlfield-factory.properties";

    /**
     * Map of the default factories implementations
     */
    private static Map<String, Class<?>> defaultFactoriesClass;

    private static boolean firstTime = true;

    private static Logger logger = LoggerFactory.getLogger(XmlFieldFactoryFinder.class);

    static {
        defaultFactoriesClass = new HashMap<String, Class<?>>();
        defaultFactoriesClass.put(XmlFieldSelectorFactory.class.getName(), DomSelectorFactory.class);
        defaultFactoriesClass.put(XmlFieldNodeParserFactory.class.getName(), DomNodeParserFactory.class);
        defaultFactoriesClass
                .put(XmlFieldNodeModifierFactory.class.getName(), DomNodeModifierFactory.class);
    }

    private final ClassLoader classloader;

    /**
     * Search the classpath for a configuration and instanciate a new factory.
     * 
     * @param classLoader
     *            used to search a configuration file.
     */
    public XmlFieldFactoryFinder(final ClassLoader classLoader) {
        this.classloader = classLoader;
    }

    /**
     * Instantiate a new factory instance. Search in the classpath a file named <code>xmlfield-factory.properties</code>
     * where a specific factories implementation should be declared.
     * 
     * @param <T>
     *            factory type
     * @param factoryClass
     *            lookup factory class
     * @return intance of the requested factory class
     */
    @SuppressWarnings("unchecked")
    public <T> T newFactory(Class<T> factoryClass) {
        final String requestedFactoryClass = factoryClass.getName();
        if (!defaultFactoriesClass.containsKey(requestedFactoryClass)) {
            logger.error("This requseted factory class is not managed by this finder, factory class : {}",
                    requestedFactoryClass);
            return null;
        }
        if (firstTime) {
            init();
        }
        String implementationFactoryClass = cachedProperties.getProperty(requestedFactoryClass);
        if (implementationFactoryClass == null) {
            logger.debug("The requested factory is not overriden ({}), we return the factory class : {}",
                    requestedFactoryClass, defaultFactoriesClass.get(requestedFactoryClass));
            return (T) createDefaultInstance(requestedFactoryClass);
        }
        logger.debug("The requested factory is overriden by : {}", implementationFactoryClass);
        return (T) createInstance(implementationFactoryClass);
    }

    /**
     * <p>
     * Create class using appropriate ClassLoader.
     * </p>
     * 
     * @param className
     *            Name of class to create.
     * @return Created class or <code>null</code>.
     */
    private Class<?> createClass(String className) {
        Class<?> clazz;

        // use approprite ClassLoader
        try {
            if (this.classloader != null) {
                clazz = this.classloader.loadClass(className);
            } else {
                clazz = Class.forName(className);
            }
        } catch (Throwable t) {
            logger.error("Error when creating the class named {}", className);
            return null;
        }

        return clazz;
    }

    /**
     * Create the default instance of the specified string factory class parameter.
     * 
     * @param <T>
     *            factory type
     * @param factoryClass
     *            string factory class
     * @return instance of the factory class
     */
    private Object createDefaultInstance(String factoryClass) {
        Class<?> implementationFactoryClass = defaultFactoriesClass.get(factoryClass);
        if (implementationFactoryClass == null) {
            return null;
        }
        return createInstance(implementationFactoryClass);
    }

    private Object createInstance(Class<?> clazz) {
        Object factory;
        try {
            factory = clazz.newInstance();
        } catch (ClassCastException classCastException) {
            logger.error("could not instantiate {}", clazz.getName());
            return null;
        } catch (IllegalAccessException illegalAccessException) {
            logger.error("could not instantiate {}", clazz.getName());
            return null;
        } catch (InstantiationException instantiationException) {
            logger.error("could not instantiate {}", clazz.getName());
            return null;
        }
        return factory;
    }

    private Object createInstance(String className) {
        Class<?> implementationFactoryClass = createClass(className);
        if (implementationFactoryClass == null) {
            return null;
        }
        return createInstance(implementationFactoryClass);
    }

    /**
     * XmlFieldFactory initialization
     */
    private void init() {
        synchronized (cachedProperties) {
            if (firstTime) {
                try {
                    Enumeration<URL> configFiles;
                    configFiles = classloader.getResources(CONFIG_LOCATION);

                    if (configFiles == null) {
                        logger.info("No configuration file ({}) found in the classpath.", CONFIG_LOCATION);
                        return;
                    }
                    firstTime = false;
                    boolean alreadyLoaded = false;
                    while (configFiles.hasMoreElements()) {
                        final URL url = configFiles.nextElement();
                        if (!alreadyLoaded) {
                            final InputStream is = url.openStream();
                            cachedProperties.load(is);
                            is.close();
                            logger.info("XmlFieldFactory configuration loaded from the file {}", url);
                        } else {
                            logger.info(
                                    "An other XmlFieldFactory configuration file is found in the classpath. This file won't be loaded {}",
                                    url);
                        }
                    }
                } catch (IOException e) {
                    logger.error("An error occur during the XmlFieldFActory initialization", e);
                }
            }
        }
    }
}
