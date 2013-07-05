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
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;

import org.xml.sax.SAXException;
import org.xmlfield.core.api.XmlFieldNode;
import org.xmlfield.core.api.XmlFieldNodeList;
import org.xmlfield.core.api.XmlFieldNodeModifier;
import org.xmlfield.core.api.XmlFieldNodeModifierFactory;
import org.xmlfield.core.api.XmlFieldNodeParser;
import org.xmlfield.core.api.XmlFieldNodeParserFactory;
import org.xmlfield.core.api.XmlFieldObject;
import org.xmlfield.core.api.XmlFieldSelector;
import org.xmlfield.core.api.XmlFieldSelectorFactory;
import org.xmlfield.core.exception.XmlFieldException;
import org.xmlfield.core.exception.XmlFieldParsingException;
import org.xmlfield.core.exception.XmlFieldXPathException;
import org.xmlfield.core.impl.dom.DomNodeParser;
import org.xmlfield.core.internal.NamespaceMap;
import org.xmlfield.core.internal.XPathUtils;
import org.xmlfield.core.internal.XmlFieldInvocationHandler;
import org.xmlfield.core.internal.XmlFieldUtils;

/**
 * This class is the entry point of XmlField. It can convert xml data to objects
 * (both ways).
 * 
 * <pre>
 * // Source Xml
 * String xml ="&lt;modelRootTag&gt;&lt;/modelRootTag&gt;"; 
 * 
 * // Read doc
 * XmlField xf = new XmlField();
 * IModel model = xf.xmlToObject(xmlRessource, IModel.class)
 * 
 * // Back to XML.
 * xml = xf.objectToXml( model);
 * </pre>
 * 
 * <p>
 * This class and return objects are not thread safe. If the same objects are
 * used concurrently in multiple threads, be sure to synchronize or use
 * XmlFieldFactory with ThreadLocal enabled.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * @author Nicolas Richeton
 */
public class XmlField {
	/**
	 * Classloader used to load the proxies.
	 */
	private static final ClassLoader classLoader = Thread.currentThread()
			.getContextClassLoader();

	private static XmlFieldNodeModifierFactory modifierFactory = XmlFieldNodeModifierFactory
			.newInstance();
	private static XmlFieldNodeParserFactory parserFactory = XmlFieldNodeParserFactory
			.newInstance();
	private static XmlFieldSelectorFactory selectorFactory = XmlFieldSelectorFactory
			.newInstance();

	private boolean getterCache = false;
	private XmlFieldNodeModifier modifier;
	/**
	 * Parser used to parse the xml to node
	 */
	private XmlFieldNodeParser parser;

	private Map<String, String> parserConfiguration;
	/**
	 * Selector used to execute xpath expression
	 */
	private XmlFieldSelector selector;

	/**
	 * Create XmlField object for xml/object manipulations.
	 * 
	 * <p>
	 * Use default configuration.
	 */
	public XmlField() {
		this(null);
	}

	/**
	 * Create XmlField object for xml/object manipulations. This constructor can
	 * be used to gain fine control over xmlfield output for example
	 * (indentation, xml declaration etc. ).
	 * 
	 * <p>
	 * Configuration is forwarded to xml transformer.
	 * </p>
	 * <p>
	 * See {@link OutputKeys} for universal valid keys and values.
	 * </p>
	 * <p>
	 * For specific transformer implementation see available documentation, for
	 * exemple, if xalan is used see
	 * org.apache.xml.serializer.OutputPropertiesFactory
	 * </p>
	 * <p>
	 * BEWARE : if {@link OutputKeys#MEDIA_TYPE} is set to anything but xml, it
	 * can defeat XmlField purpose. this is applicable to other configuration
	 * too.
	 * </p>
	 * 
	 * @param parserConfiguration
	 *            parser configuration. Allowed keys are specific to the parser
	 *            implementations. See {@link DomNodeParser} for default
	 *            implementation.
	 */
	public XmlField(Map<String, String> parserConfiguration) {
		this.parserConfiguration = parserConfiguration;
	}

	/**
	 * Returns the modifier associated with this XmlField object.
	 * <p>
	 * {@link XmlFieldNodeModifier} instance is created on demand at the first
	 * call.
	 * 
	 * @return
	 */
	public XmlFieldNodeModifier _getModifier() {
		if (modifier == null) {
			modifier = modifierFactory.newModifier();
		}

		return modifier;
	}

	/**
	 * Returns the parser associated with this XmlField object.
	 * <p>
	 * {@link XmlFieldNodeParser} instance is created on demand at the first
	 * call.
	 * 
	 * @return
	 */
	public XmlFieldNodeParser _getParser() {
		if (parser == null) {
			parser = parserFactory.newParser(parserConfiguration);
		}

		return parser;
	}

	/**
	 * Returns the selector associated with this XmlField object.
	 * <p>
	 * {@link XmlFieldSelector} instance is created on demand at the first call.
	 * 
	 * @return
	 */
	public XmlFieldSelector _getSelector() {
		if (selector == null) {
			selector = selectorFactory.newSelector();
		}

		return selector;
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
	public <T> T castObject(Object o, Class<T> type) {
		return loadProxy(XmlFieldUtils.getXmlFieldNode(o), type);
	}

	/**
	 * Returns the current parser configuration.
	 * <p>
	 * The returned object is cannot be updated.
	 * 
	 * @return
	 */
	public Map<String, String> getParserConfiguration() {
		return new HashMap<String, String>(parserConfiguration);
	}

	public boolean isGetterCache() {
		return getterCache;
	}

	private <T> T loadProxy(final XmlFieldNode node, final Class<T> type) {

		// Handle case when requested type is String.
		if (String.class.equals(type)) {
			return type.cast(node.getTextContent());
		}

		final Class<?>[] types = new Class<?>[] { type, XmlFieldObject.class };

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
	 *         <code>type<code> and {@link XmlFieldObject}
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XmlFieldParsingException
	 */
	public <T> T newObject(Class<T> type) throws XmlFieldParsingException {

		// Create a new xml document with an empty tag (based on the
		// annotation).
		String resourceXPath = getResourceXPath(type);
		String tag = getElementNameWithSelector(resourceXPath);
		NamespaceMap namespaces = getResourceNamespaces(type);
		String xml = XmlFieldUtils.emptyTag(tag, namespaces);

		// Create object from this document
		return xmlToObject(xml, type);
	}

	public <T> T[] nodeToArray(final String resourceXPath,
			final XmlFieldNode node, final Class<T> type)
			throws XmlFieldXPathException {

		final NamespaceMap namespaces = getResourceNamespaces(type);

		final XmlFieldNodeList xmlFieldNodes = _getSelector()
				.selectXPathToNodeList(namespaces, resourceXPath, node);

		final List<T> list = new ArrayList<T>();
		for (int i = 0; i < xmlFieldNodes.getLength(); i++) {
			final T proxy = loadProxy(xmlFieldNodes.item(i), type);

			list.add(proxy);
		}
		return toArray(list, type);
	}

	public <T> T[] nodeToArray(final XmlFieldNode node, final Class<T> type)
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
			final XmlFieldNode node,
			final Map<String, Class<?>> explicitCollection)
			throws XmlFieldXPathException {

		// we should replace the last occurrence of the last xpath name with a *
		String toReplace = XPathUtils.getElementName(resourceXPath);
		StringBuilder b = new StringBuilder(resourceXPath);
		b.replace(resourceXPath.lastIndexOf(toReplace),
				resourceXPath.lastIndexOf(toReplace) + 1, "*");
		final String resourceXPathGlobal = b.toString();

		// TODO
		// See https://sourceforge.net/apps/mantisbt/xmlfield/view.php?id=46

		final NamespaceMap namespaces = getResourceNamespaces(null);

		final XmlFieldNodeList xmlFieldNodes = _getSelector()
				.selectXPathToNodeList(namespaces, resourceXPathGlobal, node);

		final List<Object> list = new ArrayList<Object>();

		for (int i = 0; i < xmlFieldNodes.getLength(); i++) {
			XmlFieldNode xmlFieldNode = xmlFieldNodes.item(i);
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
			final XmlFieldNode node, final Class<T> resourceType) {

		final NamespaceMap namespaces = getResourceNamespaces(resourceType);

		final XmlFieldNode subNode;

		if (resourceXPath == null) {

			subNode = node;

		} else {

			try {

				subNode = _getSelector().selectXPathToNode(namespaces,
						resourceXPath, node);

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
	public <T> T nodeToObject(final XmlFieldNode node, final Class<T> type) {
		// Get the root tag and create an object from it.
		return nodeToObject(getResourceXPath(type), node, type);
	}

	public String nodeToXml(final XmlFieldNode node)
			throws XmlFieldParsingException {
		return _getParser().nodeToXml(node);
	}

	public void nodeToXml(final XmlFieldNode node, Writer writer)
			throws XmlFieldParsingException {
		_getParser().nodeToXml(node, writer);
	}

	public XmlFieldNode objectToNode(Object o) {
		return XmlFieldUtils.getXmlFieldNode(o);
	}

	public String objectToXml(Object o) throws XmlFieldParsingException {
		return _getParser().nodeToXml(objectToNode(o));
	}

	public void objectToXml(Object o, Writer writer)
			throws XmlFieldParsingException {
		_getParser().nodeToXml(objectToNode(o), writer);
	}

	/**
	 * Enables caching for get methods.
	 * 
	 * <p>
	 * Warning : this cache is experimental and cannot get changes mades when
	 * using different objects to access the same XML Node. The use of this
	 * cache is NOT recommended at the moment.
	 * 
	 * @param getterCache
	 */
	public void setGetterCache(boolean getterCache) {
		this.getterCache = getterCache;
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

		XmlFieldNode node = xmlToNode(xml);
		T[] resultArray = nodeToArray(
				getElementNameWithSelector(getResourceXPath(type)), node, type);
		return resultArray;

	}

	/**
	 * Load the XML document from an input stream, load it internally in a tree
	 * and return the root node.
	 * <p>
	 * As XmlField supports multiple XML parsing engines, the return object is a
	 * generic type which wraps the actual implementation of the tree.
	 * 
	 * <p>
	 * The document root is intended to be used with XmlField#nodeToObject(
	 * XmlFieldNode node, Class<T> type) to get an object instance to read/write
	 * the document.
	 * 
	 * @param xmlInputStream
	 *            Input stream on an XML document.
	 * @return Root node of the XML document tree.
	 * @throws XmlFieldParsingException
	 *             When document is invalid, and cannot be parsed or when an
	 *             exception occurs.
	 */
	public XmlFieldNode xmlToNode(final InputStream xmlInputStream)
			throws XmlFieldParsingException {
		return _getParser().xmlToNode(xmlInputStream);
	}

	/**
	 * Load the XML document from a string, load it internally in a tree and
	 * return the root node.
	 * <p>
	 * As XmlField supports multiple XML parsing engines, the return object is a
	 * generic type which wraps the actual implementation of the tree.
	 * 
	 * <p>
	 * The document root is intended to be used with XmlField#nodeToObject(
	 * XmlFieldNode node, Class<T> type) to get an object instance to read/write
	 * the document.
	 * 
	 * @param xml
	 *            An XML document in a single string
	 * @return Root node of the XML document tree.
	 * @throws XmlFieldParsingException
	 *             When document is invalid and cannot be parsed.
	 */
	public XmlFieldNode xmlToNode(final String xml)
			throws XmlFieldParsingException {
		return _getParser().xmlToNode(xml);
	}

	/**
	 * @param xmlContent
	 * @param type
	 * @return
	 * @throws XmlFieldParsingException
	 */
	public <T> T xmlToObject(InputStream xmlContent, Class<T> type)
			throws XmlFieldParsingException {
		return nodeToObject(xmlToNode(xmlContent), type);
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
		return nodeToObject(xmlToNode(xml), type);
	}
}
