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
package org.xmlfield.utils;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.substringAfter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xmlfield.core.internal.XmlFieldUtils.NamespaceMap;

/**
 * 
 * 
 * @author Loic Abemonty <loic.abemonty@capgemini.com>
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 * 
 */
public abstract class JaxpUtils {

    static Logger logger = LoggerFactory.getLogger(JaxpUtils.class);
    public static final Attributes NO_ATTRIBUTES = new AttributesImpl();

    public static Attr _createAttribute(final Node node,
            final String attributeName, final String textContent) {

        final Document document = getNodeDocument(node);

        final Attr attribute = document.createAttribute(attributeName);

        ((Element) node).setAttributeNode(attribute);

        if (textContent != null) {

            attribute.setTextContent(textContent);
        }

        return attribute;
    }

    public static Node createElement(final NamespaceMap namespaces,
            final Node node, final String elementName) {

        return createElement(namespaces, node, elementName, null);
    }

    public static Node createElement(final NamespaceMap namespaces,
            final Node node, final String elementName, final String textContent) {

        if (StringUtils.isEmpty(elementName)) {
            return node;
        }

        final Document document = getNodeDocument(node);

        final Element element = _createElement(namespaces, document,
                elementName);

        node.appendChild(element);

        if (textContent != null) {

            element.appendChild(document.createTextNode(textContent));
        }

        return element;
    }

    public static void dumpNode(final Node node)
            throws ParserConfigurationException, SAXException, IOException {

        dumpNode(null, node);
    }

    public static void dumpNode(final String message, final Node node)
            throws ParserConfigurationException, SAXException, IOException {

        final OutputFormat outputFormat = null;

        final Writer sw = new StringWriter();

        final DOMSerializer serializer = new XMLSerializer(sw, outputFormat);

        serializer.serialize((Element) node);

        logger.info((message == null ? "" : (message + ": ")) + sw);
    }

    public static Document getNodeDocument(final Node node) {

        checkNotNull(node, "node");

        for (Node n = node; n != null; n = n.getParentNode()) {

            if (Document.class.isInstance(n)) {

                return (Document) n;
            }
        }

        throw new IllegalArgumentException("Node has no Document ancestor.");
    }

    public static XPath getXPath(final NamespaceMap namespaces) {

        final XPath xpath = XPathFactory.newInstance().newXPath();

        if (namespaces != null) {

            final Map<String, String> prefixesURIs = namespaces
                    .getPrefixesURIs();

            final NamespaceContext ns = new NamespaceContext() {

                @Override
                public String getNamespaceURI(final String prefix) {

                    if (prefix == null) {

                        return null;
                    }

                    final String nsURI = prefixesURIs.get(prefix);

                    return nsURI;
                }

                @Override
                public String getPrefix(final String namespaceURI) {

                    if (namespaceURI == null) {

                        return null;
                    }

                    if (prefixesURIs.containsValue(namespaceURI)) {

                        for (final Map.Entry<String, String> e : prefixesURIs
                                .entrySet()) {

                            if (namespaceURI.equals(e.getValue())) {

                                return e.getKey();
                            }
                        }
                    }

                    return null;
                }

                @Override
                public Iterator<?> getPrefixes(final String namespaceURI) {

                    return prefixesURIs.keySet().iterator();
                }
            };

            xpath.setNamespaceContext(ns);
        }

        return xpath;
    }

    private static Element _createElement(final NamespaceMap namespaces,
            final Document document, final String elementName) {

        String prefix = XPathUtils.getElementPrefix(elementName);

        if (prefix != null) {

            if (namespaces == null) {

                throw new IllegalArgumentException(
                        "No namespaceURI defined for <" + elementName + ">");
            }

            final String uri = namespaces.getPrefixesURIs().get(prefix);

            if (uri == null) {

                throw new IllegalArgumentException(
                        "No namespaceURI defined for <" + elementName + ">");
            }

            final String localName = substringAfter(elementName, ":");

            return document.createElementNS(uri, localName);

        } else {

            return document.createElement(elementName);
        }
    }

    public static Node createComplexElement(NamespaceMap namespaces,
            Node contextNode, String elementName, String stringValue) {

        Node result = contextNode;
        // Create required node
        switch (XPathUtils.getElementType(elementName)) {
        case XPathUtils.TYPE_ATTRIBUTE:

            _createAttribute(contextNode, elementName.substring(1), stringValue);
            break;
        case XPathUtils.TYPE_TAG:

            result = createElement(namespaces, contextNode, elementName,
                    stringValue);
            break;

        case XPathUtils.TYPE_TAG_WITH_ATTRIBUTE:
            // Create tag
            String tagName = XPathUtils.getElementName(elementName);
            result = createElement(namespaces, contextNode, tagName,
                    stringValue);

            // Then create attributes in selector
            Map<String, String> attributes = XPathUtils
                    .getElementSelectorAttributes(elementName);
            for (String key : attributes.keySet()) {
                _createAttribute(result, key, attributes.get(key));
            }

            break;
        }

        return result;

    }
}
