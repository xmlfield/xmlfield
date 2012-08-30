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
package org.xmlfield.core.impl.dom;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

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
import org.xmlfield.core.api.XmlFieldNode;
import org.xmlfield.core.api.XmlFieldNodeParser;
import org.xmlfield.core.exception.XmlFieldParsingException;

/**
 * Default xml field node parser. This implementation deal with a {@link Node}
 * object
 * <p>
 * DomNodeParser is not thread safe.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * @author Nicolas Richeton
 */
public class DomNodeParser implements XmlFieldNodeParser {
	public static String CONFIG_INDENT_XML = "indent";

	DocumentBuilder documentBuilder = null;
	boolean indent = false;
	Transformer t = null;

	public DomNodeParser() throws TransformerConfigurationException,
			TransformerFactoryConfigurationError {
		this(null);
	}

	public DomNodeParser(Map<String, String> configuration)
			throws TransformerConfigurationException,
			TransformerFactoryConfigurationError {

		if (configuration != null
				&& Boolean.parseBoolean(configuration.get(CONFIG_INDENT_XML))) {

			indent = true;

		}
	}

	private void ensureBuilder() throws ParserConfigurationException {

		if (documentBuilder == null) {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();

			documentBuilderFactory.setNamespaceAware(true);
			documentBuilder = documentBuilderFactory.newDocumentBuilder();

		}
	}

	private void ensureTransformer() throws TransformerConfigurationException,
			TransformerFactoryConfigurationError {
		if (t == null) {
			t = TransformerFactory.newInstance().newTransformer();

			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			if (indent) {
				t.setOutputProperty(OutputKeys.INDENT, "yes");
			}

		}
	}

	@Override
	public String nodeToXml(XmlFieldNode node) throws XmlFieldParsingException {
		StringWriter sw;
		try {

			sw = new StringWriter();
			ensureTransformer();
			t.transform(new DOMSource((Node) node.getNode()), new StreamResult(
					sw));
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
	public void nodeToXml(XmlFieldNode node, Writer writer)
			throws XmlFieldParsingException {
		try {
			ensureTransformer();

			t.transform(new DOMSource((Node) node.getNode()), new StreamResult(
					writer));
		} catch (TransformerConfigurationException e) {
			throw new XmlFieldParsingException(e);
		} catch (IllegalArgumentException e) {
			throw new XmlFieldParsingException(e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new XmlFieldParsingException(e);
		} catch (TransformerException e) {
			throw new XmlFieldParsingException(e);
		}

	}

	/**
	 * Loads xml content from the input source and create XML DOM object.
	 * 
	 * @param xmlInputSource
	 * @return
	 * @throws XmlFieldParsingException
	 */
	private Node xmlToNode(final InputSource xmlInputSource)
			throws XmlFieldParsingException {

		Document document = null;
		try {
			ensureBuilder();
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

	@Override
	public XmlFieldNode xmlToNode(InputStream xmlContent)
			throws XmlFieldParsingException {
		return new DomNode(xmlToNode(new InputSource(xmlContent)));
	}

	@Override
	public XmlFieldNode xmlToNode(String xml) throws XmlFieldParsingException {
		return new DomNode(xmlToNode(new InputSource(new StringReader(xml))));
	}

}
