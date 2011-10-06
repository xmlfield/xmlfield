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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Node;
import org.xmlfield.annotations.Association;
import org.xmlfield.annotations.ExplicitCollection;
import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.Namespaces;
import org.xmlfield.annotations.ResourceXPath;
import org.xmlfield.core.XmlFieldBinder;
import org.xmlfield.core.XmlFieldNode;
import org.xmlfield.core.XmlFieldNodeList;
import org.xmlfield.core.XmlFieldNodeModifier;
import org.xmlfield.core.XmlFieldNodeModifierFactory;
import org.xmlfield.core.XmlFieldSelector;
import org.xmlfield.core.XmlFieldSelectorFactory;
import org.xmlfield.core.exception.XmlFieldXPathException;
import org.xmlfield.utils.XPathUtils;

/**
 * Xml manipulation node utility class.
 * 
 * @author David Andrianavalontsalama
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 */
public abstract class XmlFieldUtils {

    /**
     * Namespaces container class.
     * 
     * @author PGMY03781
     * 
     */
    public static class NamespaceMap implements Iterable<Map.Entry<String, String>> {

        private final Map<String, String> prefixesURIs = new HashMap<String, String>();

        public NamespaceMap(final String... namespaces) {
            if (namespaces != null) {
                for (final String n : namespaces) {
                    final String prefix = substringBetween(n, ":", "=");

                    if (prefix == null) {

                        throw new IllegalArgumentException("Illegal namespace prefix for XPath expressions: " + n);
                    }

                    final String uri = substringAfter(n, "=").replace("\"", "");
                    prefixesURIs.put(prefix, uri);
                }
            }
        }

        private NamespaceMap(final Namespaces namespaces) {
            this(namespaces == null ? new String[0] : namespaces.value());
        }

        public String get(String prefix) {
            return prefixesURIs.get(prefix);
        }

        public Map<String, String> getPrefixesURIs() {
            return prefixesURIs;
        }

        public boolean isEmpty() {
            return prefixesURIs.isEmpty();
        }

        @Override
        public Iterator<Entry<String, String>> iterator() {
            return prefixesURIs.entrySet().iterator();
        }

        private void addNamespaces(final NamespaceMap nMap) {
            if (nMap != null) {
                prefixesURIs.putAll(nMap.prefixesURIs);
            }
        }

    }

    /**
     * All the method prefixes known by xmlfield.
     */
    private static final String[] METHOD_PREFIXES = { "set", "get", "has", "is", "addTo", "sizeOf", "isNull", "new",
            "removeFrom" };

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
    public static <T> T add(final Object root, final String xpath, final Class<T> type) throws XmlFieldXPathException {
        return add(getXmlFieldNode(root), xpath, type);
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
    public static <T> T add(final XmlFieldNode<?> root, final String xpath, final Class<T> type)
            throws XmlFieldXPathException {

        final XmlFieldNode<?> node = addNode(root, xpath, type);

        final XmlFieldBinder binder = new XmlFieldBinder();

        return binder.bind(null, node, type);
    }

    /**
     * Add the specified binded node at the end of the xpath location.
     * 
     * @param root
     *            root element
     * @param fieldXPath
     *            xpath location relative to the root element where we want to add the new element
     * @param type
     *            element type to add
     * @return the new node
     * @throws XmlFieldXPathException
     *             xpath exception
     */
    public static XmlFieldNode<?> addNode(final XmlFieldNode<?> root, final String fieldXPath, final Class<?> type)
            throws XmlFieldXPathException {

        final NamespaceMap namespaces = getResourceNamespaces(type);
        final XmlFieldNode<?> parentNode = addParentNodes(root, fieldXPath, type);

        // Create requested node.

        final String elementName = XPathUtils.getElementNameWithSelector(fieldXPath);
        final XmlFieldNode<?> node = createComplexElement(namespaces, parentNode, elementName, null);

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
    public static XmlFieldNode<?> addParentNodes(final XmlFieldNode<?> root, final String fieldXPath,
            final Class<?> type) throws XmlFieldXPathException {

        final NamespaceMap namespaces = getResourceNamespaces(type);

        final XmlFieldSelector selector = XmlFieldSelectorFactory.newInstance().newSelector();

        final XmlFieldNode<?> parentNode;

        // Check if this type of element already exists in the document.
        final XmlFieldNodeList nodeList = selector.selectXPathToNodeList(namespaces, fieldXPath, root);

        if (nodeList != null && nodeList.getLength() > 0) {
            // Siblings exist. We will add item to their parent.
            if (nodeList.item(0).getNodeType() == XmlFieldNode.ATTRIBUTE_NODE) {
                String xPathElement = XPathUtils.getElementXPath(fieldXPath);
                if (xPathElement != null) {
                    parentNode = selector.selectXPathToNode(namespaces, xPathElement, root);
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
                XmlFieldNode<?> node;

                // Loop over the XPath hierarchy
                for (String xPathBuffer = fieldXPath;;) {
                    // Remove field name. Keep only parents
                    xPathBuffer = substringBeforeLast(xPathBuffer, "/");

                    // Ensure xpath was valid.
                    if (isBlank(xPathBuffer)) {
                        throw new IllegalStateException("Unable to create child in list with XPath: " + fieldXPath);
                    }

                    // If parent node already exists, we can create the element
                    // directly.
                    final XmlFieldNodeList nList = selector.selectXPathToNodeList(namespaces, xPathBuffer, root);
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
                    final XmlFieldNode<?> n = createComplexElement(namespaces, node, elementName, null);
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
     * 
     * @param namespaces
     * @param contextNode
     * @param elementName
     * @param stringValue
     * @return
     */
    public static XmlFieldNode<?> createComplexElement(NamespaceMap namespaces, XmlFieldNode<?> contextNode,
            String elementName, String stringValue) {

        XmlFieldNode<?> result = contextNode;

        XmlFieldNodeModifier modifier = XmlFieldNodeModifierFactory.newInstance().newModifier();

        // Create required node
        switch (XPathUtils.getElementType(elementName)) {
        case XPathUtils.TYPE_ATTRIBUTE:
            modifier.createAttribute(contextNode, elementName.substring(1), stringValue);
            break;
        case XPathUtils.TYPE_TAG:

            result = modifier.createElement(namespaces, contextNode, elementName, stringValue);
            break;

        case XPathUtils.TYPE_TAG_WITH_ATTRIBUTE:
            // Create tag
            String tagName = XPathUtils.getElementName(elementName);
            result = modifier.createElement(namespaces, contextNode, tagName, stringValue);

            // Then create attributes in selector
            Map<String, String> attributes = XPathUtils.getElementSelectorAttributes(elementName);
            for (String key : attributes.keySet()) {
                modifier.createAttribute(result, key, attributes.get(key));
            }

            break;
        }

        return result;

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
            elementXPath = XPathUtils.getElementXPath(getResourceXPath(type) + "/" + fieldXPath);
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
    public static Map<String, Class<?>> getExplicitCollections(final Method method) {

        Map<String, Class<?>> explicitAssociations = new HashMap<String, Class<?>>();

        if (method == null) {
            return null;
        }

        final ExplicitCollection explicitCollection = method.getAnnotation(ExplicitCollection.class);
        
        if (explicitCollection != null) {

        	for (int j = 0; j < explicitCollection.value().length; j++) {
                // looking for ExplicitCollectionAssociation in a ExplicitCollection
                if (explicitCollection.value()[j] != null) {
                    Association explicitCollectionAssociation = explicitCollection.value()[j];
                    explicitAssociations.put(explicitCollectionAssociation.xpath(),
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

                final String methodSuffix = methodName.substring(prefix.length());

                final Method getterMethod;

                try {

                    getterMethod = method.getDeclaringClass().getMethod("get" + methodSuffix);

                } catch (final NoSuchMethodException e) {

                    return null;
                }

                return getExplicitCollections(getterMethod);
            }
        }

        return explicitAssociations;
    }

    /**
     * récupère le contenu de l'annotation @{@link FieldFormat} sur une méthode, ou <tt>null</tt>. Si la méthode passée
     * en paramètre est un <em>setter</em>, cette méthode regarde aussi du côté du <em>getter</em> correspondant.
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
     * récupère le contenu de l'annotation @{@link FieldXPath} sur une méthode, ou <tt>null</tt>. Si la méthode passée
     * en paramètre est un <em>setter</em>, cette méthode regarde aussi du côté du <em>getter</em> correspondant.
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
     * récupère le contenu de l'annotation @{@link FieldXPathType} sur une méthode, ou <tt>null</tt>.
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
     * récupérer le nœud XML qui correspond à un objet.
     * 
     * @param object
     *            l'objet.
     * @return le nœud XML qui correspond à l'objet.
     */
    @Deprecated
    public static Node getNode(final Object object) {
        if (object instanceof INodeable<?>) {
            return ((INodeable<Node>) object).toNode().getNode();
        }
        if (object instanceof Node) {
            return (Node) object;
        }

        return null;
    }

    /**
     * récupère l'annotation @{@link Namespaces} sur une class, ou <tt>null</tt> .
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
     * récupère le contenu de l'annotation @{@link ResourceXPath} sur une class, ou <tt>null</tt>.
     * 
     * @param type
     *            la classe pour laquelle on veut le contenu de l'annotation.
     * @return la valeur de l'annotation, ou <tt>null</tt>.
     */
    public static String getResourceXPath(final Class<?> type) {

        if (type == null) {
            return null;
        }

        final ResourceXPath resourceXPathDeclaration = type.getAnnotation(ResourceXPath.class);

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
    public static XmlFieldNode<?> getXmlFieldNode(final Object object) {
        if (object instanceof INodeable<?>) {
            return ((INodeable<?>) object).toNode();
        }
        if (object instanceof XmlFieldNode<?>) {
            return (XmlFieldNode<?>) object;
        }
        return null;
    }

    /**
     * retirer un nœud XML de son parent.
     * 
     * @param node
     *            le nœud à retirer.
     */
    @Deprecated
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
     * Remove element from document. item must have been acquired either by calling
     * {@link XmlFieldBinder#bind(Node, Class)} or any get or addTo method.
     * 
     * @param item
     *            Item to remove
     */
    public static void remove(final Object item) {
        remove(getXmlFieldNode(item));
    }

    /**
     * retirer un nœud XML de son parent.
     * 
     * @param node
     *            le nœud à retirer.
     */
    public static void remove(final XmlFieldNode<?> node) {

        if (node == null) {
            return;
        }

        final XmlFieldNode<?> parent = node.getParentNode();

        if (parent != null) {
            XmlFieldNodeModifier modifier = XmlFieldNodeModifierFactory.newInstance().newModifier();
            modifier.removeChild(parent, node);
        }
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

                final String methodSuffix = methodName.substring(prefix.length());

                final Method getterMethod;

                try {

                    getterMethod = method.getDeclaringClass().getMethod("get" + methodSuffix);

                } catch (final NoSuchMethodException e) {

                    return null;
                }

                return getFieldXPathAnnotation(getterMethod);
            }
        }

        return null;
    }

}
