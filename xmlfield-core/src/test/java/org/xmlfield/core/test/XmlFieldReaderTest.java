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
package org.xmlfield.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;
import org.xmlfield.core.XmlFieldReader;
import org.xmlfield.tests.pack1.Catalog;
import org.xmlfield.utils.XPathUtils;
import org.xmlfield.utils.XmlUtils;

/**
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 * @author Mabrouk Belhout
 */
public class XmlFieldReaderTest {

    Logger log = LoggerFactory.getLogger(XmlFieldReaderTest.class);

    private String sampleXml1() {
        return "<Catalog>" //
                + "<Cd>"
                + "  <Title>toto</Title>"
                + "  <Artist>Bob Dylan</Artist><Country>USA</Country>"
                + "  <Company>Columbia</Company>" //
                + "  <Price>10.90</Price><Year>1985</Year>"
                + "</Cd>"
                + "<Cd>"
                + "   <Title>toto</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country>"
                + "   <Company>CBS Records</Company>" //
                + "   <Price>9.90</Price><Year>1988</Year>" //
                + "</Cd>" + //
                "</Catalog>";

    }

    private String sampleXmlBuggy() {
        return "<CatalogInvalid>" //
                + "<Cd>"
                + "  <Title>toto</Title>"
                + "  <Artist>Bob Dylan</Artist><Country>USA</Country>"
                + "  <Company>Columbia</Company>" //
                + "  <Price>10.90</Price><Year>1985</Year>"
                + "</Cd>"
                + "<Cd>"
                + "   <Title>toto</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country>"
                + "   <Company>CBS Records</Company>" //
                + "   <Price>9.90</Price><Year>1988</Year>" //
                + "</Cd>" + //
                "</Catalog>";
    }

    private String sampleXmlWrongEntity() {
        return "<Invalid>" //
                + "<Cd>"
                + "  <Title>toto</Title>"
                + "  <Artist>Bob Dylan</Artist><Country>USA</Country>"
                + "  <Company>Columbia</Company>" //
                + "  <Price>10.90</Price><Year>1985</Year>"
                + "</Cd>"
                + "<Cd>"
                + "   <Title>toto</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country>"
                + "   <Company>CBS Records</Company>" //
                + "   <Price>9.90</Price><Year>1988</Year>" //
                + "</Cd>" + //
                "</Invalid>";
    }

    @Test
    public void testAttachReadOnly() throws Exception {
        XmlFieldReader reader = new XmlFieldReader();
        Catalog catalog = reader.attachReadOnly(sampleXml1(), Catalog.class);
        assertNotNull(catalog);
    }

    /**
     * Check launched exceptions for buggy xml.
     */
    @Test(expected = SAXParseException.class)
    public void testAttachReadOnlyInvalidXml() throws Exception {
        XmlFieldReader reader = new XmlFieldReader();
        Catalog catalog = reader.attachReadOnly(sampleXmlBuggy(), Catalog.class);
        assertNotNull(catalog);
    }

    /**
     * Test null returned for non matching xml and type.
     */
    @Test
    public void testAttachReadOnlyWrongEntityXml() throws Exception {
        XmlFieldReader reader = new XmlFieldReader();
        Catalog catalog = reader.attachReadOnly(sampleXmlWrongEntity(), Catalog.class);
        assertNull(catalog);
    }

    @Test
    public void testGetNodeFromInterface() throws Exception {
        XmlFieldReader reader = new XmlFieldReader();
        Catalog catalog = reader.attachReadOnly(sampleXml1(), Catalog.class);

        assertNotNull(catalog);

        Node node = XmlUtils.getNode(catalog);
        String xml = XmlUtils.nodeToXml(node);

    }

    @Test
    public void testSimple() throws Exception {
        Map<String, String> map = XPathUtils.getElementSelectorAttributes("/cd/div[@class=\"title\"]");

        assertEquals("title", map.get("class"));
        assertEquals(1, map.size());

        map = XPathUtils.getElementSelectorAttributes("/cd/div[@class=\"title\"][@id=\'1\']");
        assertEquals("title", map.get("class"));
        assertEquals("1", map.get("id"));
        assertEquals(2, map.size());

    }
}
