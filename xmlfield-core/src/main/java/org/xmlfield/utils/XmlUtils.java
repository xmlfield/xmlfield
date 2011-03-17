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
package org.xmlfield.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlfield.core.XmlFieldNode;
import org.xmlfield.core.XmlFieldNodeParser;
import org.xmlfield.core.XmlFieldNodeParserFactory;
import org.xmlfield.core.XmlFieldReader;
import org.xmlfield.core.exception.XmlFieldParsingException;
import org.xmlfield.core.impl.DefaultXmlFieldNode;
import org.xmlfield.core.impl.DefaultXmlFieldNodeParser;
import org.xmlfield.core.internal.XmlFieldUtils;
import org.xmlfield.core.internal.XmlFieldUtils.NamespaceMap;

/**
 * Classe de lecture/ecriture d'un flux XML/Node
 * 
 * @author Loic Abemonty <loic.abemonty@capgemini.com>
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 */
public class XmlUtils {

    /**
     * Create an empty tag matching the given data.
     * 
     * @param tag
     *            an xml tag, can be of the form "ns:name" or "name"
     * @param namespaces
     *            the namespaces to use or null if none
     * @return a string representing the given tag with the given namespaces
     */
    public static String emptyTag(String tag, NamespaceMap namespaces) {
        StringBuilder builder = new StringBuilder("<");
        builder.append(tag);

        if (namespaces != null) {
            for (Entry<String, String> entry : namespaces) {
                builder.append(" xmlns:");
                builder.append(entry.getKey());
                builder.append("=\"");
                builder.append(entry.getValue());
                builder.append("\"");
            }
        }
        builder.append(" />");

        return builder.toString();
    }

    /**
     * Retrouve le {@link Node} associé à un Object.
     * 
     * @throws ClassCastException
     *             si l'objet n'est pas un Object XmlField
     * @param o
     *            un objet attaché grace à {@link XmlFieldReader#attach(Node, Class)}
     * @return un {@link Node} à l'origine de l'attachement
     * @deprecated use the {@link XmlFieldUtils#getXmlFieldNode(Object)} method instead
     */
    @Deprecated
    public static Node getNode(Object o) {
        return XmlFieldUtils.getNode(o);
    }

    /**
     * Produit du XML à partir de sa représentation objet.
     * 
     * @param node
     * @return
     * @throws IOException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @deprecated use the {@link XmlUtils#xmlFieldNodeToXml(XmlFieldNode)} method instead
     */
    @Deprecated
    public static String nodeToXml(final Node node) throws IOException, TransformerException,
            TransformerFactoryConfigurationError {

        XmlFieldNodeParser<Node> parser = new DefaultXmlFieldNodeParser();

        try {
            return parser.nodeToXml(new DefaultXmlFieldNode(node));
        } catch (XmlFieldParsingException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else if (cause instanceof TransformerException) {
                throw (TransformerException) cause;
            } else if (cause instanceof TransformerFactoryConfigurationError) {
                throw (TransformerFactoryConfigurationError) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }

    }

    /**
     * Produit du XML à partir d'un objet (par exemple les interfaces d'accès du XML)
     * 
     * @param o
     * @return
     * @throws IOException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @deprecated use the {@link XmlUtils#xmlFieldNodeToXml(Object)} method instead
     */
    @Deprecated
    public static String nodeToXml(final Object o) throws IOException, TransformerException,
            TransformerFactoryConfigurationError {

        XmlFieldNodeParser<Node> parser = new DefaultXmlFieldNodeParser();

        try {
            return parser.nodeToXml(o);
        } catch (XmlFieldParsingException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else if (cause instanceof TransformerException) {
                throw (TransformerException) cause;
            } else if (cause instanceof TransformerFactoryConfigurationError) {
                throw (TransformerFactoryConfigurationError) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }

    }

    public static String xmlFieldNodeToXml(final Object o) throws XmlFieldParsingException {

        XmlFieldNodeParser<?> parser = XmlFieldNodeParserFactory.newInstance().newParser();

        return parser.nodeToXml(o);

    }

    /**
     * Produit du XML à partir de sa représentation objet.
     * 
     * @param node
     * @return
     * @throws XmlFieldParsingException
     */
    public static String xmlFieldNodeToXml(final XmlFieldNode<?> node) throws XmlFieldParsingException {

        XmlFieldNodeParser<?> parser = XmlFieldNodeParserFactory.newInstance().newParser();

        return parser.nodeToXml(node);

    }

    /**
     * Produit une représentation objet d'un flux XML.
     * 
     * @param xmlContent
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @deprecated use the {@link XmlUtils#xmlToXmlFieldNode(InputStream)} method instead
     */
    @Deprecated
    public static Node xmlToNode(final InputStream xmlContent) throws ParserConfigurationException, SAXException,
            IOException {
        XmlFieldNodeParser<Node> parser = new DefaultXmlFieldNodeParser();

        try {
            return parser.xmlToNode(xmlContent).getNode();
        } catch (XmlFieldParsingException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else if (cause instanceof SAXException) {
                throw (SAXException) cause;
            } else if (cause instanceof ParserConfigurationException) {
                throw (ParserConfigurationException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

    /**
     * Produit une représentation objet du XML.
     * 
     * @param xml
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @deprecated use the {@link XmlUtils#xmlToXmlFieldNode(String)} method instead
     */
    @Deprecated
    public static Node xmlToNode(final String xml) throws ParserConfigurationException, SAXException, IOException {
        XmlFieldNodeParser<Node> parser = new DefaultXmlFieldNodeParser();

        try {
            return parser.xmlToNode(xml).getNode();
        } catch (XmlFieldParsingException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else if (cause instanceof SAXException) {
                throw (SAXException) cause;
            } else if (cause instanceof ParserConfigurationException) {
                throw (ParserConfigurationException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

    public static XmlFieldNode<?> xmlToXmlFieldNode(final InputStream xmlContent) throws XmlFieldParsingException {
        XmlFieldNodeParser<?> parser = XmlFieldNodeParserFactory.newInstance().newParser();

        return parser.xmlToNode(xmlContent);
    }

    public static XmlFieldNode<?> xmlToXmlFieldNode(final String xml) throws XmlFieldParsingException {
        XmlFieldNodeParser<?> parser = XmlFieldNodeParserFactory.newInstance().newParser();

        return parser.xmlToNode(xml);
    }
}
