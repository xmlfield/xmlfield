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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Node;
import org.xmlfield.core.api.XmlFieldNode;
import org.xmlfield.core.api.XmlFieldNodeList;
import org.xmlfield.core.api.XmlFieldSelector;
import org.xmlfield.core.exception.XmlFieldXPathException;
import org.xmlfield.core.internal.XmlFieldUtils.NamespaceMap;

import com.google.common.collect.MapMaker;

/**
 * Default xml field selector implementation. Use the jaxp implementation.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * 
 */
public class DomJaxenSelector implements XmlFieldSelector {

	public static XPath addNamespace(final NamespaceMap namespaces, XPath xp)
			throws JaxenException {
		if (namespaces != null) {
			for (Entry<String, String> entry : namespaces) {
				xp.addNamespace(entry.getKey(), entry.getValue());
			}
		}
		return xp;
	}

	/**
	 * Cache for XPath selectors. Uses soft values to let the garbage collector
	 * free unused selectors.
	 */
	private Map<String, XPath> xpathCache = new MapMaker().softValues()
			.makeMap();

	private void checkXPathNotNull(String xpath) throws XmlFieldXPathException {
		if (xpath == null) {
			throw new XmlFieldXPathException("The requested xpath is null");
		}
	}

	/**
	 * Get XPath selector, trying to reuse an item from the cache or create a
	 * new one.
	 * 
	 * @param namespaces
	 * @param xpath
	 * @return
	 * @throws JaxenException
	 */
	private XPath getXPath(NamespaceMap namespaces, String xpath)
			throws JaxenException {

		// Build cache key
		String key = StringUtils.EMPTY;
		if (namespaces != null) {
			key = key + namespaces.toString();
		}
		key = key + "|" + xpath;

		// Get from cache
		XPath result = xpathCache.get(key);

		// If not in cache, create new selector and add it to the cache.
		if (result == null) {
			result = new DOMXPath(xpath);
			addNamespace(namespaces, result);
			xpathCache.put(key, result);
		}

		return result;
	}

	@Override
	public Boolean selectXPathToBoolean(NamespaceMap namespaces, String xpath,
			XmlFieldNode node) throws XmlFieldXPathException {
		checkXPathNotNull(xpath);
		final Boolean value;
		try {
			final XPath xp = getXPath(namespaces, xpath);
			value = xp.booleanValueOf(node.getNode());
		} catch (JaxenException e) {
			throw new XmlFieldXPathException(e);
		}
		return value;
	}

	@Override
	public XmlFieldNode selectXPathToNode(NamespaceMap namespaces,
			String xpath, XmlFieldNode node) throws XmlFieldXPathException {
		checkXPathNotNull(xpath);
		final Node value;
		try {
			final XPath xp = getXPath(namespaces, xpath);
			value = (Node) xp.selectSingleNode(node.getNode());
		} catch (JaxenException e) {
			throw new XmlFieldXPathException(e);
		}
		if (value == null) {
			return null;
		}
		return new DomNode(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public XmlFieldNodeList selectXPathToNodeList(NamespaceMap namespaces,
			String xpath, XmlFieldNode node) throws XmlFieldXPathException {
		checkXPathNotNull(xpath);
		final List<Node> values;
		try {
			final XPath xp = getXPath(namespaces, xpath);
			values = xp.selectNodes(node.getNode());
		} catch (JaxenException e) {
			throw new XmlFieldXPathException(e);
		}
		final int nodeCount = values.size();

		final List<XmlFieldNode> list = new ArrayList<XmlFieldNode>();

		for (int i = 0; i < nodeCount; ++i) {

			final XmlFieldNode subNode = new DomNode(values.get(i));

			list.add(subNode);
		}
		return new DomNodeList(list);
	}

	@Override
	public Double selectXPathToNumber(NamespaceMap namespaces, String xpath,
			XmlFieldNode node) throws XmlFieldXPathException {
		checkXPathNotNull(xpath);
		final Double value;
		try {
			final XPath xp = getXPath(namespaces, xpath);
			value = xp.numberValueOf(node.getNode()).doubleValue();
		} catch (JaxenException e) {
			throw new XmlFieldXPathException(e);
		}
		return value;
	}

	@Override
	public String selectXPathToString(NamespaceMap namespaces, String xpath,
			XmlFieldNode node) throws XmlFieldXPathException {
		checkXPathNotNull(xpath);
		final String value;
		try {
			final XPath xp = getXPath(namespaces, xpath);
			value = xp.stringValueOf(node.getNode());
		} catch (JaxenException e) {
			throw new XmlFieldXPathException(e);
		}
		return value;
	}

}
