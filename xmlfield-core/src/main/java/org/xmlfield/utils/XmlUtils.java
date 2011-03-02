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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlfield.core.XmlFieldReader;
import org.xmlfield.core.internal.XmlFieldInvocationHandler;
import org.xmlfield.core.internal.XmlFieldUtils;

/**
 * Classe de lecture/ecriture d'un flux XML/Node
 * 
 * @author Loic Abemonty <loic.abemonty@capgemini.com>
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 * 
 */
public class XmlUtils {

    /**
     * Retrouve le {@link Node} associé à un Object.
     * 
     * @throws ClassCastException
     *             si l'objet n'est pas un Object XmlField
     * @param o
     *            un objet attaché grace à
     *            {@link XmlFieldReader#attach(Node, Class)}
     * @return un {@link Node} à l'origine de l'attachement
     * 
     */
    public static Node getNode(Object o) {
        if (o instanceof java.lang.reflect.Proxy) {
            InvocationHandler handler = Proxy.getInvocationHandler(o);
            if (handler instanceof XmlFieldInvocationHandler) {
                XmlFieldInvocationHandler xmlField = (XmlFieldInvocationHandler) handler;
                return xmlField.getNode();
            }
        }
        throw new ClassCastException("object is not an XmlField interface" + String.valueOf(o));
    }

    /**
     * Produit du XML à partir de sa représentation objet.
     * 
     * @param node
     * @return
     * @throws IOException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     */
    public static String nodeToXml(final Node node) throws IOException, TransformerException,
            TransformerFactoryConfigurationError {

        final Transformer t = TransformerFactory.newInstance().newTransformer();

        final StringWriter sw = new StringWriter();

        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        // t.setOutputProperty(OutputKeys.INDENT, "yes");

        t.transform(new DOMSource(node), new StreamResult(sw));

        return sw.toString();
    }

    /**
     * Produit du XML à partir d'un objet (par exemple les interfaces d'accès du
     * XML)
     * 
     * @param o
     * @return
     * @throws IOException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     */
    public static String nodeToXml(final Object o) throws IOException, TransformerException,
            TransformerFactoryConfigurationError {

        checkNotNull(o, "o");

        final Node node = XmlFieldUtils.getNode(o);

        if (node == null) {

            throw new IllegalArgumentException("Argument should be an instance of Nodeable.");
        }

        return nodeToXml(node);
    }

    /**
     * Loads xml content from the input source and create XML DOM object.
     * 
     * @param xmlInputSource
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private static Node xmlToNode(final InputSource xmlInputSource) throws ParserConfigurationException, SAXException,
            IOException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document document = documentBuilder.parse(xmlInputSource);
        return document.getDocumentElement();
    }

    /**
     * Produit une représentation objet d'un flux XML.
     * 
     * @param xmlContent
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Node xmlToNode(final InputStream xmlContent) throws ParserConfigurationException, SAXException,
            IOException {
        return xmlToNode(new InputSource(xmlContent));
    }

    /**
     * Produit une représentation objet du XML.
     * 
     * @param xml
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Node xmlToNode(final String xml) throws ParserConfigurationException, SAXException, IOException {
        return xmlToNode(new InputSource(new StringReader(xml)));
    }

}
