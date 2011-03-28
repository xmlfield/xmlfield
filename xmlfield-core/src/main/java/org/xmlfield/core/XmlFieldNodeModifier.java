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

import org.xmlfield.core.internal.XmlFieldUtils.NamespaceMap;

/**
 * Modifier interface, this interface describe the diffrent operation needed to be done on an XML document.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * 
 */
public interface XmlFieldNodeModifier {

    /**
     * Create an attribute to a node
     * 
     * @param contextNode
     *            node where the attribute will be created
     * @param attributeName
     *            name of the attribute
     * @param textContent
     *            text content of the attribute
     */
    void createAttribute(final XmlFieldNode<?> contextNode, final String attributeName, final String textContent);

    /**
     * Create a new element node at the end of the root node.
     * 
     * @param namespaces
     *            document namespaces
     * @param node
     *            context node
     * @param elementName
     *            element name
     * @return the created element
     */
    XmlFieldNode<?> createElement(final NamespaceMap namespaces, final XmlFieldNode<?> node, final String elementName);

    /**
     * Create a new element node at the end of the root node.
     * 
     * @param namespaces
     *            document namespaces
     * @param node
     *            context node
     * @param elementName
     *            element name
     * @param textContent
     *            text content of the new element
     * @return the new element
     */
    XmlFieldNode<?> createElement(final NamespaceMap namespaces, final XmlFieldNode<?> node, final String elementName,
            final String textContent);

    /**
     * Insert a node before another node.
     * 
     * @param parentNode
     *            parent node of the nodes
     * @param newChild
     *            node to insert
     * @param refChild
     *            reference node where the node should be inserted before
     * @return the inserted node
     */
    XmlFieldNode<?> insertBefore(final XmlFieldNode<?> parentNode, final XmlFieldNode<?> newChild,
            XmlFieldNode<?> refChild);

    /**
     * Remove an attribute from a specified node
     * 
     * @param node
     *            node
     * @param attributeName
     *            attribute name
     * @return the attribute node removed
     */
    XmlFieldNode<?> removeAttribute(final XmlFieldNode<?> node, final String attributeName);

    /**
     * Remove a child node
     * 
     * @param contextNode
     *            the context node
     * @param oldChild
     *            child to be removed
     * @return the removed node
     */
    XmlFieldNode<?> removeChild(final XmlFieldNode<?> contextNode, final XmlFieldNode<?> oldChild);

    /**
     * Remove a node list
     * 
     * @param contextNode
     *            the context node
     * @param nodesToRemove
     *            node list of children to be removed
     */
    void removeChildren(final XmlFieldNodeList nodesToRemove);

}
