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
package org.xmlfield.core.api;

/**
 * Interface used by the framework to access to an xml node.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * 
 * @param <T>
 *            underlying xml node representation type
 */
public interface XmlFieldNode<T> {

    /**
     * The node is an <code>Attr</code>.
     */
    static final short ATTRIBUTE_NODE = 2;

    /**
     * The node is an <code>Element</code>.
     */
    static final short ELEMENT_NODE = 1;
    /**
     * The node is a <code>Text</code> node.
     */
    static final short TEXT_NODE = 3;
    /**
     * The node is an unknow node.
     */
    static final short UNKNOW_NODE = -1;

    /**
     * Get the document node
     * 
     * @return document node
     */
    XmlFieldNode<T> getDocumentNode();

    /**
     * Get the underlying node
     * 
     * @return the underlying node
     */
    T getNode();

    /**
     * Get the xml node name of the underlying node
     * 
     * @return node name
     */
    String getNodeName();

    /**
     * Get the node type.
     * 
     * @return node type
     */
    short getNodeType();

    /**
     * Retrieve the parent node
     * 
     * @return parent node of this node
     */
    XmlFieldNode<T> getParentNode();

    /**
     * Get the node content as string.
     * 
     * @return node content
     */
    String getTextContent();

    /**
     * Check if the current node has attributes
     * 
     * @return true if the node has attributes
     */
    boolean hasAttributes();

    /**
     * Set the node text content
     * 
     * @param textContent
     */
    void setTextContent(String textContent);
}
