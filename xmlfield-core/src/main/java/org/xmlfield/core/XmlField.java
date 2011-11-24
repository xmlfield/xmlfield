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
import static org.xmlfield.core.internal.XPathUtils.getElementNameWithSelector;
import static org.xmlfield.core.internal.XmlFieldUtils.getResourceNamespaces;
import static org.xmlfield.core.internal.XmlFieldUtils.getResourceXPath;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xmlfield.core.exception.XmlFieldException;
import org.xmlfield.core.exception.XmlFieldParsingException;
import org.xmlfield.core.exception.XmlFieldXPathException;
import org.xmlfield.core.internal.INodeable;
import org.xmlfield.core.internal.XPathUtils;
import org.xmlfield.core.internal.XmlFieldInvocationHandler;
import org.xmlfield.core.internal.XmlFieldUtils;
import org.xmlfield.core.internal.XmlFieldUtils.NamespaceMap;

/**
 * Class which bind an interface annotated with some xpath expressions to an xml
 * field node which represent an xml content.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 */
public class XmlField {
	/**
	 * Classloader used to load the proxies.
	 */
	private static final ClassLoader classLoader = Thread.currentThread()
			.getContextClassLoader();

	/**
	 * Parser used to parse the xml to node
	 */
	private final XmlFieldNodeParser<?> parser = XmlFieldNodeParserFactory
			.newInstance().newParser();

	/**
	 * Selector used to execute xpath expression
	 */
	private final XmlFieldSelector selector = XmlFieldSelectorFactory
			.newInstance().newSelector();

	/**
	 * Changes interface of an already attached node.
	 * 
	 * @param o
	 *            an object obtained by any {@link #attach} call.
	 * @param type
	 *            the new interface for dom manipulation.
	 * @return an objet implementing the given interface.
	 */
	public <T> T castObject(Object o, Class<T> type) {
		return loadProxy(XmlFieldUtils.getXmlFieldNode(o), type);
	}

	private <T> T loadProxy(final XmlFieldNode<?> node, final Class<T> type) {

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
	 * by {@link #bind} methods
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
	 * @throws XmlFieldParsingException
	 */
	public <T> T newObject(Class<T> type) throws XmlFieldParsingException {
		String resourceXPath = getResourceXPath(type);
		String tag = getElementNameWithSelector(resourceXPath);
		NamespaceMap namespaces = getResourceNamespaces(type);
		String xml = XmlFieldUtils.emptyTag(tag, namespaces);
		return xmlToObject(xml, type);
	}

	public <T> T[] nodeToArray(final String resourceXPath,
			final XmlFieldNode<?> node, final Class<T> type)
			throws XmlFieldXPathException {

		final NamespaceMap namespaces = getResourceNamespaces(type);

		final XmlFieldNodeList xmlFieldNodes = selector.selectXPathToNodeList(
				namespaces, resourceXPath, node);

		final List<T> list = new ArrayList<T>();
		for (int i = 0; i < xmlFieldNodes.getLength(); i++) {
			final T proxy = loadProxy(xmlFieldNodes.item(i), type);

			list.add(proxy);
		}
		return toArray(list, type);
	}

	public <T> T[] nodeToArray(final XmlFieldNode<?> node, final Class<T> type)
			throws XmlFieldXPathException {

		final String resourceXPath = getResourceXPath(type);

		return nodeToArray(resourceXPath, node, type);
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
	 * @throws XmlFieldXPathException
	 */
	public Object[] nodeToExplicitArray(final String resourceXPath,
			final XmlFieldNode<?> node,
			final Map<String, Class<?>> explicitCollection)
			throws XmlFieldXPathException {

		// we should replace the last occurance of the last xpath name with a *
		String toReplace = XPathUtils.getElementName(resourceXPath);
		StringBuilder b = new StringBuilder(resourceXPath);
		b.replace(resourceXPath.lastIndexOf(toReplace),
				resourceXPath.lastIndexOf(toReplace) + 1, "*");
		final String resourceXPathGlobal = b.toString();
		// TODO
		final NamespaceMap namespaces = getResourceNamespaces(null);

		final XmlFieldNodeList xmlFieldNodes = selector.selectXPathToNodeList(
				namespaces, resourceXPathGlobal, node);

		final List<Object> list = new ArrayList<Object>();

		for (int i = 0; i < xmlFieldNodes.getLength(); i++) {
			XmlFieldNode<?> xmlFieldNode = xmlFieldNodes.item(i);
			if (explicitCollection.containsKey(xmlFieldNode.getNodeName())) {
				final Object proxy = loadProxy(xmlFieldNode,
						explicitCollection.get(xmlFieldNode.getNodeName()));
				list.add(proxy);
			}

		}

		return toArray(list, Object.class);
	}

	/**
	 * Bind a node located by the xpath expression to the specified type
	 * 
	 * @param resourceXPath
	 *            xpath expression used to locate the node to bind
	 * @param node
	 *            the root node {@link XmlFieldNode}
	 * @param resourceType
	 *            the expected interface class
	 * @return null for non matching xml/type.
	 */
	public <T> T nodeToObject(final String resourceXPath,
			final XmlFieldNode<?> node, final Class<T> resourceType) {

		final NamespaceMap namespaces = getResourceNamespaces(resourceType);

		final XmlFieldNode<?> subNode;

		if (resourceXPath == null) {

			subNode = node;

		} else {

			try {

				subNode = selector.selectXPathToNode(namespaces, resourceXPath,
						node);

			} catch (final XmlFieldXPathException e) {

				throw new RuntimeException(e);
			}
		}

		if (subNode == null || subNode.getNode() == null) {
			return null;
		} else {
			return loadProxy(subNode, resourceType);
		}
	}

	/**
	 * Bind an xml field node to the specified type. This type should have some
	 * xpath annotations.
	 * 
	 * @param <T>
	 *            interface type
	 * @param node
	 *            node
	 * @param type
	 *            interface to bind to
	 * @return instance binded to the xml
	 */
	public <T> T nodeToObject(final XmlFieldNode<?> node, final Class<T> type) {

		final String resourceXPath = getResourceXPath(type);

		return nodeToObject(resourceXPath, node, type);
	}

	public String nodeToXml(final XmlFieldNode<?> node)
			throws XmlFieldParsingException {
		return parser.nodeToXml(node);
	}

	public XmlFieldNode<?> objectToNode(Object o) {
		return XmlFieldUtils.getXmlFieldNode(o);
	}

	public String objectToXml(Object o) throws XmlFieldParsingException {
		return parser.nodeToXml(o);
	}

	/**
	 * Bind an xml string to an array of entities.
	 * 
	 * the xml should be a list of entities enclosed by a root element.
	 * 
	 * @param <T>
	 *            interface type
	 * @param xml
	 *            an xml string with a root element enclosing a list of node to
	 *            bind
	 * @param type
	 *            interface to bind to
	 * @return an array
	 * @throws XmlFieldParsingException
	 * @throws XmlFieldXPathException
	 */
	public <T> T[] xmlToArray(String xml, Class<T> type)
			throws XmlFieldException {

		XmlFieldNode<?> node = xmlToNode(xml);
		T[] resultArray = nodeToArray(
				getElementNameWithSelector(getResourceXPath(type)), node, type);
		return resultArray;

	}

	public XmlFieldNode<?> xmlToNode(final InputStream xmlContent)
			throws XmlFieldParsingException {
		return parser.xmlToNode(xmlContent);
	}

	public XmlFieldNode<?> xmlToNode(final String xml)
			throws XmlFieldParsingException {
		return parser.xmlToNode(xml);
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
	 * @throws XmlFieldParsingException
	 */
	public <T> T xmlToObject(String xml, Class<T> type)
			throws XmlFieldParsingException {
		XmlFieldNode<?> node = parser.xmlToNode(xml);
		return nodeToObject(node, type);
	}
}
