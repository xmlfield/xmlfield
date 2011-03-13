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
package org.xmlfield.tests.pack7;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.xmlfield.utils.XmlUtils.xmlToNode;

import org.junit.Test;
import org.w3c.dom.Node;
import org.xmlfield.core.XmlFieldReader;
import org.xmlfield.utils.XmlUtils;

/**
 * @author Jean-Pierre Grillon
 */
public class TestPack7Test {

    private XmlFieldReader parser = new XmlFieldReader();

    @Test
    public void testNull() throws Exception {
        final String xml = "<Catalog></Catalog>";
        final Node node = xmlToNode(xml);

        final Catalog catalog = parser.attach(node, Catalog.class);
        assertNull(catalog.getCd());
    }

    @Test
    public void testSimple() throws Exception {
        String xml = "<Catalog><Cd /></Catalog>";
        final Node node = xmlToNode(xml);

        final Catalog catalog = parser.attach(node, Catalog.class);
        assertNotNull(catalog.getCd());
        assertEquals(null, catalog.getCd().getTitle());

        catalog.getCd().setTitle("Wolfmother");

        xml = XmlUtils.nodeToXml(catalog);
        assertEquals("<Catalog><Cd><Title>Wolfmother</Title></Cd></Catalog>", xml);
    }

    @Test
    public void testSet() throws Exception {
        String xml = "<Catalog></Catalog>";
        String title = "Space Oddity";
        Node node = xmlToNode(xml);

        final Catalog catalog = parser.attach(node, Catalog.class);

        Cd cd = catalog.newCd();
        cd.setTitle(title);

        assertNotNull(catalog.getCd());
        xml = XmlUtils.nodeToXml(catalog);
        assertEquals("<Catalog><Cd><Title>" + title + "</Title></Cd></Catalog>", xml);

        catalog.setCd(cd);
        assertEquals("<Catalog><Cd><Title>" + title + "</Title></Cd></Catalog>", xml);
    }

    @Test
    public void testSet2() throws Exception {
        String title = "Ça fait rire les oiseaux";
        String xml = "<Catalog><Cd><Title>" + title + "</Title></Cd></Catalog>";
        Node node = xmlToNode(xml);

        final Catalog catalog = parser.attach(node, Catalog.class);

        Cd cd = catalog.newCd();

        assertNotNull(catalog.getCd());
        assertEquals(cd.getTitle(), title);

    }

}
