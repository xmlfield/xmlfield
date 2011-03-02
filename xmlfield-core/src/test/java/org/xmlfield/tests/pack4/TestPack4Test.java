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
package org.xmlfield.tests.pack4;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.xmlfield.utils.XmlUtils.nodeToXml;
import static org.xmlfield.utils.XmlUtils.xmlToNode;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xmlfield.core.XmlString;
import org.xmlfield.core.XmlFieldReader;

/**
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
public class TestPack4Test {

    Logger log = LoggerFactory.getLogger(TestPack4Test.class);

    private XmlFieldReader parser = new XmlFieldReader();

    @Test
    public void testSetValue() throws Exception {

        // Load initial XML
        final String xml = "<list><string>String1</string><string>String2</string></list>";
        String result = null;
        final Node node = xmlToNode(xml);

        // Attach and assert object values
        final StringList list = parser.attach(node, StringList.class);
        assertEquals(2, list.getStrings().length);

        XmlString[] tempList = list.getStrings();

        // Set new Value and assert
        XmlString s = list.addToStrings();
        result = nodeToXml(node);
        assertEquals(
                "<list><string>String1</string><string>String2</string><string/></list>",
                result);
        s.setString("String3");
        result = nodeToXml(node);
        assertEquals(
                "<list><string>String1</string><string>String2</string><string>String3</string></list>",
                result);

        list.setStrings((XmlString[]) ArrayUtils.add(tempList, s));
        result = nodeToXml(node);
        log.info(result);
        assertEquals(
                "<list><string>String1</string><string>String2</string><string>String3</string></list>",
                result);

        // Set new Value and assert
        list.setStrings((XmlString[]) ArrayUtils.remove(list.getStrings(), 1));
        result = nodeToXml(node);
        log.info(result);
        assertEquals(
                "<list><string>String1</string><string>String3</string></list>",
                result);

    }

    @Test
    public void testSetNull() throws Exception {

        // Load initial XML
        final String xml = "<list><string>String1</string><string>String2</string></list>";
        String result = null;
        final Node node = xmlToNode(xml);

        // Attach and assert object values
        final StringList list = parser.attach(node, StringList.class);
        assertEquals(2, list.getStrings().length);

        // Remove all tags 'string' by setting 'null' value
        list.setStrings(null);
        result = nodeToXml(node);
        log.info(result);
        assertEquals("<list/>", result);
    }

    @Test
    public void testAlterDocument() throws Exception {
        // Load initial XML
        // XML Document contains values which are not mapped to object and
        // should not be lost during the test
        final String xml = "<list><string>String1</string><string selected=\"true\">String2</string></list>";
        String result = null;
        final Node node = xmlToNode(xml);

        // Set new Value and assert
        final StringList list = parser.attach(node, StringList.class);
        list.setStrings((XmlString[]) ArrayUtils.remove(list.getStrings(), 0));
        result = nodeToXml(node);
        log.info(result);
        assertEquals("<list><string selected=\"true\">String2</string></list>",
                result);

    }
    
    @Test
	public void testAddItemList() throws Exception {
    	 // Load initial XML
        final String xml = "<list><string>String1</string><string>String2</string></list>";
        final Node node = xmlToNode(xml);

        // Attach and assert object values
        final StringList list = parser.attach(node, StringList.class);
        assertEquals(2, list.getStrings().length);
        
        XmlString s = list.addToStrings();
        s.setString("String3");
        assertEquals(3, list.getStrings().length);

        String result = nodeToXml(node);
        assertEquals("<list><string>String1</string><string>String2</string><string>String3</string></list>",result);
        
	}
}
