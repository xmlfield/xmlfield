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

import org.w3c.dom.Node;
import org.xmlfield.core.XmlFieldNode;

/**
 * Default xml field node implementation
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * 
 */
public class DomNode implements XmlFieldNode<Node> {

    private Node node;

    public DomNode(Node node) {
        super();
        this.node = node;
    }

    @Override
    public XmlFieldNode<Node> getDocumentNode() {
        if (this.node != null) {
            return new DomNode(this.node.getOwnerDocument().getDocumentElement());
        }
        return null;
    }

    @Override
    public Node getNode() {
        return this.node;
    }

    @Override
    public String getNodeName() {
        if (this.node == null) {
            return null;
        }
        return this.node.getNodeName();
    }

    @Override
    public short getNodeType() {
        if (this.node == null) {
            return XmlFieldNode.UNKNOW_NODE;
        }
        short nodeW3CType = this.node.getNodeType();
        switch (nodeW3CType) {
        case Node.ATTRIBUTE_NODE:
            return XmlFieldNode.ATTRIBUTE_NODE;

        case Node.ELEMENT_NODE:
            return XmlFieldNode.ELEMENT_NODE;

        case Node.TEXT_NODE:
            return XmlFieldNode.TEXT_NODE;

        default:
            return XmlFieldNode.UNKNOW_NODE;
        }
    }

    @Override
    public XmlFieldNode<Node> getParentNode() {
        if (this.node == null) {
            return null;
        }
        return new DomNode(this.node.getParentNode());
    }

    @Override
    public String getTextContent() {
        if (this.node == null) {
            return null;
        }
        return this.node.getTextContent();
    }

    @Override
    public boolean hasAttributes() {
        if (this.node == null) {
            return false;
        }
        return this.node.hasAttributes();
    }

    public void setNode(Node node) {
        this.node = node;
    }

    @Override
    public void setTextContent(String textContent) {
        if (this.node != null) {
            this.node.setTextContent(textContent);
        }
    }

}
