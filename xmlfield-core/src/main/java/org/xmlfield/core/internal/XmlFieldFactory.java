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

/**
 * Genric xml field factory.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 */
public abstract class XmlFieldFactory {
    /**
     * <p>
     * Get a new factory instance managed by the <code>XmlFieldFactoryFinder</code>.
     * 
     * @return Instance of an xml field factory.
     * 
     * @throws RuntimeException
     *             When there is a failure in creating an <code>XmlFieldSelectorFactory</code>
     */
    protected static final <T> T newInstance(Class<T> factoryClass) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (classLoader == null) {
            // use the current class loader
            classLoader = XmlFieldFactory.class.getClassLoader();
        }

        T xmlFieldFactory = new XmlFieldFactoryFinder(classLoader).newFactory(factoryClass);

        return xmlFieldFactory;
    }
}
