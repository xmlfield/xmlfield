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

import org.xmlfield.core.exception.XmlFieldXPathException;
import org.xmlfield.core.internal.XmlFieldUtils.NamespaceMap;

/**
 * The XPath selector interface used to select xpath expression on an xml field
 * node.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * 
 */
public interface XmlFieldSelector {

	/**
	 * Select xpath expression to an xml field node and return the result as a
	 * boolean.
	 * 
	 * @param namespaces
	 *            xml namespaces
	 * @param xpath
	 *            xpath expression
	 * @param node
	 *            xml field node
	 * @return result of the xpath evaluation
	 * @throws XmlFieldXPathException
	 *             exception thrown when the xpath evaluation failed
	 */
	Boolean selectXPathToBoolean(NamespaceMap namespaces, String xpath,
			XmlFieldNode<?> node) throws XmlFieldXPathException;

	/**
	 * Select xpath expression to an xml field node and return the result as an
	 * xml field node.
	 * 
	 * @param namespaces
	 *            xml namespaces
	 * @param xpath
	 *            xpath expression
	 * @param node
	 *            xml field node
	 * @return result of the xpath evaluation
	 * @throws XmlFieldXPathException
	 *             exception thrown when the xpath evaluation failed
	 */
	XmlFieldNode<?> selectXPathToNode(NamespaceMap namespaces, String xpath,
			XmlFieldNode<?> node) throws XmlFieldXPathException;

	/**
	 * Select xpath expression to an xml field node and return the result as an
	 * xml field node list.
	 * 
	 * @param namespaces
	 *            xml namespaces
	 * @param xpath
	 *            xpath expression
	 * @param node
	 *            xml field node
	 * @return result of the xpath evaluation
	 * @throws XmlFieldXPathException
	 *             exception thrown when the xpath evaluation failed
	 */
	XmlFieldNodeList selectXPathToNodeList(NamespaceMap namespaces,
			String xpath, XmlFieldNode<?> node) throws XmlFieldXPathException;

	/**
	 * Select xpath expression to an xml field node and return the result as a
	 * double.
	 * 
	 * @param namespaces
	 *            xml namespaces
	 * @param xpath
	 *            xpath expression
	 * @param node
	 *            xml field node
	 * @return result of the xpath evaluation
	 * @throws XmlFieldXPathException
	 *             exception thrown when the xpath evaluation failed
	 */
	Double selectXPathToNumber(NamespaceMap namespaces, String xpath,
			XmlFieldNode<?> node) throws XmlFieldXPathException;

	/**
	 * Select xpath expression to an xml field node and return the result as a
	 * string.
	 * 
	 * @param namespaces
	 *            xml namespaces
	 * @param xpath
	 *            xpath expression
	 * @param node
	 *            xml field node
	 * @return result of the xpath evaluation
	 * @throws XmlFieldXPathException
	 *             exception thrown when the xpath evaluation failed
	 */
	String selectXPathToString(NamespaceMap namespaces, String xpath,
			XmlFieldNode<?> node) throws XmlFieldXPathException;
}
