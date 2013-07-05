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
package org.xmlfield.core.api;

import java.io.InputStream;
import java.io.Writer;

import org.xmlfield.core.exception.XmlFieldParsingException;

/**
 * Interface of a xml field node parser.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * @author Nicolas Richeton
 * 
 * @param <T>
 *            underlying xml node representation type
 */
public interface XmlFieldNodeParser {

	/**
	 * Transform an xml field object to an xml string.
	 * 
	 * @param object
	 *            xml field object
	 * @return xml string
	 * @throws XmlFieldParsingException
	 *             parsing exception
	 */
	// String nodeToXml(Object object) throws XmlFieldParsingException;

	/**
	 * Transform an xml field node to an xml string.
	 * 
	 * @param node
	 *            xml field node
	 * @return xml string
	 * @throws XmlFieldParsingException
	 *             parsing exception
	 */
	String nodeToXml(XmlFieldNode node) throws XmlFieldParsingException;

	void nodeToXml(XmlFieldNode node, Writer writer)
			throws XmlFieldParsingException;

	/**
	 * Transform an xml inputstream to an xml field node.
	 * 
	 * @param xmlContent
	 *            xml input stream
	 * @return xml field node
	 * @throws XmlFieldParsingException
	 *             parsing exception
	 */
	XmlFieldNode xmlToNode(InputStream xmlContent)
			throws XmlFieldParsingException;

	/**
	 * Transform an xml string to an xml field node.
	 * 
	 * @param xml
	 *            xml string
	 * @return xml field node
	 * @throws XmlFieldParsingException
	 *             parsing exception
	 */
	XmlFieldNode xmlToNode(String xml) throws XmlFieldParsingException;
}
