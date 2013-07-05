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
package org.xmlfield.core.internal;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Node;
import org.xmlfield.annotations.Association;
import org.xmlfield.annotations.ExplicitCollection;
import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.Namespaces;
import org.xmlfield.annotations.ResourceXPath;
import org.xmlfield.core.XmlField;
import org.xmlfield.core.api.XmlFieldNode;
import org.xmlfield.core.api.XmlFieldNodeModifier;
import org.xmlfield.core.api.XmlFieldObject;

/**
 * Xml manipulation node utility class.
 * 
 * @author David Andrianavalontsalama
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 */
public abstract class XmlFieldUtils {

	/**
	 * All the method prefixes known by xmlfield.
	 */
	private static final String[] METHOD_PREFIXES = { "set", "get", "has",
			"is", "addTo", "sizeOf", "isNull", "new", "removeFrom" };

	/**
	 * 
	 * @param namespaces
	 * @param contextNode
	 * @param elementName
	 * @param stringValue
	 * @return
	 */
	public static XmlFieldNode createComplexElement(NamespaceMap namespaces,
			XmlFieldNode contextNode, String elementName, String stringValue,
			XmlField xf) {

		XmlFieldNode result = contextNode;

		XmlFieldNodeModifier modifier = xf._getModifier();

		// Create required node
		switch (XPathUtils.getElementType(elementName)) {
		case XPathUtils.TYPE_ATTRIBUTE:
			modifier.createAttribute(contextNode, elementName.substring(1),
					stringValue);
			break;
		case XPathUtils.TYPE_TAG:

			result = modifier.createElement(namespaces, contextNode,
					elementName, stringValue);
			break;

		case XPathUtils.TYPE_TAG_WITH_ATTRIBUTE:
			// Create tag
			String tagName = XPathUtils.getElementName(elementName);
			result = modifier.createElement(namespaces, contextNode, tagName,
					stringValue);

			// Then create attributes in selector
			Map<String, String> attributes = XPathUtils
					.getElementSelectorAttributes(elementName);
			for (String key : attributes.keySet()) {
				modifier.createAttribute(result, key, attributes.get(key));
			}

			break;
		}

		return result;

	}

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
	 * Used to get an element xpath expression from an attribute expression
	 * 
	 * @param fieldXPath
	 *            field xpath
	 * @return element xpath expression
	 */
	public static String getElementXPath(String fieldXPath, Class<?> type) {
		String elementXPath = XPathUtils.getElementXPath(fieldXPath);
		if (elementXPath == null) {
			elementXPath = XPathUtils.getElementXPath(getResourceXPath(type)
					+ "/" + fieldXPath);
		}
		return elementXPath;
	}

	/**
	 * Function to get all the associations of explicit collection
	 * 
	 * @param method
	 *            method to get the Collection
	 * @return map with an association of xpath name and classes
	 */
	public static Map<String, Class<?>> getExplicitCollections(
			final Method method) {

		Map<String, Class<?>> explicitAssociations = new HashMap<String, Class<?>>();

		if (method == null) {
			return null;
		}

		final ExplicitCollection explicitCollection = method
				.getAnnotation(ExplicitCollection.class);

		if (explicitCollection != null) {

			for (int j = 0; j < explicitCollection.value().length; j++) {
				// looking for ExplicitCollectionAssociation in a
				// ExplicitCollection
				if (explicitCollection.value()[j] != null) {
					Association explicitCollectionAssociation = explicitCollection
							.value()[j];
					explicitAssociations.put(
							explicitCollectionAssociation.xpath(),
							explicitCollectionAssociation.targetClass());
				}

			}

			return explicitAssociations;
		}

		final String methodName = method.getName();

		if (methodName.startsWith("get")) {
			return explicitAssociations;
		}

		for (final String prefix : METHOD_PREFIXES) {

			if (methodName.startsWith(prefix)) {

				final String methodSuffix = methodName.substring(prefix
						.length());

				final Method getterMethod;

				try {

					getterMethod = method.getDeclaringClass().getMethod(
							"get" + methodSuffix);

				} catch (final NoSuchMethodException e) {

					return null;
				}

				return getExplicitCollections(getterMethod);
			}
		}

		return explicitAssociations;
	}

	/**
	 * récupère le contenu de l'annotation @{@link FieldFormat} sur une méthode,
	 * ou <tt>null</tt>. Si la méthode passée en paramètre est un
	 * <em>setter</em>, cette méthode regarde aussi du côté du <em>getter</em>
	 * correspondant.
	 * 
	 * @param method
	 *            la méthode pour laquelle on veut le contenu de l'annotation.
	 * @return la valeur de l'annotation, ou <tt>null</tt>.
	 */
	public static String getFieldFormat(final Method method) {

		final FieldXPath fieldXPath = getFieldXPathAnnotation(method);

		if (fieldXPath == null) {

			return null;
		}

		final String format = fieldXPath.format();

		if (isBlank(format)) {

			return null;
		}

		return format;
	}

	/**
	 * récupère le contenu de l'annotation @{@link FieldXPath} sur une méthode,
	 * ou <tt>null</tt>. Si la méthode passée en paramètre est un
	 * <em>setter</em>, cette méthode regarde aussi du côté du <em>getter</em>
	 * correspondant.
	 * 
	 * @param method
	 *            la méthode pour laquelle on veut le contenu de l'annotation.
	 * @return la valeur de l'annotation, ou <tt>null</tt>.
	 */
	public static String getFieldXPath(final Method method) {

		final FieldXPath fieldXPath = getFieldXPathAnnotation(method);

		if (fieldXPath == null) {

			return null;
		}

		return fieldXPath.value();
	}

	private static FieldXPath getFieldXPathAnnotation(final Method method) {

		if (method == null) {
			return null;
		}

		final FieldXPath fieldXPath = method.getAnnotation(FieldXPath.class);

		if (fieldXPath != null) {

			return fieldXPath;
		}

		final String methodName = method.getName();

		if (methodName.startsWith("get")) {
			return null;
		}

		for (final String prefix : METHOD_PREFIXES) {

			if (methodName.startsWith(prefix)) {

				final String methodSuffix = methodName.substring(prefix
						.length());

				final Method getterMethod;

				try {

					getterMethod = method.getDeclaringClass().getMethod(
							"get" + methodSuffix);

				} catch (final NoSuchMethodException e) {

					return null;
				}

				return getFieldXPathAnnotation(getterMethod);
			}
		}

		return null;
	}

	/**
	 * récupère le contenu de l'annotation @{@link FieldXPathType} sur une
	 * méthode, ou <tt>null</tt>.
	 * 
	 * @param method
	 *            la méthode pour laquelle on veut le contenu de l'annotation.
	 * @return la valeur de l'annotation, ou <tt>null</tt>.
	 */
	public static Class<?> getFieldXPathType(final Method method) {

		final FieldXPath fieldXPath = getFieldXPathAnnotation(method);

		if (fieldXPath == null) {

			return null;
		}

		final Class<?> xpathType = fieldXPath.xpathType();

		if (xpathType == null || void.class.equals(xpathType)) {

			return null;
		}

		return xpathType;
	}

	/**
	 * récupère l'annotation @{@link Namespaces} sur une class, ou <tt>null</tt>
	 * .
	 * 
	 * @param type
	 *            la classe pour laquelle on veut le contenu de l'annotation.
	 * @return l'annotation, ou <tt>null</tt>.
	 */
	public static NamespaceMap getResourceNamespaces(final Class<?> type) {

		if (type == null) {
			return null;
		}

		final Namespaces namespaces = type.getAnnotation(Namespaces.class);

		final Class<?>[] interfaces = type.getInterfaces();

		if (interfaces == null || interfaces.length == 0) {

			return namespaces == null ? null : new NamespaceMap(namespaces);
		}

		final NamespaceMap nMap = new NamespaceMap(namespaces);

		for (final Class<?> c : interfaces) {

			final NamespaceMap ns = getResourceNamespaces(c);

			nMap.addNamespaces(ns);
		}

		return nMap.isEmpty() ? null : nMap;
	}

	/**
	 * récupère le contenu de l'annotation @{@link ResourceXPath} sur une class,
	 * ou <tt>null</tt>.
	 * 
	 * @param type
	 *            la classe pour laquelle on veut le contenu de l'annotation.
	 * @return la valeur de l'annotation, ou <tt>null</tt>.
	 */
	public static String getResourceXPath(final Class<?> type) {

		if (type == null) {
			return null;
		}

		final ResourceXPath resourceXPathDeclaration = type
				.getAnnotation(ResourceXPath.class);

		if (resourceXPathDeclaration == null) {

			return null;
		}

		return resourceXPathDeclaration.value();
	}

	/**
	 * récupérer le nœud XML qui correspond à un objet.
	 * 
	 * @param object
	 *            l'objet.
	 * @return le nœud XML qui correspond à l'objet.
	 */
	public static XmlFieldNode getXmlFieldNode(final Object object) {
		if (object instanceof XmlFieldObject) {
			return ((XmlFieldObject) object).toNode();
		}
		if (object instanceof XmlFieldNode) {
			return (XmlFieldNode) object;
		}
		return null;
	}

	/**
	 * Remove element from document. item must have been acquired either by
	 * calling {@link XmlField#nodeToObject(Node, Class)} or any get or addTo
	 * method.
	 * 
	 * @param item
	 *            Item to remove
	 */
	public static void remove(final Object item, XmlField xf) {
		remove(getXmlFieldNode(item), xf);
	}

	/**
	 * retirer un nœud XML de son parent.
	 * 
	 * @param node
	 *            le nœud à retirer.
	 */
	public static void remove(final XmlFieldNode node, XmlField xf) {

		if (node == null) {
			return;
		}

		final XmlFieldNode parent = node.getParentNode();

		if (parent != null) {
			xf._getModifier().removeChild(parent, node);
		}
	}

}
