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

import static com.google.common.collect.Iterables.toArray;
import static org.xmlfield.core.internal.XmlFieldUtils.getResourceNamespaces;
import static org.xmlfield.core.internal.XmlFieldUtils.getResourceXPath;
import static org.xmlfield.utils.JaxpUtils.getXPath;
import static org.xmlfield.utils.XPathUtils.getElementNameWithSelector;
import static org.xmlfield.utils.XmlUtils.xmlToNode;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.core.internal.INodeable;
import org.xmlfield.core.internal.XmlFieldInvocationHandler;
import org.xmlfield.core.internal.XmlFieldUtils;
import org.xmlfield.core.internal.XmlFieldUtils.NamespaceMap;
import org.xmlfield.utils.JaxpUtils;
import org.xmlfield.utils.XPathUtils;
import org.xmlfield.utils.XmlUtils;

/**
 * utilitaire qui attache des objets Java à des noeuds DOM, annotés avec
 * 
 * @{@link ResourceXPath} et @{@link FieldXPath}.
 * 
 * @author David Andrianavalontsalama
 * @author Mabrouk Belhout
 */
public class XmlFieldReader {

	private static final ClassLoader classLoader = Thread.currentThread()
			.getContextClassLoader();

	public <T> T attach(final Node node, final Class<T> type) {

		final String resourceXPath = getResourceXPath(type);

		return attach(resourceXPath, node, type);
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
	 */
	public <T> T attach(final String resourceXPath, final Node node,
			final Class<T> type) {

		final NamespaceMap namespaces = getResourceNamespaces(type);

		final XPath xpath = getXPath(namespaces);

		final Node subNode;

		if (resourceXPath == null) {

			subNode = node;

		} else {

			try {

				subNode = (Node) xpath.evaluate(resourceXPath, node,
						XPathConstants.NODE);

			} catch (final XPathExpressionException e) {

				throw new RuntimeException(e);
			}
		}

		if (subNode == null) {
			return null;
		} else {
			return loadProxy(subNode, type);
		}
	}

	public <T> T[] attachArray(final Node node, final Class<T> type)
			throws XPathExpressionException {

		final String resourceXPath = getResourceXPath(type);

		return attachArray(resourceXPath, node, type);
	}

	public <T> T[] attachArray(final Node node, final Class<T> type,
			final String xpathSelector) throws XPathExpressionException {

		final String resourceXPath = getResourceXPath(type);

		return attachArray(resourceXPath, node, type, xpathSelector);
	}

	public <T> T[] attachArray(final String resourceXPath, final Node node,
			final Class<T> type) throws XPathExpressionException {

		final NamespaceMap namespaces = getResourceNamespaces(type);

		final XPath xpath = getXPath(namespaces);

		final NodeList nodeList = (NodeList) xpath.evaluate(resourceXPath,
				node, XPathConstants.NODESET);

		final int nodeCount = nodeList.getLength();

		final List<T> list = new ArrayList<T>();

		for (int i = 0; i < nodeCount; ++i) {

			final Node subNode = nodeList.item(i);

			final T proxy = loadProxy(subNode, type);

			list.add(proxy);
		}

		return toArray(list, type);
	}

	public <T> T[] attachArray(final String resourceXPath, final Node node,
			final Class<T> type, final String xpathSelector)
			throws XPathExpressionException {

		final NamespaceMap namespaces = getResourceNamespaces(type);

		final XPath xpath = getXPath(namespaces);

		final NodeList nodeList = (NodeList) xpath.evaluate(resourceXPath
				+ xpathSelector, node, XPathConstants.NODESET);

		final int nodeCount = nodeList.getLength();

		final List<T> list = new ArrayList<T>();

		for (int i = 0; i < nodeCount; ++i) {

			final Node subNode = nodeList.item(i);

			final T proxy = loadProxy(subNode, type);

			list.add(proxy);
		}

		return toArray(list, type);
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
	 */
	public Object[] attachExplicitArray(final String resourceXPath,
			final Node node, final HashMap<String, Class<?>> explicitCollection)
			throws XPathExpressionException {

		// we should replace the last occurance of the last xpath name with a *
		String toReplace = XPathUtils.getElementName(resourceXPath);
		StringBuilder b = new StringBuilder(resourceXPath);
		b.replace(resourceXPath.lastIndexOf(toReplace),
				resourceXPath.lastIndexOf(toReplace) + 1, "*");
		final String resourceXPathGlobal = b.toString();
		// TODO
		final NamespaceMap namespaces = getResourceNamespaces(null);

		final XPath xpath = getXPath(namespaces);

		final NodeList nodeList = (NodeList) xpath.evaluate(
				resourceXPathGlobal, node, XPathConstants.NODESET);

		final int nodeCount = nodeList.getLength();

		final List<Object> list = new ArrayList<Object>();

		for (int i = 0; i < nodeCount; ++i) {

			final Node subNode = nodeList.item(i);

			if (explicitCollection.containsKey(subNode.getNodeName())) {
				final Object proxy = loadProxy(subNode,
						explicitCollection.get(subNode.getNodeName()));
				list.add(proxy);
			}

		}

		return toArray(list, Object.class);
	}

	/**
	 * Create an interface for the given xml and matching the given interface.
	 * 
	 * @param xml
	 *            the xml String to load
	 * @param type
	 *            the expected interface
	 * @return a proxy object responding to given type, return null if type
	 *         does'n match the xml string.
	 * @throws ParserConfigurationException
	 *             when parsing xml failed
	 * @throws SAXException
	 *             when parsing xml failed
	 * @throws IOException
	 *             when parsing xml failed
	 */
	public <T> T attachReadOnly(String xml, Class<T> type)
			throws ParserConfigurationException, //
			SAXException, IOException {
		Node node = xmlToNode(xml);
		return attach(node, type);
	}

	/**
	 * Changes interface of an already attached node.
	 * 
	 * @param o
	 *            an object obtained by any {@link #attach} call.
	 * @param type
	 *            the new interface for dom manipulation.
	 * @return an objet implementing the given interface.
	 */
	public <T> T reattach(Object o, Class<T> type) {
		return loadProxy(XmlUtils.getNode(o), type);
	}

	private <T> T loadProxy(final Node node, final Class<T> type) {

		if (String.class.equals(type)) {

			return type.cast(node.getTextContent());
		}

		final Class<?>[] types = new Class<?>[] { type, INodeable.class };

		final InvocationHandler invocationHandler = new XmlFieldInvocationHandler(
				this, node, type);

		final T proxy = type.cast(Proxy.newProxyInstance(classLoader, types,
				invocationHandler));

		return proxy;
	}

	/**
	 * instantiate a new XmlField interface.
	 * 
	 * the return object can be manipulated by Xmlfield like any object obtained
	 * by {@link #attach} methods
	 * 
	 * @param <T>
	 *            Class of interface to instantiate
	 * @param type
	 *            Class of interface to instantiate
	 * @return an object implementing Class
	 *         <code>type<code> and {@link INodeable}
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public <T> T instantiate(Class<T> type)
			throws ParserConfigurationException, SAXException, IOException {
		String resourceXPath = getResourceXPath(type);
		String tag = getElementNameWithSelector(resourceXPath);
		NamespaceMap namespaces = getResourceNamespaces(type);
		String xml = JaxpUtils.emptyTag(tag, namespaces);
		return attachReadOnly(xml, type);
	}
}
