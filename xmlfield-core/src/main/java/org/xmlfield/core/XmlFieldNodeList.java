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

/**
 * XmlFieldNodeList interface
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * 
 */
public interface XmlFieldNodeList {

    /**
     * Get the node list length.
     * 
     * @return node list length
     */
    public int getLength();

    /**
     * Retrieve the item positioned at the specified index.
     * 
     * @param index
     *            index
     * @return node
     */
    public XmlFieldNode<?> item(int index);
}
