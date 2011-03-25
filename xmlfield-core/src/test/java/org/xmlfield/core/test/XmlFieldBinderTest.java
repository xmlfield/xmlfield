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
import static org.junit.Assert.assertTrue;
import static org.xmlfield.utils.XmlUtils.xmlFieldNodeToXml;

import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlfield.core.XmlFieldBinder;
import org.xmlfield.core.XmlFieldNode;
import org.xmlfield.core.exception.XmlFieldParsingException;
import org.xmlfield.core.internal.XmlFieldUtils;
import org.xmlfield.utils.XPathUtils;

/**
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 * @author Mabrouk Belhout
 */
public class XmlFieldBinderTest {

    Logger log = LoggerFactory.getLogger(XmlFieldBinderTest.class);

    @Test
    public void testAttachReadOnly() throws Exception {
        XmlFieldBinder binder = new XmlFieldBinder();
        Catalog catalog = binder.bindReadOnly(sampleXml1(), Catalog.class);
        assertNotNull(catalog);
    }

    /**
     * Check launched exceptions for buggy xml.
     */
    @Test(expected = XmlFieldParsingException.class)
    public void testAttachReadOnlyInvalidXml() throws Exception {
        XmlFieldBinder binder = new XmlFieldBinder();
        Catalog catalog = binder.bindReadOnly(sampleXmlBuggy(), Catalog.class);
        assertNotNull(catalog);
    }

    /**
     * Test null returned for non matching xml and type.
     */
    @Test
    public void testAttachReadOnlyWrongEntityXml() throws Exception {
        XmlFieldBinder binder = new XmlFieldBinder();
        Catalog catalog = binder.bindReadOnly(sampleXmlWrongEntity(), Catalog.class);
        assertNull(catalog);
    }

    @Test
    public void testGetNodeFromInterface() throws Exception {
        XmlFieldBinder binder = new XmlFieldBinder();
        Catalog catalog = binder.bindReadOnly(sampleXml1(), Catalog.class);

        assertNotNull(catalog);

        XmlFieldNode<?> node = XmlFieldUtils.getXmlFieldNode(catalog);
        String xml = xmlFieldNodeToXml(node);

        assertEquals(
                "<Catalog><Cd>  <Title>toto</Title>  <Artist>Bob Dylan</Artist><Country>USA</Country>  <Company>Columbia</Company>  <Price>10.90</Price><Year>1985</Year></Cd><Cd>   <Title>toto</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country>   <Company>CBS Records</Company>   <Price>9.90</Price><Year>1988</Year></Cd></Catalog>",
                xml);
    }

    @Test
    public void testInstantiate() throws Exception {
        XmlFieldBinder binder = new XmlFieldBinder();

        Catalog catalog = binder.instantiate(Catalog.class);
        assertNotNull(catalog);
        assertTrue(catalog.getCd().length == 0);

        assertEquals("<Catalog/>", xmlFieldNodeToXml(catalog));

        catalog.addToCd().setTitle("title");
        catalog.addToCd().setPrice(987);

        assertEquals("<Catalog><Cd><Title>title</Title></Cd><Cd><Price>987.0</Price></Cd></Catalog>",
                xmlFieldNodeToXml(catalog));
    }

    @Test
    public void testInstantiateWithNamespaces() throws Exception {
        XmlFieldBinder binder = new XmlFieldBinder();

        AtomCatalog catalog = binder.instantiate(AtomCatalog.class);
        assertNotNull(catalog);
        assertTrue(catalog.getCd().length == 0);

        assertEquals("<a:entry xmlns:a=\"http://www.w3.org/2005/Atom\" xmlns:x=\"http://www.w3.org/1999/xhtml\"/>",
                xmlFieldNodeToXml(catalog));

        catalog.addToCd().setTitle("title");
        catalog.addToCd().setPrice(987);

        assertEquals(
                "<a:entry xmlns:a=\"http://www.w3.org/2005/Atom\" xmlns:x=\"http://www.w3.org/1999/xhtml\">"
                        + "<content xmlns=\"http://www.w3.org/2005/Atom\"><div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"cd\"><span class=\"title\">title</span></div><div class=\"cd\"><span class=\"price\">987.0</span></div></div></content></a:entry>",
                xmlFieldNodeToXml(catalog));
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
}