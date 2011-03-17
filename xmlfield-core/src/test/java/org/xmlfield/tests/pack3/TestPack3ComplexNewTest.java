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
import static org.xmlfield.utils.XmlUtils.xmlFieldNodeToXml;
import static org.xmlfield.utils.XmlUtils.xmlToXmlFieldNode;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlfield.core.XmlFieldBinder;
import org.xmlfield.core.XmlFieldNode;

/**
 * Test getter and setter with array of native types but with complex xpath values (several nodes, attributes).
 * 
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
public class TestPack3ComplexNewTest {

    private final XmlFieldBinder binder = new XmlFieldBinder();

    Logger log = LoggerFactory.getLogger(TestPack3ComplexNewTest.class);

    @Test
    public void testSetNull() throws Exception {

        // Load initial XML
        final String xml = "<list><string>String1</string><string>String2</string><single>Single value</single></list>";
        String result = null;
        final XmlFieldNode<?> node = xmlToXmlFieldNode(xml);

        // Attach and assert object values
        final StringList list = binder.bind(node, StringList.class);
        assertEquals(2, list.getStrings().length);
        assertEquals("Single value", list.getSingle());

        // Remove tag 'single' by setting 'null' value
        list.setSingle(null);
        result = xmlFieldNodeToXml(node);
        log.info(result);
        assertEquals("<list><string>String1</string><string>String2</string></list>", result);

        // Remove all tags 'string' by setting 'null' value
        list.setStrings(null);
        result = xmlFieldNodeToXml(node);
        log.info(result);
        assertEquals("<list/>", result);
    }

    @Test
    public void testSetValueComplex() throws Exception {

        // Load initial XML
        final String xml = "<list></list>";
        String result = null;
        final XmlFieldNode<?> node = xmlToXmlFieldNode(xml);

        // Attach and assert object values
        final ComplexStringList list = binder.bind(node, ComplexStringList.class);
        assertEquals(0, list.getStrings().length);

        // Set new Value and assert
        list.setStrings(new String[] { "String1", "String2", "String3" });
        result = xmlFieldNodeToXml(node);
        log.info(result);
        assertEquals(
                "<list><parent1><parent2><stringlist type=\"complex\"><string>String1</string><string>String2</string><string>String3</string></stringlist></parent2></parent1></list>",
                result);

        // Set new Value and assert
        list.setStrings((String[]) ArrayUtils.remove(list.getStrings(), 1));
        result = xmlFieldNodeToXml(node);
        log.info(result);
        assertEquals(
                "<list><parent1><parent2><stringlist type=\"complex\"><string>String1</string><string>String3</string></stringlist></parent2></parent1></list>",
                result);

    }

}
