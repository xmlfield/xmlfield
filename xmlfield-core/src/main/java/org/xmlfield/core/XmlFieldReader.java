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
package org.xmlfield.core;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.core.exception.XmlFieldParsingException;
import org.xmlfield.core.exception.XmlFieldXPathException;
import org.xmlfield.core.impl.DefaultXmlFieldNode;
import org.xmlfield.core.internal.INodeable;

/**
 * utilitaire qui attache des objets Java à des noeuds DOM, annotés avec
 * 
 * @{@link ResourceXPath} et @{@link FieldXPath}.
 * 
 * @author David Andrianavalontsalama
 * @author Mabrouk Belhout
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * @deprecated use {@link XmlFieldBinder} class instead
 */
@Deprecated
public class XmlFieldReader {

    private final XmlFieldBinder binder = new XmlFieldBinder();

    /**
     * 
     * @param <T>
     * @param node
     * @param type
     * @return
     * @deprecated use {@link XmlFieldBinder#bind(XmlFieldNode, Class)} method instead
     */
    @Deprecated
    public <T> T attach(final Node node, final Class<T> type) {
        return binder.bind(new DefaultXmlFieldNode(node), type);
    }

    /**
     * TODO
     * 
     * @param resourceXPath
     * @param node
     *            a xml {@link Node}
     * @param type
     *            the expected interface
     * @return null for non matching xml/type.
     * @deprecated use {@link XmlFieldBinder#bind(String, XmlFieldNode, Class)} method instead
     */
    @Deprecated
    public <T> T attach(final String resourceXPath, final Node node, final Class<T> type) {
        return binder.bind(resourceXPath, new DefaultXmlFieldNode(node), type);
    }

    /**
     * 
     * @param <T>
     * @param node
     * @param type
     * @return
     * @throws XPathExpressionException
     * @deprecated use {@link XmlFieldBinder#bindToArray(XmlFieldNode, Class)} method instead
     */
    @Deprecated
    public <T> T[] attachArray(final Node node, final Class<T> type) throws XPathExpressionException {
        try {
            return binder.bindToArray(new DefaultXmlFieldNode(node), type);
        } catch (XmlFieldXPathException e) {
            Throwable cause = e.getCause();
            if (cause instanceof XPathExpressionException) {
                throw (XPathExpressionException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

    /**
     * 
     * @param <T>
     * @param node
     * @param type
     * @param xpathSelector
     * @return
     * @throws XPathExpressionException
     * @deprecated use {@link XmlFieldBinder#bindToArray(XmlFieldNode, Class, String)} method instead
     */
    @Deprecated
    public <T> T[] attachArray(final Node node, final Class<T> type, final String xpathSelector)
            throws XPathExpressionException {
        try {
            return binder.bindToArray(new DefaultXmlFieldNode(node), type, xpathSelector);
        } catch (XmlFieldXPathException e) {
            Throwable cause = e.getCause();
            if (cause instanceof XPathExpressionException) {
                throw (XPathExpressionException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

    /**
     * 
     * @param <T>
     * @param resourceXPath
     * @param node
     * @param type
     * @return
     * @throws XPathExpressionException
     * @deprecated use {@link XmlFieldBinder#bindToArray(String, XmlFieldNode, Class)} method instead
     */
    @Deprecated
    public <T> T[] attachArray(final String resourceXPath, final Node node, final Class<T> type)
            throws XPathExpressionException {
        try {
            return binder.bindToArray(resourceXPath, new DefaultXmlFieldNode(node), type);
        } catch (XmlFieldXPathException e) {
            Throwable cause = e.getCause();
            if (cause instanceof XPathExpressionException) {
                throw (XPathExpressionException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

    /**
     * 
     * @param <T>
     * @param resourceXPath
     * @param node
     * @param type
     * @param xpathSelector
     * @return
     * @throws XPathExpressionException
     * @deprecated use {@link XmlFieldBinder#bindToArray(String, XmlFieldNode, Class, String)} method instead
     */
    @Deprecated
    public <T> T[] attachArray(final String resourceXPath, final Node node, final Class<T> type,
            final String xpathSelector) throws XPathExpressionException {
        try {
            return binder.bindToArray(resourceXPath, new DefaultXmlFieldNode(node), type, xpathSelector);
        } catch (XmlFieldXPathException e) {
            Throwable cause = e.getCause();
            if (cause instanceof XPathExpressionException) {
                throw (XPathExpressionException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

    /**
     * Function to attach an array of different objects type .
     * 
     * @param resourceXPath
     *            Xpath to the collection.
     * @param explicitCollection
     *            Hashmap for matching name of xpath and clas.
     * @param node
     *            node of java object.
     * @deprecated use {@link XmlFieldBinder#bindToExplicitArray(String, XmlFieldNode, java.util.Map)} method instead
     */
    @Deprecated
    public Object[] attachExplicitArray(final String resourceXPath, final Node node,
            final HashMap<String, Class<?>> explicitCollection) throws XPathExpressionException {

        try {
            return binder.bindToExplicitArray(resourceXPath, new DefaultXmlFieldNode(node), explicitCollection);
        } catch (XmlFieldXPathException e) {
            Throwable cause = e.getCause();
            if (cause instanceof XPathExpressionException) {
                throw (XPathExpressionException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

    /**
     * Create an interface for the given xml and matching the given interface.
     * 
     * @param xml
     *            the xml String to load
     * @param type
     *            the expected interface
     * @return a proxy object responding to given type, return null if type does'n match the xml string.
     * @throws ParserConfigurationException
     *             when parsing xml failed
     * @throws SAXException
     *             when parsing xml failed
     * @throws IOException
     *             when parsing xml failed
     * @deprecated use {@link XmlFieldBinder#bindReadOnly(String, Class)} method instead
     */
    @Deprecated
    public <T> T attachReadOnly(String xml, Class<T> type) throws ParserConfigurationException, SAXException,
            IOException {
        try {
            return binder.bindReadOnly(xml, type);
        } catch (XmlFieldParsingException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ParserConfigurationException) {
                throw (ParserConfigurationException) cause;
            } else if (cause instanceof SAXException) {
                throw (SAXException) cause;
            } else if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

    /**
     * instantiate a new XmlField interface.
     * 
     * the return object can be manipulated by Xmlfield like any object obtained by {@link #attach} methods
     * 
     * @param <T>
     *            Class of interface to instantiate
     * @param type
     *            Class of interface to instantiate
     * @return an object implementing Class <code>type<code> and {@link INodeable}
     * 
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @deprecated use {@link XmlFieldBinder#instantiate(Class)} method instead
     */
    @Deprecated
    public <T> T instantiate(Class<T> type) throws ParserConfigurationException, SAXException, IOException {
        try {
            return binder.instantiate(type);
        } catch (XmlFieldParsingException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ParserConfigurationException) {
                throw (ParserConfigurationException) cause;
            } else if (cause instanceof SAXException) {
                throw (SAXException) cause;
            } else if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

    /**
     * Changes interface of an already attached node.
     * 
     * @param o
     *            an object obtained by any {@link #attach} call.
     * @param type
     *            the new interface for dom manipulation.
     * @return an objet implementing the given interface.
     * @deprecated use {@link XmlFieldBinder#rebind(Object, Class)} method instead
     */
    @Deprecated
    public <T> T reattach(Object o, Class<T> type) {
        return binder.rebind(o, type);
    }

}
