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
import static org.xmlfield.core.internal.XmlFieldUtils.getExplicitCollections;
import static org.xmlfield.core.internal.XmlFieldUtils.getFieldFormat;
import static org.xmlfield.core.internal.XmlFieldUtils.getFieldXPath;
import static org.xmlfield.core.internal.XmlFieldUtils.getFieldXPathType;
import static org.xmlfield.core.internal.XmlFieldUtils.getResourceNamespaces;
import static org.xmlfield.utils.JaxpUtils.getXPath;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.core.XmlFieldReader;
import org.xmlfield.core.internal.XmlFieldUtils.NamespaceMap;
import org.xmlfield.utils.JaxpUtils;
import org.xmlfield.utils.XPathUtils;

/**
 * l'objet {@link InvocationHandler} à utiliser sur les proxies chargés à la
 * lecture des nœuds XML.
 * 
 * @author David Andrianavalontsalama
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
public class XmlFieldInvocationHandler implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(XmlFieldInvocationHandler.class);

    /**
     * vérifie qu'un type réel est compatible avec un type déclaré.
     */
    private static boolean isCompatible(final Class<?> realType, final Class<?> declaredType) {

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

            if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers)) {

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
                || (methodName.startsWith("is") && !methodName.startsWith("isNull"));
    }

    private final Set<String> methodNames = new TreeSet<String>();

    private final NamespaceMap namespaces;

    private final Node node;

    private final Class<?> type;

    private final XmlFieldReader xmlFieldReader;

    /**
     * constructeur, qui dit ce que doit renvoyer chaque méthode <em>getter</em>
     * .
     * 
     * @param xmlFieldReader
     *            l'objet reader, qui permet notamment de récupérer des
     *            sous-champs.
     * @param type
     *            le type de l'objet Java.
     * @param node
     *            le nœud de l'objet Java.
     */
    public XmlFieldInvocationHandler(final XmlFieldReader xmlFieldReader, final Node node, final Class<?> type) {

        this.xmlFieldReader = checkNotNull(xmlFieldReader, "xmlFieldReader");
        this.node = checkNotNull(node, "node");
        this.type = checkNotNull(type, "type");
        this.namespaces = getResourceNamespaces(type);

        for (final Method method : type.getMethods()) {

            final String methodName = method.getName();

            if (method.isAnnotationPresent(FieldXPath.class) && isMethodNameGetter(methodName)) {

                final Class<?>[] paramTypes = method.getParameterTypes();

                if (paramTypes == null || paramTypes.length == 0) {

                    methodNames.add(methodName);
                }
            }
        }
    }

    /**
     * invoque une méthode "<tt>addToXxx()</tt>".
     */
    private Object doNew(final Object proxy, final Method method, final Class<?> type) throws Exception {

        final String fieldXPath = getFieldXPath(method);

        return XmlFieldUtils.add(proxy, fieldXPath, type);
    }

    /**
     * invoque une méthode "<tt>addToXxx()</tt>".
     */
    private Object doAddTo(final Object proxy, final Method method, final Class<?> type) throws Exception {

        final String fieldXPath = getFieldXPath(method);

        return XmlFieldUtils.add(proxy, fieldXPath, type);
    }

    /**
     * invoque la méthode "<tt>equals(Object)</tt>".
     */
    private Object doEquals(final Object proxy, final Object ob) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, XPathExpressionException {

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
     */
    private Object doGet(final String methodName) throws NoSuchMethodException, XPathExpressionException {

        final Object value = getMethodValue(methodName);

        if (value == null) {

            return null;
        }

        final Class<?> returnType = type.getMethod(methodName).getReturnType();

        final Class<? extends Object> valueClass = value.getClass();

        if (!isCompatible(valueClass, returnType)) {

            throw new RuntimeException("Expected: " + returnType.getName() + " on method " + methodName
                    + "(), but stored value has type: " + valueClass.getName() + " for class: " + type.getName());
        }

        return value;
    }

    /**
     * invoque la méthode "<tt>hashCode()</tt>".
     */
    private Object doHashCode() throws XPathExpressionException {
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
     * @throws XPathExpressionException
     */
    private Object doIsNull(final String methodName) throws XPathExpressionException {

        final Object rawValue = getMethodDomValue("get" + methodName.substring(6));

        return rawValue == null;
    }

    /**
     * Remove object from xml.
     */
    private Object doRemoveFrom(Method method, Object obj) throws Exception {

        final String fieldXPath = getFieldXPath(method);

        XmlFieldUtils.remove(obj);

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
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    private Object doSet(final Method method, final Object value) throws XPathExpressionException,
            ParserConfigurationException, SAXException, TransformerException,
            TransformerFactoryConfigurationError {

        final XPath xpath = getXPath(namespaces);

        final String fieldXPath = getFieldXPath(method);

        final Node contextNode;

        Node n;

        if (value == null) {
            // Value is null. We have to delete the current value.

            n = (Node) xpath.evaluate(fieldXPath, node, XPathConstants.NODE);
            if (n == null) {
                // No node was matching the Xpath. Value is already null

                if (logger.isDebugEnabled()) {
                    logger.debug("value null, node null");
                }

            } else if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
                NamedNodeMap nnMap = node.getAttributes();
                if (nnMap.getLength() == 0) {
                    // the resource is not the node who contains the attributes
                    Node e = (Node) xpath
                            .evaluate(XmlFieldUtils.getElementXPath(fieldXPath), node, XPathConstants.NODE);
                    nnMap = e.getAttributes();
                }
                nnMap.removeNamedItem(n.getNodeName());

            } else {
                // Remove all matching nodes.
                NodeList nodeList = (NodeList) xpath.evaluate(fieldXPath, node, XPathConstants.NODESET);
                for (int i = nodeList.getLength() - 1; i >= 0; i--) {
                    Node currentNode = nodeList.item(i);
                    currentNode.getParentNode().removeChild(currentNode);
                }
            }

        } else {
            // We have to set a value.
            // First : create all parent nodes.
            contextNode = XmlFieldUtils.addParentNodes(node, fieldXPath, type);

            // Ensure we have an array to loop on. If single item, convert to
            // array.
            Object[] items = null;
            if (value instanceof Object[]) {
                items = (Object[]) value;

                if (!(items[0] instanceof INodeable)) {
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
            NodeList nodeList = (NodeList) xpath.evaluate(XPathUtils.getElementNameWithSelector(fieldXPath),
                    contextNode, XPathConstants.NODESET);

            // Loop on new values
            Node currentNode = null;
            Object currentValue = null;
            String stringValue = null;

            // Loop 1 : reorder
            Node valueNode = null;
            boolean listUpdated = false;
            for (int i = 0; i < items.length; i++) {
                // Get current existing node and value.
                // Note: currentNode may be null, node will be created.
                currentNode = nodeList.item(i);
                currentValue = items[i];

                if (currentValue instanceof INodeable) {
                    valueNode = ((INodeable) currentValue).toNode();
                    valueNode.getParentNode().insertBefore(valueNode, currentNode);
                    listUpdated = true;
                }
            }

            // Update list if necessary
            if (listUpdated) {
                nodeList = (NodeList) xpath.evaluate(XPathUtils.getElementNameWithSelector(fieldXPath), contextNode,
                        XPathConstants.NODESET);
            }

            // Java bug : we have to call item() once with a valid node to make
            // this method work again.
            // see : http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6333993
            currentNode = nodeList.item(0);

            // Loop 2 :Assign values
            for (int i = 0; i < items.length; i++) {
                // Get current existing node and value.
                // Note: currentNode may be null, node will be created.
                currentNode = nodeList.item(i);
                currentValue = items[i];

                if (currentValue instanceof INodeable) {
                    // Values are already set by setter methods
                    continue;
                }

                if (currentValue instanceof DateTime) {

                    final String pattern = getFieldFormat(method);

                    DateTimeFormatter formatter = ISODateTimeFormat.timeParser();
                    if (pattern != null) {
                        formatter = DateTimeFormat.forPattern(pattern).withChronology(ISOChronology.getInstanceUTC());
                    }
                    final DateTime d = (DateTime) currentValue;
                    stringValue = d.toString(formatter);
                } else {
                    stringValue = currentValue.toString();
                }

                if (currentNode == null) {
                    // Node didn't exist : create new node
                    JaxpUtils.createComplexElement(namespaces, contextNode,
                            XPathUtils.getElementNameWithSelector(fieldXPath), stringValue);
                } else {
                    // Node exists : set value.
                    currentNode.setTextContent(stringValue);
                }
            }

            // Remove additional items
            for (int i = items.length; i < nodeList.getLength(); i++) {
                currentNode = nodeList.item(i);
                currentNode.getParentNode().removeChild(currentNode);
            }
        }

        return null;
    }

    /**
     * invoque une méthode "<tt>sizeOfXxx()</tt>".
     */
    private Object doSizeOf(final String methodName) throws XPathExpressionException {

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
     */
    private Object doToString() throws XPathExpressionException {

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

    private Object getMethodDomValue(final String methodName) throws XPathExpressionException {

        Method method = getMethodByName(methodName);

        if (method == null) {
            return null;
        }

        final String fieldXPath = getFieldXPath(method);

        if (fieldXPath == null) {
            return null;
        }

        final Object value;

        final XPath xpath = JaxpUtils.getXPath(namespaces);

        final Class<?> xpathType = getFieldXPathType(method);

        if (Number.class.equals(xpathType)) {

            final Double d = (Double) xpath.evaluate(fieldXPath, node, XPathConstants.NUMBER);

            final double v = (d == null) ? 0 : d.doubleValue();

            value = v;

        } else if (String.class.equals(xpathType)) {

            final String s = (String) xpath.evaluate(fieldXPath, node, XPathConstants.STRING);

            value = s;

        } else if (Boolean.class.equals(xpathType)) {

            final Boolean b = (Boolean) xpath.evaluate(fieldXPath, node, XPathConstants.BOOLEAN);

            final boolean v = (b == null) ? false : b.booleanValue();

            value = v;

        } else {

            final Node n = (Node) xpath.evaluate(fieldXPath, node, XPathConstants.NODE);

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
     * @throws XPathExpressionException
     */
    private Object getMethodValue(final String methodName) throws XPathExpressionException {

        final Object domValue = getMethodDomValue(methodName);

        final Method method = getMethodByName(methodName);

        final Class<?> fieldType = method.getReturnType();

        final String fieldXPath = getFieldXPath(method);

        // research Explicit collections
        final HashMap<String, Class<?>> explicitAssociations = getExplicitCollections(method);

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

        } else if (DateTime.class.equals(fieldType)) {

            value = parseDateTime(methodName, domValue, method, fieldXPath);

        } else if (fieldType.isArray() && explicitAssociations.size() != 0) {
            // case of an explicit collection
            value = xmlFieldReader.attachExplicitArray(fieldXPath, node, explicitAssociations);

        } else if (fieldType.isArray()) {
            // cas nominal
            value = xmlFieldReader.attachArray(fieldXPath, node, fieldType.getComponentType());

        } else if (fieldType.isEnum()) {
            value = parseEnum(domValue, (Class<? extends Enum>) fieldType);
        } else if (isXmlFieldInterface(fieldType)) {
            value = xmlFieldReader.attach(fieldXPath, node, fieldType);

        } else {

            throw new NotImplementedException("fieldType: " + type + ", method: " + method);
        }

        return value;
    }

    private boolean isXmlFieldInterface(final Class<?> fieldType) {
        return XmlFieldUtils.getResourceXPath(fieldType) != null;
    }

    private Object parseEnum(final Object domValue, final Class<? extends Enum> fieldType) {
        String s = parseString(domValue);
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        return Enum.valueOf(fieldType, s);
    }

    public Node getNode() {
        return node;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        final String methodName = method.getName();

        final boolean noArg = (args == null) || args.length == 0;

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

    private boolean parseBoolean(final String methodName, final Object domValue, final String fieldXPath) {
        if (Boolean.class.isInstance(domValue)) {
            return (Boolean) domValue;
        }
        if (domValue instanceof Node) {
            String textContent = ((Node) domValue).getTextContent();
            try {
                return Boolean.parseBoolean(textContent);
            } catch (final RuntimeException e) {
                logger.error("Cannot parse boolean: " + textContent + //
                        ", xpath=" + fieldXPath + ", method=" + methodName + "()");
                logger.warn("Cannot parse boolean (details) : ", e);
            }
        }

        return false;
    }

    private DateTime parseDateTime(final String methodName, final Object domValue, final Method method,
            final String fieldXPath) {

        final Node n = (Node) domValue;

        if (n != null) {

            final String textContent = n.getTextContent();

            final String pattern = getFieldFormat(method);

            DateTimeFormatter formatter = ISODateTimeFormat.timeParser();
            if (pattern != null) {
                formatter = DateTimeFormat.forPattern(pattern).withChronology(ISOChronology.getInstanceUTC());
            }

            try {

                return formatter.parseDateTime(textContent);

            } catch (final RuntimeException e) {

                e.printStackTrace();

                logger.error("Cannot parse DateTime: " + textContent + ", xpath=" + fieldXPath + ", method="
                        + methodName + "()");
                logger.warn("Cannot parse DateTime (details) : ", e);
            }
        }

        return null;
    }

    private double parseDouble(final String methodName, final Object domValue, final String fieldXPath) {

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

            final Node n = (Node) domValue;

            if (n != null) {

                final String textContent = n.getTextContent();

                try {

                    return Double.parseDouble(textContent);

                } catch (final RuntimeException e) {

                    logger.error("Cannot parse double: " + textContent + ", xpath=" + fieldXPath + ", method="
                            + methodName + "()");
                    logger.warn("Cannot parse double (details) : ", e);
                }
            }
        }

        return 0;
    }

    private float parseFloat(final String methodName, final Object domValue, final String fieldXPath) {
        if (domValue instanceof Number) {
            return ((Number) domValue).floatValue();
        }
        if (domValue instanceof Node) {
            String textContent = ((Node) domValue).getTextContent();
            try {
                return Float.parseFloat(textContent);
            } catch (final RuntimeException e) {
                logger.error("Cannot parse float: " + textContent + //
                        ", xpath=" + fieldXPath + ", method=" + methodName + "()");
                logger.warn("Cannot parse float (details) : ", e);
            }
        }
        return 0;
    }

    private int parseInt(final String methodName, final Object domValue, final String fieldXPath) {
        if (domValue instanceof Number) {
            return ((Number) domValue).intValue();
        }
        if (domValue instanceof Node) {
            String textContent = ((Node) domValue).getTextContent();
            try {
                return Integer.parseInt(textContent);
            } catch (final RuntimeException e) {
                logger.error("Cannot parse int: " + textContent + //
                        ", xpath=" + fieldXPath + ", method=" + methodName + "()");
                logger.warn("Cannot parse int (details) : ", e);
            }
        }

        return 0;
    }

    private long parseLong(final String methodName, final Object domValue, final String fieldXPath) {

        if (domValue instanceof Number) {
            return ((Number) domValue).longValue();
        }

        if (domValue instanceof Node) {
            String textContent = ((Node) domValue).getTextContent();
            try {
                return Long.parseLong(textContent);
            } catch (final RuntimeException e) {
                logger.error("Cannot parse long: " + textContent + ", xpath=" + fieldXPath + //
                        ", method=" + methodName + "()");
                logger.warn("Cannot parse long (details) : ", e);
            }
        }

        return 0;
    }

    private short parseShort(final String methodName, final Object domValue, final String fieldXPath) {

        if (domValue instanceof Number) {
            return ((Number) domValue).shortValue();
        }
        if (domValue instanceof Node) {

            Node n = (Node) domValue;
            String textContent = n.getTextContent();

            try {
                return Short.parseShort(textContent);
            } catch (final RuntimeException e) {
                logger.error("Cannot parse short: " + textContent + ", xpath=" + fieldXPath + //
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
        if (domValue instanceof Node) {
            return ((Node) domValue).getTextContent();
        }
        return null;
    }
}
