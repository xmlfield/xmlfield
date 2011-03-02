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
package org.xmlfield.tests.pack3;

import static org.junit.Assert.assertEquals;
import static org.xmlfield.utils.XmlUtils.nodeToXml;
import static org.xmlfield.utils.XmlUtils.xmlToNode;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xmlfield.core.XmlFieldReader;

/**
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
public class TestPack3Test {

    Logger log = LoggerFactory.getLogger(TestPack3Test.class);

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

        // Set new Value and assert
        list.setStrings((String[]) ArrayUtils.add(list.getStrings(), "String3"));
        result = nodeToXml(node);
        log.info(result);
        assertEquals(
                "<list><string>String1</string><string>String2</string><string>String3</string></list>",
                result);

        // Set new Value and assert
        list.setStrings((String[]) ArrayUtils.remove(list.getStrings(), 1));
        result = nodeToXml(node);
        log.info(result);
        assertEquals(
                "<list><string>String1</string><string>String3</string></list>",
                result);

    }

    @Test
    public void testSetNull() throws Exception {

        // Load initial XML
        final String xml = "<list><string>String1</string><string>String2</string><single>Single value</single></list>";
        String result = null;
        final Node node = xmlToNode(xml);

        // Attach and assert object values
        final StringList list = parser.attach(node, StringList.class);
        assertEquals(2, list.getStrings().length);
        assertEquals("Single value", list.getSingle());

        // Remove tag 'single' by setting 'null' value
        list.setSingle(null);
        result = nodeToXml(node);
        log.info(result);
        assertEquals(
                "<list><string>String1</string><string>String2</string></list>",
                result);

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

        // Due to current implementation and design, attributes are LOST during
        // this set. When used with primitive types.
        // This has yet to be fixed.
        // Until resolution, the buggy behavior is the documented one.
        final String xml = "<list><string>String1</string><string selected=\"true\">String2</string></list>";
        String result = null;
        final Node node = xmlToNode(xml);

        // Set new Value and assert
        final StringList list = parser.attach(node, StringList.class);
        list.setStrings((String[]) ArrayUtils.remove(list.getStrings(), 0));
        result = nodeToXml(node);
        log.info(result);
        assertEquals("<list><string>String2</string></list>", result);

        // We should have expected this
        // assertEquals("<list><string selected=\"true\">String2</string></list>",
        // result);

    }
}
