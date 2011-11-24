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
package org.xmlfield.tests.pack5;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlfield.core.XmlField;
import org.xmlfield.core.internal.XmlFieldNode;

/**
 * Test get/set xmlfield abilities on an attribute xpath expression type.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 */
public class TestPack5NewTest {

    private final XmlField binder = new XmlField();

    Logger log = LoggerFactory.getLogger(TestPack5NewTest.class);

    /**
     * Test that xml attribute is deleted on the underlying xml element when you set a null value with a method mapped
     * with an attribute xpath expression.
     * 
     * @throws Exception
     */
    @Test
    public void testSetNull() throws Exception {

        // Load initial XML
        final String xml = "<person><name first=\"John\" last=\"Abbitbol\"/><age value=\"52\"/><children age=\"10\" firstName=\"Paul\" lastName=\"Abbitbol\"/><children age=\"5\" firstName=\"Mia\" lastName=\"Abbitbol\"/></person>";
        String result = null;
        final XmlFieldNode<?> node = binder.xmlToNode(xml);

        // Attach and assert object values
        final Person person = binder.nodeToObject(node, Person.class);
        assertEquals("John", person.getFirstName());
        assertEquals("John", person.getFirstName());
        assertEquals("Abbitbol", person.getLastName());

        // Remove all values by setting 'null' value
        person.setAge(0);
        person.setFirstName(null);
        person.setLastName(null);
        person.getChildrens()[0].setAge(0);
        person.getChildrens()[0].setFirstName(null);
        person.getChildrens()[0].setLastName(null);

        result = binder.nodeToXml(node);
        log.info(result);
        assertEquals(
                "<person><name/><age value=\"0\"/><children age=\"0\"/><children age=\"5\" firstName=\"Mia\" lastName=\"Abbitbol\"/></person>",
                result);
    }

    /**
     * Test the ability to modify a value in an xml attribute.
     * 
     * @throws Exception
     *             errors
     */
    @Test
    public void testSetValue() throws Exception {

        // Load initial XML
        final String xml = "<person><name first=\"John\" last=\"Abbitbol\"/><age value=\"52\"/><children age=\"10\" firstName=\"Paul\" lastName=\"Abbitbol\"/><children age=\"5\" firstName=\"Mia\" lastName=\"Abbitbol\"/></person>";
        String result = null;
        final XmlFieldNode<?> node = binder.xmlToNode(xml);

        // Attach and assert object values
        final Person person = binder.nodeToObject(node, Person.class);
        assertEquals(52, person.getAge());
        assertEquals("John", person.getFirstName());
        assertEquals("Abbitbol", person.getLastName());
        assertEquals(2, person.getChildrens().length);
        assertEquals(10, person.getChildrens()[0].getAge());
        assertEquals("Abbitbol", person.getChildrens()[0].getLastName());
        assertEquals("Paul", person.getChildrens()[0].getFirstName());
        assertEquals(5, person.getChildrens()[1].getAge());
        assertEquals("Abbitbol", person.getChildrens()[1].getLastName());
        assertEquals("Mia", person.getChildrens()[1].getFirstName());

        // Set new Value and assert
        person.setAge(25);
        person.setFirstName("Freddy");
        person.setLastName("Kruger");
        person.getChildrens()[0].setAge(50);
        person.getChildrens()[0].setFirstName("Horror");
        person.getChildrens()[0].setLastName("Kruger");
        person.getChildrens()[1].setAge(40);
        person.getChildrens()[1].setFirstName("Diane");
        person.getChildrens()[1].setLastName("Kruger");
        result = binder.nodeToXml(node);
        assertEquals(
                "<person><name first=\"Freddy\" last=\"Kruger\"/><age value=\"25\"/><children age=\"50\" firstName=\"Horror\" lastName=\"Kruger\"/><children age=\"40\" firstName=\"Diane\" lastName=\"Kruger\"/></person>",
                result);
    }
}
