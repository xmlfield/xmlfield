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

import java.util.List;

import org.xmlfield.core.XmlFieldNode;
import org.xmlfield.core.XmlFieldNodeList;

/**
 * Default xml field node list implementation.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * 
 */
public class XmlFieldDomNodeList implements XmlFieldNodeList {

    private final List<XmlFieldNode<?>> nodeList;

    public XmlFieldDomNodeList(List<XmlFieldNode<?>> nodeList) {
        super();
        this.nodeList = nodeList;
    }

    @Override
    public int getLength() {
        return nodeList.size();
    }

    @Override
    public XmlFieldNode<?> item(int index) {
        if (index < nodeList.size()) {
            return nodeList.get(index);
        }
        return null;
    }

}
