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
package org.xmlfield.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlfield.core.XmlFieldNode;
import org.xmlfield.core.XmlFieldNodeParser;
import org.xmlfield.core.exception.XmlFieldParsingException;
import org.xmlfield.core.internal.XmlFieldUtils;

/**
 * Default xml field node parser. This implementation deal with a {@link Node} object
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * 
 */
public class XmlFieldDomNodeParser implements XmlFieldNodeParser<Node> {

    @Override
    public String nodeToXml(Object object) throws XmlFieldParsingException {
        @SuppressWarnings("unchecked")
        XmlFieldNode<Node> node = (XmlFieldNode<Node>) XmlFieldUtils.getXmlFieldNode(object);
        return nodeToXml(node);
    }

    @Override
    public String nodeToXml(XmlFieldNode<Node> node) throws XmlFieldParsingException {
        StringWriter sw;
        try {
            final Transformer t = TransformerFactory.newInstance().newTransformer();

            sw = new StringWriter();

            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            // t.setOutputProperty(OutputKeys.INDENT, "yes");

            t.transform(new DOMSource(node.getNode()), new StreamResult(sw));
        } catch (TransformerConfigurationException e) {
            throw new XmlFieldParsingException(e);
        } catch (IllegalArgumentException e) {
            throw new XmlFieldParsingException(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new XmlFieldParsingException(e);
        } catch (TransformerException e) {
            throw new XmlFieldParsingException(e);
        }

        return sw.toString();
    }

    @Override
    public XmlFieldNode<Node> xmlToNode(InputStream xmlContent) throws XmlFieldParsingException {
        return new XmlFieldDomNode(xmlToNode(new InputSource(xmlContent)));
    }

    @Override
    public XmlFieldNode<Node> xmlToNode(String xml) throws XmlFieldParsingException {
        return new XmlFieldDomNode(xmlToNode(new InputSource(new StringReader(xml))));
    }

    /**
     * Loads xml content from the input source and create XML DOM object.
     * 
     * @param xmlInputSource
     * @return
     * @throws XmlFieldParsingException
     */
    private Node xmlToNode(final InputSource xmlInputSource) throws XmlFieldParsingException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder;
        Document document = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(xmlInputSource);
        } catch (ParserConfigurationException e) {
            throw new XmlFieldParsingException(e);
        } catch (SAXException e) {
            throw new XmlFieldParsingException(e);
        } catch (IOException e) {
            throw new XmlFieldParsingException(e);
        }

        return document.getDocumentElement();
    }

}
