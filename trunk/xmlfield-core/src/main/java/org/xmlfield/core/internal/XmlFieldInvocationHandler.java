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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import static org.xmlfield.core.internal.XmlFieldUtils.getExplicitCollections;
import static org.xmlfield.core.internal.XmlFieldUtils.getFieldFormat;
import static org.xmlfield.core.internal.XmlFieldUtils.getFieldXPath;
import static org.xmlfield.core.internal.XmlFieldUtils.getFieldXPathType;
import static org.xmlfield.core.internal.XmlFieldUtils.getResourceNamespaces;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.core.XmlField;
import org.xmlfield.core.api.XmlFieldNode;
import org.xmlfield.core.api.XmlFieldNodeList;
import org.xmlfield.core.api.XmlFieldObject;
import org.xmlfield.core.exception.XmlFieldTechnicalException;
import org.xmlfield.core.exception.XmlFieldXPathException;

import com.google.common.collect.MapMaker;

/**
 * l'objet {@link InvocationHandler} à utiliser sur les proxies chargés à la
 * lecture des nœuds XML.
 * 
 * @author David Andrianavalontsalama
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 */
public class XmlFieldInvocationHandler implements InvocationHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(XmlFieldInvocationHandler.class);

	private static Map<String, Set<String>> methodnamesCache = new MapMaker()
			.softValues().makeMap();

	private static Map<String, NamespaceMap> namespaceCache = new MapMaker()
			.softValues().makeMap();

	/**
	 * vérifie qu'un type réel est compatible avec un type déclaré.
	 */
	private static boolean isCompatible(final Class<?> realType,
			final Class<?> declaredType) {

		if (declaredType.isAssignableFrom(realType)) {

			return true;
		}

		if (declaredType.isPrimitive()) {

			// TODO : voir si on peut faire mieux

			final Field primitiveTypeField;

			try {

				primitiveTypeField = realType.getField("TYPE");

			} catch (final NoSuchFieldException e) {

				return false;
			}

			final int modifiers = primitiveTypeField.getModifiers();

			if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers)
					|| !Modifier.isPublic(modifiers)) {

				return false;
			}

			final Object primitiveType;

			try {

				primitiveType = primitiveTypeField.get(null);

			} catch (final IllegalAccessException e) {

				return false;
			}

			return declaredType.equals(primitiveType);
		}

		return false;
	}

	/**
	 * vérifie qu'un nom de méthode est un nom de getter, en <tt>"getXxx()"</tt>
	 * , <tt>"hasXxx()"</tt> ou <tt>"isXxx()"</tt>, mais pas
	 * <tt>"isNullXxx()"</tt>.
	 */
	private static boolean isMethodNameGetter(final String methodName) {
		return methodName.startsWith("get") || methodName.startsWith("has")
				|| methodName.startsWith("is")
				&& !methodName.startsWith("isNull");
	}

	private final Map<String, Object> cache = new HashMap<String, Object>();

	private Set<String> methodNames = null;

	private NamespaceMap namespaces;

	private final XmlFieldNode node;

	private final Class<?> type;
	private final XmlField xmlField;

	/**
	 * T .
	 * 
	 * @param xmlField
	 *            l'objet reader, qui permet notamment de récupérer des
	 *            sous-champs.
	 * @param node
	 *            le nœud de l'objet Java.
	 * @param type
	 *            le type de l'objet Java.
	 * @param selector
	 *            selector object to use for that proxy.
	 * @param modifier
	 *            TODO
	 */
	public XmlFieldInvocationHandler(final XmlField xmlField,
			final XmlFieldNode node, final Class<?> type) {

		this.xmlField = checkNotNull(xmlField, "xmlField");
		this.node = checkNotNull(node, "node");
		this.type = checkNotNull(type, "type");

		String typeName = type.getName();
		// Load namespaces

		namespaces = namespaceCache.get(typeName);
		if (namespaces == null) {
			this.namespaces = getResourceNamespaces(type);
			if (namespaces != null) {
				namespaceCache.put(typeName, namespaces);
			} else {
				namespaceCache.put(typeName, new NamespaceMap());
			}
		} else if (namespaces.isEmpty()) {
			namespaces = null;
		}

		// Load method names
		methodNames = methodnamesCache.get(typeName);
		if (methodNames == null) {
			methodNames = new TreeSet<String>();
			for (final Method method : type.getMethods()) {

				final String methodName = method.getName();

				if (method.isAnnotationPresent(FieldXPath.class)
						&& isMethodNameGetter(methodName)) {

					final Class<?>[] paramTypes = method.getParameterTypes();

					if (paramTypes == null || paramTypes.length == 0) {

						methodNames.add(methodName);
					}
				}
			}
			methodnamesCache.put(typeName, methodNames);
		}
	}

	/**
	 * Add a bonded element add the end of a list located by the xpath.
	 * 
	 * @param root
	 *            root element
	 * @param xpath
	 *            xpath location to the element list
	 * @param type
	 *            element type to add
	 * @return the new element
	 * @throws XmlFieldXPathException
	 *             xpath exception
	 */
	public <T> T add(final Object root, final String xpath, final Class<T> type)
			throws XmlFieldXPathException {
		return add(XmlFieldUtils.getXmlFieldNode(root), xpath, type);
	}

	/**
	 * Add a binded instance at the end of the nodes located by the xpath.
	 * 
	 * @param root
	 *            root element
	 * @param xpath
	 *            xpath location to the element list
	 * @param type
	 *            element type to add
	 * @return the new element
	 * @throws XmlFieldXPathException
	 *             xpath exception
	 */
	public <T> T add(final XmlFieldNode root, final String xpath,
			final Class<T> type) throws XmlFieldXPathException {

		final XmlFieldNode node = addNode(root, xpath, type);

		final XmlField binder = new XmlField();

		return binder.nodeToObject(null, node, type);
	}

	/**
	 * Add the specified binded node at the end of the xpath location.
	 * 
	 * @param root
	 *            root element
	 * @param fieldXPath
	 *            xpath location relative to the root element where we want to
	 *            add the new element
	 * @param type
	 *            element type to add
	 * @return the new node
	 * @throws XmlFieldXPathException
	 *             xpath exception
	 */
	public XmlFieldNode addNode(final XmlFieldNode root,
			final String fieldXPath, final Class<?> type)
			throws XmlFieldXPathException {

		final NamespaceMap namespaces = getResourceNamespaces(type);
		final XmlFieldNode parentNode = addParentNodes(root, fieldXPath, type);

		// Create requested node.

		final String elementName = XPathUtils
				.getElementNameWithSelector(fieldXPath);
		final XmlFieldNode node = XmlFieldUtils.createComplexElement(
				namespaces, parentNode, elementName, null, xmlField);

		return node;
	}

	/**
	 * Add parent nodes to a specified node.
	 * 
	 * @param root
	 *            root element
	 * @param fieldXPath
	 * @param type
	 * @return
	 * @throws XmlFieldXPathException
	 */
	public XmlFieldNode addParentNodes(final XmlFieldNode root,
			final String fieldXPath, final Class<?> type)
			throws XmlFieldXPathException {

		final NamespaceMap namespaces = getResourceNamespaces(type);

		final XmlFieldNode parentNode;

		// Check if this type of element already exists in the document.
		final XmlFieldNodeList nodeList = xmlField._getSelector()
				.selectXPathToNodeList(namespaces, fieldXPath, root);

		if (nodeList != null && nodeList.getLength() > 0) {
			// Siblings exist. We will add item to their parent.
			if (nodeList.item(0).getNodeType() == XmlFieldNode.ATTRIBUTE_NODE) {
				String xPathElement = XPathUtils.getElementXPath(fieldXPath);
				if (xPathElement != null) {
					parentNode = xmlField._getSelector().selectXPathToNode(
							namespaces, xPathElement, root);
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
				XmlFieldNode node;

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
					final XmlFieldNodeList nList = xmlField._getSelector()
							.selectXPathToNodeList(namespaces, xPathBuffer,
									root);
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
					final XmlFieldNode n = XmlFieldUtils.createComplexElement(
							namespaces, node, elementName, null, xmlField);
					node = n;
				}

				// The request node will be attached to the last node created
				// (the parent).
				parentNode = node;
			}
		}

		return parentNode;
	}

	/**
	 * Is there a value in cache associated to the specified invoked method?
	 * 
	 * @param methodName
	 *            invoked method name.
	 * @return <code>true</code> if a value exists, <code>false</code> otherwise
	 */
	private boolean cacheExists(final String methodName) {
		return cache.containsKey(getCacheKey(methodName));
	}

	/**
	 * invoque une méthode "<tt>addToXxx()</tt>".
	 */
	private Object doAddTo(final Object proxy, final Method method,
			final Class<?> type) throws Exception {
		removeFromCache(method);

		final String fieldXPath = getFieldXPath(method);

		return add(proxy, fieldXPath, type);
	}

	/**
	 * invoque une méthode "<tt>addToXxx(Xxx.class)</tt>".
	 * 
	 * <p>
	 * La méthode récupère les champs FieldXPath et ExplicitCollection(avec les
	 * associations) de la méthode "get" associée à la méthode "addTo". Elle
	 * cherche ensuite un match entre une Association et le type d'objet à
	 * ajouter.
	 * </p>
	 * 
	 */
	private Object doAddTo(final Object proxy, final Method method,
			final Object objectType) throws Exception {
		removeFromCache(method);

		final String fieldXPath = getFieldXPath(method);

		final Class<?> objectClass = (Class<?>) objectType;

		Map<String, Class<?>> explicitCollectionAssociations = getExplicitCollections(method);

		Set<String> keysAssociations = explicitCollectionAssociations.keySet();

		String specificFieldXPath = "";

		for (String key : keysAssociations) {
			if (objectClass.isAssignableFrom(explicitCollectionAssociations
					.get(key))) {
				specificFieldXPath = fieldXPath.replace("*", key);
			}
		}

		if (StringUtils.isEmpty(specificFieldXPath)) {
			throw new XmlFieldXPathException("Aucune @Association du type "
					+ objectClass.getName() + " n'a été définie.");
		}

		return add(proxy, specificFieldXPath, objectClass);
	}

	/**
	 * invoque la méthode "<tt>equals(Object)</tt>".
	 * 
	 * @throws XmlFieldXPathException
	 */
	private Object doEquals(final Object proxy, final Object ob)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, XmlFieldXPathException {

		if (ob == null) {
			return false;
		}

		final Class<?> proxyClass = proxy.getClass();

		final Class<?> obClass = ob.getClass();

		if (!proxyClass.equals(obClass)) {
			return false;
		}

		for (final String methodName : methodNames) {

			if (!isMethodNameGetter(methodName)) {

				continue;
			}

			final Object value = getMethodValue(methodName);

			final Object obValue = obClass.getMethod(methodName).invoke(ob);

			if (value == null && obValue == null) {
				continue;
			}

			if (value == null || obValue == null) {
				return false;
			}

			if (!value.equals(obValue)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * invoque une méthode "<tt>getXxx()</tt>".
	 * 
	 * @throws XmlFieldXPathException
	 */
	private Object doGet(final String methodName) throws NoSuchMethodException,
			XmlFieldXPathException {
		if (xmlField.isGetterCache() && cacheExists(methodName)) {
			return getFromCache(methodName);
		}

		final Object value = getMethodValue(methodName);

		if (value == null) {
			setIntoCache(methodName, value);
			return null;
		}

		final Class<?> returnType = type.getMethod(methodName).getReturnType();

		final Class<? extends Object> valueClass = value.getClass();

		if (!isCompatible(valueClass, returnType)) {

			throw new RuntimeException("Expected: " + returnType.getName()
					+ " on method " + methodName
					+ "(), but stored value has type: " + valueClass.getName()
					+ " for class: " + type.getName());
		}
		setIntoCache(methodName, value);
		return value;
	}

	/**
	 * invoque la méthode "<tt>hashCode()</tt>".
	 * 
	 * @throws XmlFieldXPathException
	 */
	private Object doHashCode() throws XmlFieldXPathException {
		int hash = 0;

		for (final String methodName : methodNames) {

			if (!isMethodNameGetter(methodName)) {

				continue;
			}

			final Object value = getMethodValue(methodName);

			hash *= 5;

			hash += methodName.hashCode();

			hash *= 3;

			if (value != null) {

				hash += value.hashCode();
			}
		}

		return hash;
	}

	/**
	 * invoque une méthode "<tt>isNullXxx()</tt>".
	 * 
	 * @throws XmlFieldXPathException
	 */
	private Object doIsNull(final String methodName)
			throws XmlFieldXPathException {
		if (xmlField.isGetterCache() && cacheExists(methodName)) {
			return getFromCache(methodName);
		}

		final Object rawValue = getMethodDomValue("get"
				+ methodName.substring(6));

		final Boolean isNull;
		if (rawValue instanceof XmlFieldNode) {
			isNull = ((XmlFieldNode) rawValue).getNode() == null;
			setIntoCache(methodName, isNull);
			return isNull;
		}
		isNull = rawValue == null;
		setIntoCache(methodName, isNull);
		return isNull;
	}

	/**
	 * invoque une méthode "<tt>addToXxx()</tt>".
	 */
	private Object doNew(final Object proxy, final Method method,
			final Class<?> type) throws Exception {
		removeFromCache(method);

		final String fieldXPath = getFieldXPath(method);

		return add(proxy, fieldXPath, type);
	}

	/**
	 * Remove object from xml.
	 */
	private Object doRemoveFrom(Method method, Object obj) throws Exception {
		removeFromCache(method);

		XmlFieldUtils.remove(obj, xmlField);

		return null;
	}

	/**
	 * Invoke method "<tt>setXxx(Object obj)</tt>".
	 * <p>
	 * Behavior :
	 * <ul>
	 * <li>if obj == null -> remove node</li>
	 * <li>if obj == null -> remove node</li>
	 * 
	 * </ul>
	 * 
	 * @throws XmlFieldXPathException
	 */
	private Object doSet(final Method method, final Object value)
			throws XmlFieldXPathException {
		removeFromCache(method);

		final String fieldXPath = getFieldXPath(method);

		final XmlFieldNode contextNode;

		XmlFieldNode n;

		if (value == null) {
			// Value is null. We have to delete the current value.
			n = xmlField._getSelector().selectXPathToNode(namespaces,
					fieldXPath, node);
			if (n == null) {
				// No node was matching the Xpath. Value is already null
				if (logger.isDebugEnabled()) {
					logger.debug("value null, node null");
				}
			} else if (n.getNodeType() == XmlFieldNode.ATTRIBUTE_NODE) {
				final String attributeName = n.getNodeName();
				n = node;
				if (!n.hasAttributes()) {
					// the resource is not the node who contains the attributes
					String elementXPath = XPathUtils
							.getElementXPath(fieldXPath);
					n = xmlField._getSelector().selectXPathToNode(namespaces,
							elementXPath, node);
				}
				xmlField._getModifier().removeAttribute(n, attributeName);
			} else {
				// Remove all matching nodes.
				XmlFieldNodeList nodesToRemove = xmlField._getSelector()
						.selectXPathToNodeList(namespaces, fieldXPath, node);
				xmlField._getModifier().removeChildren(nodesToRemove);
			}

		} else {
			// We have to set a value.
			// First : create all parent nodes.
			contextNode = addParentNodes(node, fieldXPath, type);

			// Ensure we have an array to loop on. If single item, convert to
			// array.
			Object[] items = null;
			if (value instanceof Object[]) {
				items = (Object[]) value;

				if (!(items[0] instanceof XmlFieldObject)) {
					if (logger.isWarnEnabled()) {
						logger.warn("You are using "
								+ type.getName()
								+ "#"
								+ method.getName()
								+ "()"
								+ " with an array of a Java primitive type."
								+ " This usage is not able to ensure that additionnal data (such as org.xmlfield.tests.attribute)"
								+ " are not erased during call. Please use the corresponding xml-field type implementation instead."
								+ " String -> XmlString for instance.");
					}
				}
			} else {
				items = new Object[] { value };
			}

			// Get all matching nodes
			XmlFieldNodeList nodeXmlFieldList = xmlField._getSelector()
					.selectXPathToNodeList(namespaces,
							XPathUtils.getElementNameWithSelector(fieldXPath),
							contextNode);

			// Loop on new values
			XmlFieldNode currentNode = null;
			Object currentValue = null;
			String stringValue = null;

			// Loop 1 : reorder
			XmlFieldNode valueNode = null;
			boolean listUpdated = false;
			for (int i = 0; i < items.length; i++) {
				// Get current existing node and value.
				// Note: currentNode may be null, node will be created.
				currentNode = nodeXmlFieldList.item(i);
				currentValue = items[i];

				if (currentValue instanceof XmlFieldObject) {
					valueNode = ((XmlFieldObject) currentValue).toNode();
					xmlField._getModifier().insertBefore(
							valueNode.getParentNode(), valueNode, currentNode);
					listUpdated = true;
				}
			}

			// Update list if necessary
			if (listUpdated) {
				nodeXmlFieldList = xmlField
						._getSelector()
						.selectXPathToNodeList(
								namespaces,
								XPathUtils
										.getElementNameWithSelector(fieldXPath),
								contextNode);
			}

			// Java bug : we have to call item() once with a valid node to make
			// this method work again.
			// see : http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6333993
			// currentNode = nodeList.item(0);

			// Loop 2 :Assign values
			for (int i = 0; i < items.length; i++) {
				// Get current existing node and value.
				// Note: currentNode may be null, node will be created.
				currentNode = nodeXmlFieldList.item(i);
				currentValue = items[i];

				if (currentValue instanceof XmlFieldObject) {
					// Values are already set by setter methods
					continue;
				}

				if (currentValue instanceof DateTime) {

					final String pattern = getFieldFormat(method);

					DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
					if (pattern != null) {
						formatter = DateTimeFormat.forPattern(pattern)
								.withChronology(ISOChronology.getInstanceUTC());
					}
					final DateTime d = (DateTime) currentValue;
					stringValue = d.toString(formatter);
				} else {
					stringValue = currentValue.toString();
				}

				if (currentNode == null) {
					// Node didn't exist : create new node
					XmlFieldUtils.createComplexElement(namespaces, contextNode,
							XPathUtils.getElementNameWithSelector(fieldXPath),
							stringValue, xmlField);
				} else {
					// Node exists : set value.
					currentNode.setTextContent(stringValue);
				}
			}

			// Remove additional items
			for (int i = items.length; i < nodeXmlFieldList.getLength(); i++) {
				currentNode = nodeXmlFieldList.item(i);
				xmlField._getModifier().removeChild(
						currentNode.getParentNode(), currentNode);
			}
		}

		return null;
	}

	/**
	 * invoque une méthode "<tt>sizeOfXxx()</tt>".
	 * 
	 * @throws XmlFieldXPathException
	 */
	private Object doSizeOf(final String methodName)
			throws XmlFieldXPathException {

		final Object value = getMethodValue("get" + methodName.substring(6));

		if (value == null) {

			return 0;
		}

		if (value.getClass().isArray()) {

			return ((Object[]) value).length;
		}

		return 1;
	}

	/**
	 * invoque la méthode "<tt>toString()</tt>".
	 * 
	 * @throws XmlFieldXPathException
	 */
	private Object doToString() throws XmlFieldXPathException {

		final StringBuilder sb = new StringBuilder("{");

		boolean start = true;

		for (final String methodName : methodNames) {

			if (!isMethodNameGetter(methodName)) {

				continue;
			}

			final Object value = getMethodValue(methodName);

			if (value == null) {

				continue;
			}

			if (start) {

				start = false;

			} else {

				sb.append(", ");
			}

			sb.append(Character.toLowerCase(methodName.charAt(3)));
			sb.append(methodName.substring(4));
			sb.append(": ");

			if (value.getClass().isArray()) {
				sb.append(ArrayUtils.toString(value));
			} else {
				sb.append(value.toString());
			}
		}

		sb.append("}");

		return sb.toString();
	}

	/**
	 * Builds cache key from method name.<br/>
	 * Method name can be either getter, setters or others. Normalize all these
	 * names.
	 * 
	 * @param methodName
	 *            method name
	 * @return cache key
	 */
	private String getCacheKey(final String methodName) {
		final String[] prefixes = new String[] { "get", "set", "addTo",
				"removeFrom", "new", "is" };
		for (String p : prefixes) {
			if (methodName.startsWith(p)) {
				return methodName.substring(p.length());
			}
		}
		throw new XmlFieldTechnicalException("Methode non cacheable: "
				+ methodName);
	}

	/**
	 * Retrieves a value from cache.
	 * 
	 * @param methodName
	 *            invoked method name.
	 * @return value contained in cache.
	 */
	private Object getFromCache(final String methodName) {
		return cache.get(getCacheKey(methodName));
	}

	private Method getMethodByName(final String methodName) {

		for (final Method method : type.getMethods()) {

			if (methodName.equals(method.getName())) {

				final Class<?>[] paramTypes = method.getParameterTypes();

				if (paramTypes == null || paramTypes.length == 0) {

					return method;
				}
			}
		}

		return null;
	}

	private Object getMethodDomValue(final String methodName)
			throws XmlFieldXPathException {

		Method method = getMethodByName(methodName);

		if (method == null) {
			return null;
		}

		final String fieldXPath = getFieldXPath(method);

		if (fieldXPath == null) {
			return null;
		}

		final Object value;

		final Class<?> xpathType = getFieldXPathType(method);

		if (Number.class.equals(xpathType)) {

			final Double d = xmlField._getSelector().selectXPathToNumber(
					namespaces, fieldXPath, node);

			final double v = d == null ? 0 : d.doubleValue();

			value = v;

		} else if (String.class.equals(xpathType)) {

			final String s = xmlField._getSelector().selectXPathToString(
					namespaces, fieldXPath, node);

			value = s;

		} else if (Boolean.class.equals(xpathType)) {

			final Boolean b = xmlField._getSelector().selectXPathToBoolean(
					namespaces, fieldXPath, node);

			final boolean v = b == null ? false : b.booleanValue();

			value = v;

		} else {

			final XmlFieldNode n = xmlField._getSelector().selectXPathToNode(
					namespaces, fieldXPath, node);

			value = n;
		}

		return value;
	}

	/**
	 * récupère de façon dynamique la valeur d'un champ repéré par une
	 * expression XPath, donnée en annotation d'une méthode.
	 * 
	 * @param methodName
	 *            le nom de la méthode.
	 * @return la valeur du champ repéré par le XPath donné en annotation de la
	 *         méthode, ou <tt>null</tt> si le champ n'existe pas.
	 * @throws XmlFieldXPathException
	 */
	private Object getMethodValue(final String methodName)
			throws XmlFieldXPathException {

		final Object domValue = getMethodDomValue(methodName);

		final Method method = getMethodByName(methodName);

		final Class<?> fieldType = method.getReturnType();

		final String fieldXPath = getFieldXPath(method);

		// research Explicit collections
		final Map<String, Class<?>> explicitAssociations = getExplicitCollections(method);

		final Object value;

		if (String.class.equals(fieldType)) {

			value = parseString(domValue);

		} else if (int.class.equals(fieldType)) {

			value = parseInt(methodName, domValue, fieldXPath);

		} else if (long.class.equals(fieldType)) {

			value = parseLong(methodName, domValue, fieldXPath);

		} else if (short.class.equals(fieldType)) {

			value = parseShort(methodName, domValue, fieldXPath);

		} else if (float.class.equals(fieldType)) {

			value = parseFloat(methodName, domValue, fieldXPath);

		} else if (double.class.equals(fieldType)) {

			value = parseDouble(methodName, domValue, fieldXPath);

		} else if (boolean.class.equals(fieldType)) {

			value = parseBoolean(methodName, domValue, fieldXPath);

		} else if (Number.class.isAssignableFrom(fieldType)) {

			value = parseNumber(methodName, fieldType, domValue, fieldXPath);

		} else if (DateTime.class.equals(fieldType)) {

			value = parseDateTime(methodName, domValue, method, fieldXPath);

		} else if (fieldType.isArray() && explicitAssociations.size() != 0) {
			// case of an explicit collection
			value = xmlField.nodeToExplicitArray(fieldXPath, node,
					explicitAssociations);

		} else if (fieldType.isArray()) {
			// cas nominal
			value = xmlField.nodeToArray(fieldXPath, node,
					fieldType.getComponentType());

		} else if (fieldType.isEnum()) {
			value = parseEnum(domValue, (Class<? extends Enum>) fieldType);
		} else if (isXmlFieldInterface(fieldType)) {
			value = xmlField.nodeToObject(fieldXPath, node, fieldType);

		} else {

			throw new NotImplementedException("fieldType: " + type
					+ ", method: " + method);
		}

		return value;
	}

	public XmlFieldNode getNode() {
		return node;
	}

	@Override
	public Object invoke(final Object proxy, final Method method,
			final Object[] args) throws Throwable {

		final String methodName = method.getName();

		final boolean noArg = args == null || args.length == 0;

		if ("toString".equals(methodName) && noArg) {

			return doToString();

		} else if ("hashCode".equals(methodName) && noArg) {

			return doHashCode();

		} else if ("equals".equals(methodName) && !noArg && args.length == 1) {

			return doEquals(proxy, args[0]);

		} else if ("toNode".equals(methodName) && noArg) {

			return node;

		} else if (isMethodNameGetter(methodName) && noArg) {

			return doGet(methodName);

		} else if (methodName.startsWith("set") && !noArg && args.length == 1) {

			return doSet(method, args[0]);

		} else if (methodName.startsWith("addTo") && noArg) {

			return doAddTo(proxy, method, method.getReturnType());

		} else if (methodName.startsWith("addTo") && !noArg) {

			return doAddTo(proxy, method, args[0]);

		} else if (methodName.startsWith("new") && noArg) {
			Object presentObject = doGet(methodName);
			if (presentObject != null) {
				return presentObject;
			}
			return doNew(proxy, method, method.getReturnType());

		} else if (methodName.startsWith("isNull") && noArg) {

			return doIsNull(methodName);

		} else if (methodName.startsWith("sizeOf") && noArg) {

			return doSizeOf(methodName);
		} else if (methodName.startsWith("removeFrom") && args.length == 1) {

			return doRemoveFrom(method, args[0]);
		}

		return null;
	}

	private boolean isXmlFieldInterface(final Class<?> fieldType) {
		return XmlFieldUtils.getResourceXPath(fieldType) != null;
	}

	private boolean parseBoolean(final String methodName,
			final Object domValue, final String fieldXPath) {
		if (Boolean.class.isInstance(domValue)) {
			return (Boolean) domValue;
		}
		if (domValue instanceof XmlFieldNode) {
			String textContent = ((XmlFieldNode) domValue).getTextContent();
			try {
				return Boolean.parseBoolean(textContent);
			} catch (final RuntimeException e) {
				logger.error("Cannot parse boolean: " + textContent
						+ //
						", xpath=" + fieldXPath + ", method=" + methodName
						+ "()");
				logger.warn("Cannot parse boolean (details) : ", e);
			}
		}

		return false;
	}

	private DateTime parseDateTime(final String methodName,
			final Object domValue, final Method method, final String fieldXPath) {

		final XmlFieldNode n = (XmlFieldNode) domValue;

		if (n != null) {

			final String textContent = n.getTextContent();

			final String pattern = getFieldFormat(method);

			DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
			if (pattern != null) {
				formatter = DateTimeFormat.forPattern(pattern).withChronology(
						ISOChronology.getInstanceUTC());
			}

			try {

				return formatter.parseDateTime(textContent);

			} catch (final RuntimeException e) {

				e.printStackTrace();

				logger.error("Cannot parse DateTime: " + textContent
						+ ", xpath=" + fieldXPath + ", method=" + methodName
						+ "()");
				logger.warn("Cannot parse DateTime (details) : ", e);
			}
		}

		return null;
	}

	private double parseDouble(final String methodName, final Object domValue,
			final String fieldXPath) {

		if (Double.class.isInstance(domValue)) {

			return (Double) domValue;

		} else if (Integer.class.isInstance(domValue)) {

			return ((Integer) domValue).intValue();

		} else if (Long.class.isInstance(domValue)) {

			return ((Long) domValue).longValue();

		} else if (Short.class.isInstance(domValue)) {

			return ((Short) domValue).shortValue();

		} else if (Float.class.isInstance(domValue)) {

			return ((Float) domValue).floatValue();

		} else {

			final XmlFieldNode n = (XmlFieldNode) domValue;

			if (n != null) {

				final String textContent = n.getTextContent();

				try {

					return Double.parseDouble(textContent);

				} catch (final RuntimeException e) {

					logger.error("Cannot parse double: " + textContent
							+ ", xpath=" + fieldXPath + ", method="
							+ methodName + "()");
					logger.warn("Cannot parse double (details) : ", e);
				}
			}
		}

		return 0;
	}

	private Object parseEnum(final Object domValue,
			final Class<? extends Enum> fieldType) {
		String s = parseString(domValue);
		if (StringUtils.isEmpty(s)) {
			return null;
		}
		return Enum.valueOf(fieldType, s);
	}

	private float parseFloat(final String methodName, final Object domValue,
			final String fieldXPath) {
		if (domValue instanceof Number) {
			return ((Number) domValue).floatValue();
		}
		if (domValue instanceof XmlFieldNode) {
			String textContent = ((XmlFieldNode) domValue).getTextContent();
			try {
				return Float.parseFloat(textContent);
			} catch (final RuntimeException e) {
				logger.error("Cannot parse float: " + textContent
						+ //
						", xpath=" + fieldXPath + ", method=" + methodName
						+ "()");
				logger.warn("Cannot parse float (details) : ", e);
			}
		}
		return 0;
	}

	private int parseInt(final String methodName, final Object domValue,
			final String fieldXPath) {
		if (domValue instanceof Number) {
			return ((Number) domValue).intValue();
		}
		if (domValue instanceof XmlFieldNode) {
			String textContent = ((XmlFieldNode) domValue).getTextContent();
			try {
				return Integer.parseInt(textContent);
			} catch (final RuntimeException e) {
				logger.error("Cannot parse int: " + textContent
						+ //
						", xpath=" + fieldXPath + ", method=" + methodName
						+ "()");
				logger.warn("Cannot parse int (details) : ", e);
			}
		}

		return 0;
	}

	private long parseLong(final String methodName, final Object domValue,
			final String fieldXPath) {

		if (domValue instanceof Number) {
			return ((Number) domValue).longValue();
		}

		if (domValue instanceof XmlFieldNode) {
			String textContent = ((XmlFieldNode) domValue).getTextContent();
			try {
				return Long.parseLong(textContent);
			} catch (final RuntimeException e) {
				logger.error("Cannot parse long: " + textContent + ", xpath="
						+ fieldXPath + //
						", method=" + methodName + "()");
				logger.warn("Cannot parse long (details) : ", e);
			}
		}

		return 0;
	}

	private Number parseNumber(String methodName, Class<?> fieldType,
			Object domValue, String fieldXPath) {

		if (Number.class.isInstance(domValue)) {
			if (fieldType == Byte.class) {
				return ((Number) domValue).byteValue();
			}
			if (fieldType == Double.class) {
				return ((Number) domValue).doubleValue();
			}
			if (fieldType == Float.class) {
				return ((Number) domValue).floatValue();
			}
			if (fieldType == Short.class) {
				return ((Number) domValue).shortValue();
			}

			if (fieldType == Integer.class) {
				return ((Number) domValue).intValue();
			}
			if (fieldType == Long.class) {
				return ((Number) domValue).longValue();
			}
		}

		if (domValue instanceof XmlFieldNode) {
			String textContent = ((XmlFieldNode) domValue).getTextContent();
			try {
				if (fieldType == Byte.class) {
					return Byte.parseByte(textContent);
				}
				if (fieldType == Double.class) {
					return Double.parseDouble(textContent);
				}
				if (fieldType == Float.class) {
					return Float.parseFloat(textContent);
				}
				if (fieldType == Short.class) {
					return Short.parseShort(textContent);
				}
				if (fieldType == Integer.class) {
					return Integer.parseInt(textContent);
				}
				if (fieldType == Long.class) {
					return Long.parseLong(textContent);
				}
			} catch (final NumberFormatException e) {
				logger.error(
						"Cannot parse Number: {} , xpath= {} , method= {}()",
						new Object[] { textContent, fieldXPath, methodName, e });
			}
		}
		return null;
	}

	private short parseShort(final String methodName, final Object domValue,
			final String fieldXPath) {

		if (domValue instanceof Number) {
			return ((Number) domValue).shortValue();
		}
		if (domValue instanceof XmlFieldNode) {

			XmlFieldNode n = (XmlFieldNode) domValue;
			String textContent = n.getTextContent();

			try {
				return Short.parseShort(textContent);
			} catch (final RuntimeException e) {
				logger.error("Cannot parse short: " + textContent + ", xpath="
						+ fieldXPath + //
						", method=" + methodName + "()");
				logger.warn("Cannot parse short (details) : ", e);
			}
		}

		return 0;
	}

	private String parseString(final Object domValue) {
		if (domValue instanceof String) {
			return (String) domValue;
		}
		if (domValue instanceof XmlFieldNode) {
			return ((XmlFieldNode) domValue).getTextContent();
		}
		return null;
	}

	/**
	 * Removes any value contained in cache for the specified invoked method.
	 * 
	 * @param method
	 *            invoked method.
	 */
	private void removeFromCache(final Method method) {
		removeFromCache(method.getName());
	}

	/**
	 * Removes any value contained in cache for the specified invoked method
	 * name.
	 * 
	 * @param methodName
	 *            invoked method name.
	 */
	private void removeFromCache(final String methodName) {
		cache.remove(getCacheKey(methodName));
	}

	/**
	 * Put a value into cache.
	 * 
	 * @param methodName
	 *            method name
	 * @param value
	 *            value to put
	 */
	private void setIntoCache(final String methodName, final Object value) {
		cache.put(getCacheKey(methodName), value);
	}
}
