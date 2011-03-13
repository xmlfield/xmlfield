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
package org.xmlfield.tests.pack8;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xmlfield.core.XmlFieldReader;
import org.xmlfield.utils.XmlUtils;

/**
 * @author Mabrouk Belhout
 */
public class TestPack8Test {

    private XmlFieldReader parser = new XmlFieldReader();

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

    @Test
    public void testChangeInterface() throws Exception {
        Catalog catalog = parser.attachReadOnly(sampleXml1(), Catalog.class);

        // Cd cd = catalog.addToCds(); // compile error

        assertFalse(catalog instanceof ExtendedCatalog);
        assertNotNull(catalog.getCds());
        assertEquals(2, catalog.getCds().length);

        String xml = XmlUtils.nodeToXml(catalog);
        assertTrue(xml.contains("<Artist>Bob Dylan</Artist>"));

        ExtendedCatalog extended = parser.reattach(catalog, ExtendedCatalog.class);
        assertTrue(extended instanceof Catalog);
        extended.addToCds().setArtist("The Prince");
        assertEquals(3, extended.getCds().length);
        assertEquals("The Prince", extended.getCds()[2].getArtist());
    }

}
