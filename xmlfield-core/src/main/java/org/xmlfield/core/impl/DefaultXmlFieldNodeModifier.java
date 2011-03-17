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
package org.xmlfield.core.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.substringAfter;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xmlfield.core.XmlFieldNode;
import org.xmlfield.core.XmlFieldNodeList;
import org.xmlfield.core.XmlFieldNodeModifier;
import org.xmlfield.core.internal.XmlFieldUtils.NamespaceMap;
import org.xmlfield.utils.XPathUtils;

/**
 * Default xml field node modifier implementation
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * 
 */
public class DefaultXmlFieldNodeModifier implements XmlFieldNodeModifier {

    private static Element _createElement(final NamespaceMap namespaces, final Document document,
            final String elementName) {

        String prefix = XPathUtils.getElementPrefix(elementName);

        if (prefix == null) {
            return document.createElement(elementName);
        }

        if (namespaces == null) {
            throw new IllegalArgumentException("No namespaceURI defined for <" + elementName + ">");
        }

        String uri = namespaces.get(prefix);

        if (uri == null) {
            throw new IllegalArgumentException("No namespaceURI defined for <" + elementName + ">");
        }

        String localName = substringAfter(elementName, ":");

        return document.createElementNS(uri, localName);

    }

    @Override
    public void createAttribute(XmlFieldNode<?> node, String attributeName, String textContent) {
        final Document document = getNodeDocument((Node) node.getNode());

        final Attr attribute = document.createAttribute(attributeName);

        ((Element) node.getNode()).setAttributeNode(attribute);

        if (textContent != null) {

            attribute.setTextContent(textContent);
        }

    }

    @Override
    public XmlFieldNode<?> createElement(NamespaceMap namespaces, XmlFieldNode<?> node, String elementName) {
        return createElement(namespaces, node, elementName, null);
    }

    @Override
    public XmlFieldNode<?> createElement(NamespaceMap namespaces, XmlFieldNode<?> node, String elementName,
            String textContent) {
        checkNotNull(node, "node");
        checkArgument(node.getNode() instanceof Node);
        if (StringUtils.isEmpty(elementName)) {
            return node;
        }

        final Document document = getNodeDocument((Node) node.getNode());

        final Element element = _createElement(namespaces, document, elementName);

        ((Node) node.getNode()).appendChild(element);

        if (textContent != null) {

            element.appendChild(document.createTextNode(textContent));
        }

        return new DefaultXmlFieldNode(element);
    }

    @Override
    public XmlFieldNode<?> insertBefore(XmlFieldNode<?> contextNode, XmlFieldNode<?> newChild, XmlFieldNode<?> refChild) {
        Node insertedNode = ((Node) contextNode.getNode()).insertBefore((Node) newChild.getNode(),
                (Node) refChild.getNode());
        return new DefaultXmlFieldNode(insertedNode);
    }

    @Override
    public XmlFieldNode<?> removeAttribute(XmlFieldNode<?> node, String attibuteName) {
        checkNotNull(node, "node");
        checkNotNull(attibuteName, "attributeName");
        NamedNodeMap nnMap = ((Node) node.getNode()).getAttributes();
        return new DefaultXmlFieldNode(nnMap.removeNamedItem(attibuteName));
    }

    @Override
    public XmlFieldNode<?> removeChild(XmlFieldNode<?> node, XmlFieldNode<?> oldChild) {
        checkNotNull(node, "node");
        checkNotNull(oldChild, "attributeName");
        Node removedNode = ((Node) node.getNode()).removeChild((Node) oldChild.getNode());
        return new DefaultXmlFieldNode(removedNode);
    }

    @Override
    public void removeNodes(final XmlFieldNodeList nodesToRemove) {
        checkNotNull(nodesToRemove, "nodesToRemove");
        for (int i = nodesToRemove.getLength() - 1; i >= 0; i--) {
            Node currentNode = (Node) nodesToRemove.item(i).getNode();
            currentNode.getParentNode().removeChild(currentNode);
        }
    }

    private Document getNodeDocument(final Node node) {

        checkNotNull(node, "node");

        for (Node n = node; n != null; n = n.getParentNode()) {

            if (Document.class.isInstance(n)) {

                return (Document) n;
            }
        }

        throw new IllegalArgumentException("Node has no Document ancestor.");
    }
}
