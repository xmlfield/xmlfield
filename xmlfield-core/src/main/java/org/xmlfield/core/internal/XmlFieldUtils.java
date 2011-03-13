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
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import static org.apache.commons.lang.StringUtils.substringBetween;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.NotImplementedException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlfield.annotations.ExplicitCollection;
import org.xmlfield.annotations.Association;
import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.Namespaces;
import org.xmlfield.annotations.ResourceXPath;
import org.xmlfield.core.XmlFieldReader;
import org.xmlfield.utils.JaxpUtils;
import org.xmlfield.utils.XPathUtils;

/**
 * utilitaires pour la manipulation de nœuds XML.
 * 
 * @author David Andrianavalontsalama
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
public abstract class XmlFieldUtils {

	/**
	 * ajouter un élément d'un certain type en fin de liste.
	 * 
	 * @param root
	 *            l'élément racine.
	 * @param xpath
	 *            la localisation XPath, dans cet élément, des autres éléments
	 *            de la liste à laquelle ajouter le nouvel élément.
	 * @param type
	 *            le type d'élément à ajouter.
	 * @return l'élément créé et ajouté.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	public static <T> T add(final Object root, final String xpath,
			final Class<T> type) throws XPathExpressionException, SAXException,
			ParserConfigurationException {
		return add(getNode(root), xpath, type);
	}

	/**
	 * ajouter un élément d'un certain type en fin de liste.
	 * 
	 * @param root
	 *            l'élément racine.
	 * @param xpath
	 *            la localisation XPath, dans cet élément, des autres éléments
	 *            de la liste à laquelle ajouter le nouvel élément.
	 * @param type
	 *            le type d'élément à ajouter.
	 * @return l'élément créé et ajouté.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	public static <T> T add(final Node root, final String xpath,
			final Class<T> type) throws XPathExpressionException, SAXException,
			ParserConfigurationException {

		final Node node = addNode(root, xpath, type);

		return new XmlFieldReader().attach(null, node, type);
	}

	/**
	 * ajouter un nœud qui correspond à un certain type, en fin de liste.
	 * 
	 * @param root
	 *            l'élément racine.
	 * @param fieldXPath
	 *            la localisation XPath, dans cet élément, des autres éléments
	 *            de la liste à laquelle ajouter le nouvel élément.
	 * @param type
	 *            le type d'élément à ajouter.
	 * @return le nœud créé et ajouté.
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static Node addNode(final Node root, final String fieldXPath,
			final Class<?> type) throws XPathExpressionException, SAXException,
			ParserConfigurationException {

		final NamespaceMap namespaces = getResourceNamespaces(type);
		final Node parentNode = addParentNodes(root, fieldXPath, type);

		// Create requested node.

		// final String elementName = getElementName(type);
		final String elementName = XPathUtils
				.getElementNameWithSelector(fieldXPath);
		final Node node = JaxpUtils.createComplexElement(namespaces,
				parentNode, elementName, null);

		return node;
	}

	public static Node addParentNodes(final Node root, final String fieldXPath,
			final Class<?> type) throws XPathExpressionException {

		final NamespaceMap namespaces = getResourceNamespaces(type);
		final XPath xpath = JaxpUtils.getXPath(namespaces);
		final Node parentNode;

		// Check if this type of element already exists in the document.
		final NodeList nodeList = (NodeList) xpath.evaluate(fieldXPath, root,
				XPathConstants.NODESET);

		if (nodeList != null && nodeList.getLength() > 0) {
			// Siblings exist. We will add item to their parent.
			if (nodeList.item(0).getNodeType() == Node.ATTRIBUTE_NODE) {
				String xPathElement = getElementXPath(fieldXPath);
				if (xPathElement != null) {
					parentNode = (Node) xpath.evaluate(xPathElement, root,
							XPathConstants.NODE);
				} else {
					parentNode = root;
				}
			} else {
				if (".".equals(fieldXPath)) {
					parentNode = nodeList.item(0);
				} else {
					parentNode = nodeList.item(0).getParentNode();
				}
			}

		} else {
			// Sibling do not exist. We need to create the appropriate node
			// hierarchy :

			// Do we even need a hierarchy ?
			if (!fieldXPath.contains("/")) {
				// We can create this node directly in the parent.
				parentNode = root;
			} else {
				// Get hierarchy
				final List<String> elementsToCreate = new ArrayList<String>();
				Node node;

				// Loop over the XPath hierarchy
				for (String xPathBuffer = fieldXPath;;) {
					// Remove field name. Keep only parents
					xPathBuffer = substringBeforeLast(xPathBuffer, "/");

					// Ensure xpath was valid.
					if (isBlank(xPathBuffer)) {
						throw new IllegalStateException(
								"Unable to create child in list with XPath: "
										+ fieldXPath);
					}

					// If parent node already exists, we can create the element
					// directly.
					final NodeList nList = (NodeList) xpath.evaluate(
							xPathBuffer, root, XPathConstants.NODESET);
					if (nList != null && nList.getLength() != 0) {
						node = nList.item(0);
						break; // Escape from the loop and go to node creation.
					}

					// We have not parent, we need to create a node.
					final String elementName;
					if (xPathBuffer.contains("/")) {
						// Keep only parent name
						elementName = substringAfterLast(xPathBuffer, "/");
					} else {
						// This was the last element.
						elementName = xPathBuffer;
						xPathBuffer = null;
					}

					// Remenber we have to create elementName
					elementsToCreate.add(0, elementName);

					// If that was the last parent, exit the loop.
					if (xPathBuffer == null) {
						node = root;
						break;
					}
				}

				// Create all required elements
				for (final String elementName : elementsToCreate) {
					final Node n = JaxpUtils.createComplexElement(namespaces,
							node, elementName, null);
					node = n;
				}

				// The request node will be attached to the last node created
				// (the parent).
				parentNode = node;
			}
		}

		return parentNode;
	}

	public static String getElementXPath(String fieldXPath) {
		Pattern pattern = Pattern.compile("^(.+)\\/@(.+)$");
		Matcher matcher = pattern.matcher(fieldXPath);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}

	/**
	 * Remove element from document. item must have been acquired either by
	 * calling {@link XmlFieldReader#attach(Node, Class)} or any get or addTo
	 * method.
	 * 
	 * @param item
	 *            Item to remove
	 */
	public static void remove(final Object item) {
		remove(getNode(item));
	}

	/**
	 * retirer un nœud XML de son parent.
	 * 
	 * @param node
	 *            le nœud à retirer.
	 */
	public static void remove(final Node node) {

		if (node == null) {
			return;
		}

		final Node parent = node.getParentNode();

		if (parent != null) {

			parent.removeChild(node);
		}
	}

	/**
	 * récupérer le nœud XML qui correspond à un objet.
	 * 
	 * @param object
	 *            l'objet.
	 * @return le nœud XML qui correspond à l'objet.
	 */
	public static Node getNode(final Object object) {
		if (object instanceof INodeable) {
			return ((INodeable) object).toNode();
		}
		if (object instanceof Node) {
			return (Node) object;
		}

		return null;
	}

	public static Node createFromXSD(final InputSource source) {

		throw new NotImplementedException();
	}

	private static String getElementName(final Class<?> type) {

		final String resourceXPath = getResourceXPath(type);

		if (resourceXPath == null) {
			throw new IllegalArgumentException(
					"No @ResourceXPath annotation for: " + type);
		}

		return XPathUtils.getElementNameWithSelector(resourceXPath);
	}

	/**
	 * les préfixes de méthodes reconnus.
	 */
	private static final String[] METHOD_PREFIXES = { "set", "get", "has",
			"is", "addTo", "sizeOf", "isNull", "new", "removeFrom" };

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

	/**
	 * Function to get all the associations of explicit collection
	 * 
	 * @param method
	 *            method to get the Collection
	 * @return Hashmap with an asociation of xpath name and classes
	 */
	public static HashMap<String, Class<?>> getExplicitCollections(
			final Method method) {

		HashMap<String, Class<?>> explicitAssociations = new HashMap<String, Class<?>>();

		if (method == null) {
			return null;
		}

		final Annotation[] annotations = method.getAnnotations();

		for (int i = 0; i < annotations.length; i++) {
			// looking for ExplicitCollection
			if (annotations[i] instanceof ExplicitCollection) {
				ExplicitCollection tmpExplicitCollection = (ExplicitCollection) annotations[i];
				for (int j = 0; j < tmpExplicitCollection.value().length; j++) {
					// looking for ExplicitCollectionAssociation in a
					// ExplicitCollection
					if (tmpExplicitCollection.value()[j] != null) {
						Association tmpExplicitCollectionAssociation = tmpExplicitCollection
								.value()[j];
						explicitAssociations.put(
								tmpExplicitCollectionAssociation.xpath(),
								tmpExplicitCollectionAssociation.targetClass());
					}

				}
			}
		}
		return explicitAssociations;
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

	public static class NamespaceMap implements
			Iterable<Map.Entry<String, String>> {

		private void addNamespaces(final NamespaceMap nMap) {
			if (nMap != null) {
				prefixesURIs.putAll(nMap.prefixesURIs);
			}
		}

		public NamespaceMap(final String... namespaces) {
			if (namespaces != null) {
				for (final String n : namespaces) {
					final String prefix = substringBetween(n, ":", "=");

					if (prefix == null) {

						throw new IllegalArgumentException(
								"Illegal namespace prefix for XPath expressions: "
										+ n);
					}

					final String uri = substringAfter(n, "=").replace("\"", "");
					prefixesURIs.put(prefix, uri);
				}
			}
		}

		private NamespaceMap(final Namespaces namespaces) {
			this(namespaces == null ? new String[0] : namespaces.value());
		}

		public boolean isEmpty() {
			return prefixesURIs.isEmpty();
		}

		private final Map<String, String> prefixesURIs = new HashMap<String, String>();

		public Map<String, String> getPrefixesURIs() {
			return prefixesURIs;
		}

		public String get(String prefix) {
			return prefixesURIs.get(prefix);
		}

		@Override
		public Iterator<Entry<String, String>> iterator() {
			return prefixesURIs.entrySet().iterator();
		}

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
}
