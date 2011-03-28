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

import static org.xmlfield.utils.JaxpUtils.getXPath;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlfield.core.XmlFieldNode;
import org.xmlfield.core.XmlFieldNodeList;
import org.xmlfield.core.XmlFieldSelector;
import org.xmlfield.core.exception.XmlFieldXPathException;
import org.xmlfield.core.internal.XmlFieldUtils.NamespaceMap;

/**
 * Default xml field selector implementation. Use the jaxp implementation.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * 
 */
public class DefaultXmlFieldSelector implements XmlFieldSelector {

    @Override
    public Boolean selectXPathToBoolean(NamespaceMap namespaces, String xpath, XmlFieldNode<?> node)
            throws XmlFieldXPathException {
        checkXPathNotNull(xpath);
        final XPath xp = getXPath(namespaces);
        final Boolean value;
        try {
            value = (Boolean) xp.evaluate(xpath, node.getNode(), XPathConstants.BOOLEAN);
        } catch (XPathExpressionException e) {
            throw new XmlFieldXPathException(e);
        }
        return value;
    }

    @Override
    public XmlFieldNode<?> selectXPathToNode(NamespaceMap namespaces, String xpath, XmlFieldNode<?> node)
            throws XmlFieldXPathException {
        checkXPathNotNull(xpath);
        final XPath xp = getXPath(namespaces);
        final Node selectedNode;
        try {
            selectedNode = (Node) xp.evaluate(xpath, node.getNode(), XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new XmlFieldXPathException(e);
        }
        if (selectedNode == null) {
            return null;
        }
        return new DefaultXmlFieldNode(selectedNode);
    }

    @Override
    public XmlFieldNodeList selectXPathToNodeList(NamespaceMap namespaces, String xpath, XmlFieldNode<?> node)
            throws XmlFieldXPathException {
        checkXPathNotNull(xpath);
        final XPath xp = getXPath(namespaces);

        final NodeList nodeList;
        try {
            nodeList = (NodeList) xp.evaluate(xpath, node.getNode(), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new XmlFieldXPathException(e);
        }

        final int nodeCount = nodeList.getLength();

        final List<XmlFieldNode<?>> list = new ArrayList<XmlFieldNode<?>>();

        for (int i = 0; i < nodeCount; ++i) {

            final XmlFieldNode<Node> subNode = new DefaultXmlFieldNode(nodeList.item(i));

            list.add(subNode);
        }
        return new DefaultXmlFieldNodeList(list);
    }

    @Override
    public Double selectXPathToNumber(NamespaceMap namespaces, String xpath, XmlFieldNode<?> node)
            throws XmlFieldXPathException {
        checkXPathNotNull(xpath);
        final XPath xp = getXPath(namespaces);
        final Double value;
        try {
            value = (Double) xp.evaluate(xpath, node.getNode(), XPathConstants.NUMBER);
        } catch (XPathExpressionException e) {
            throw new XmlFieldXPathException(e);
        }
        return value;
    }

    @Override
    public String selectXPathToString(NamespaceMap namespaces, String xpath, XmlFieldNode<?> node)
            throws XmlFieldXPathException {
        checkXPathNotNull(xpath);
        final XPath xp = getXPath(namespaces);
        final String value;
        try {
            value = (String) xp.evaluate(xpath, node.getNode(), XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new XmlFieldXPathException(e);
        }
        return value;
    }

    private void checkXPathNotNull(String xpath) throws XmlFieldXPathException {
        if (xpath == null) {
            throw new XmlFieldXPathException("The requested xpath is null");
        }
    }

}
