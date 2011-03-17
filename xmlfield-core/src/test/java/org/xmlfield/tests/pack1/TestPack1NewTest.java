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
package org.xmlfield.tests.pack1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.xmlfield.utils.XmlUtils.xmlFieldNodeToXml;
import static org.xmlfield.utils.XmlUtils.xmlToXmlFieldNode;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlfield.core.XmlFieldBinder;
import org.xmlfield.core.XmlFieldNode;

/**
 * Test basic xmlfield usage, with a simple xml file (no namespace, no attribute).
 * 
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
public class TestPack1NewTest {

    private final XmlFieldBinder binder = new XmlFieldBinder();

    Logger log = LoggerFactory.getLogger(TestPack1NewTest.class);

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testAddAndRemoveCd() throws Exception {
        // Create initial XML
        final String xml = "<Catalog><Others/></Catalog>";
        final XmlFieldNode<?> node = xmlToXmlFieldNode(xml);

        // Get object and do initial asserts
        final Catalog catalog = binder.bind(node, Catalog.class);
        int numberOfCds = catalog.getCd().length;
        assertEquals(0, numberOfCds);

        // Add a Cd and assert
        Cd cd = catalog.addToCd();
        cd.setTitle("title");

        String result = xmlFieldNodeToXml(node);
        log.info(result);
        assertEquals("<Catalog><Others/><Cd><Title>title</Title></Cd></Catalog>", result);

        // Add an OtherCd (more complex xpath) and assert
        OtherCd oCd = catalog.addToOtherCd();
        oCd.setTitle("other title");
        oCd.setPrice(12);
        result = xmlFieldNodeToXml(node);
        log.info(result);
        assertEquals(
                "<Catalog><Others><Misc1><Misc2><Misc3><Cd><Titles><Title>other title</Title></Titles><Prices><France><Price>12.0</Price></France></Prices></Cd></Misc3></Misc2></Misc1></Others><Cd><Title>title</Title></Cd></Catalog>",
                result);

        // Remove Cd and assert
        catalog.removeFromCd(cd);
        result = xmlFieldNodeToXml(node);
        log.info(result);
        assertEquals(
                "<Catalog><Others><Misc1><Misc2><Misc3><Cd><Titles><Title>other title</Title></Titles><Prices><France><Price>12.0</Price></France></Prices></Cd></Misc3></Misc2></Misc1></Others></Catalog>",
                result);

    }

    /**
     * @throws Exception
     */
    @Test
    public void testEmpty() throws Exception {
        final String xml = "<Catalog/>";

        final XmlFieldNode<?> node = xmlToXmlFieldNode(xml);

        final Catalog catalog = binder.bind(node, Catalog.class);

        int numberOfCds = catalog.getCd().length;
        assertEquals(0, numberOfCds);
    }

    @Test
    public void testSetValue() throws Exception {

        // Load initial XML
        final String xml = "<Catalog><Cd><Title>toto</Title><Artist>Bob Dylan</Artist><Country>USA</Country><Company>Columbia</Company><Price>10.90</Price><Year>1985</Year></Cd><Cd><Title>toto</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country><Company>CBS Records</Company><Price>9.90</Price><Year>1988</Year></Cd></Catalog>";
        String result = null;
        final XmlFieldNode<?> node = xmlToXmlFieldNode(xml);

        // Attach
        final Catalog catalog = binder.bind(node, Catalog.class);
        int numberOfCds = catalog.getCd().length;
        assertEquals(2, numberOfCds);

        // Set new Value and assert
        catalog.setCd((Cd[]) ArrayUtils.remove(catalog.getCd(), 0));
        result = xmlFieldNodeToXml(node);
        log.info(result);
        assertEquals(
                "<Catalog><Cd><Title>toto</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country><Company>CBS Records</Company><Price>9.90</Price><Year>1988</Year></Cd></Catalog>",
                result);

        // Set new Value and assert
        catalog.setCd(null);
        result = xmlFieldNodeToXml(node);
        log.info(result);
        assertEquals("<Catalog/>", result);

    }

    /**
     * Check read and update abilities (get/set).
     * 
     * @throws Exception
     */
    @Test
    public void testSimple() throws Exception {

        final String xml = "<Catalog><Cd>	<Title>Empire Burlesque</Title>	<Artist>Bob Dylan</Artist>	<Country>USA</Country>	<Company>Columbia</Company>	<Price>10.90</Price>	<Year>1985</Year></Cd><Cd>	<Title>Hide your heart</Title>	<Artist>Bonnie Tyler</Artist>	<Country>UK</Country>	<Company>CBS Records</Company>	<Price>9.90</Price>	<Year>1988</Year></Cd><Cd>	<Title>Greatest Hits</Title>	<Artist>Dolly Parton</Artist>	<Country>USA</Country>	<Company>RCA</Company>	<Price>9.90</Price>	<Year>1982</Year></Cd><Cd>	<Title>Still got the blues</Title>	<Artist>Gary Moore</Artist>	<Country>UK</Country>	<Company>Virgin records</Company>	<Price>10.20</Price>	<Year>1990</Year></Cd><Cd>	<Title>Eros</Title>	<Artist>Eros Ramazzotti</Artist>	<Country>EU</Country>	<Company>BMG</Company>	<Price>9.90</Price>	<Year>1997</Year></Cd><Cd>	<Title>One night only</Title>	<Artist>Bee Gees</Artist>	<Country>UK</Country>	<Company>Polydor</Company>	<Price>10.90</Price>	<Year>1998</Year></Cd><Cd>	<Title>Sylvias Mother</Title>	<Artist>Dr.Hook</Artist>	<Country>UK</Country>	<Company>CBS</Company>	<Price>8.10</Price>	<Year>1973</Year></Cd><Cd>	<Title>Maggie May</Title>	<Artist>Rod Stewart</Artist>	<Country>UK</Country>	<Company>Pickwick</Company>	<Price>8.50</Price>	<Year>1990</Year></Cd><Cd>	<Title>Romanza</Title>	<Artist>Andrea Bocelli</Artist>	<Country>EU</Country>	<Company>Polydor</Company>	<Price>10.80</Price>	<Year>1996</Year></Cd><Cd>	<Title>When a man loves a woman</Title>	<Artist>Percy Sledge</Artist>	<Country>USA</Country>	<Company>Atlantic</Company>	<Price>8.70</Price>	<Year>1987</Year></Cd><Cd>	<Title>Black angel</Title>	<Artist>Savage Rose</Artist>	<Country>EU</Country>	<Company>Mega</Company>	<Price>10.90</Price>	<Year>1995</Year></Cd><Cd>	<Title>1999 Grammy Nominees</Title>	<Artist>Many</Artist>	<Country>USA</Country>	<Company>Grammy</Company>	<Price>10.20</Price>	<Year>1999</Year></Cd><Cd>	<Title>For the good times</Title>	<Artist>Kenny Rogers</Artist>	<Country>UK</Country>	<Company>Mucik Master</Company>	<Price>8.70</Price>	<Year>1995</Year></Cd><Cd>	<Title>Big Willie style</Title>	<Artist>Will Smith</Artist>	<Country>USA</Country>	<Company>Columbia</Company>	<Price>9.90</Price>	<Year>1997</Year></Cd><Cd>	<Title>Tupelo Honey</Title>	<Artist>Van Morrison</Artist>	<Country>UK</Country>	<Company>Polydor</Company>	<Price>8.20</Price>	<Year>1971</Year></Cd><Cd>	<Title>Soulsville</Title>	<Artist>Jorn Hoel</Artist>	<Country>Norway</Country>	<Company>WEA</Company>	<Price>7.90</Price>	<Year>1996</Year></Cd><Cd>	<Title>The very best of</Title>	<Artist>Cat Stevens</Artist>	<Country>UK</Country>	<Company>Island</Company>	<Price>8.90</Price>	<Year>1990</Year></Cd><Cd>	<Title>Stop</Title>	<Artist>Sam Brown</Artist>	<Country>UK</Country>	<Company>A and M</Company>	<Price>8.90</Price>	<Year>1988</Year></Cd><Cd>	<Title>Bridge of Spies</Title>	<Artist>T'Pau</Artist>	<Country>UK</Country>	<Company>Siren</Company>	<Price>7.90</Price>	<Year>1987</Year></Cd><Cd>	<Title>Private Dancer</Title>	<Artist>Tina Turner</Artist>	<Country>UK</Country>	<Company>Capitol</Company>	<Price>8.90</Price>	<Year>1983</Year></Cd><Cd>	<Title>Midt om natten</Title>	<Artist>Kim Larsen</Artist>	<Country>EU</Country>	<Company>Medley</Company>	<Price>7.80</Price>	<Year>1983</Year></Cd><Cd>	<Title>Pavarotti Gala Concert</Title>	<Artist>Luciano Pavarotti</Artist>	<Country>UK</Country>	<Company>DECCA</Company>	<Price>9.90</Price>	<Year>1991</Year></Cd><Cd>	<Title>The dock of the bay</Title>	<Artist>Otis Redding</Artist>	<Country>USA</Country>	<Company>Atlantic</Company>	<Price>7.90</Price>	<Year>1987</Year></Cd><Cd>	<Title>Picture book</Title>	<Artist>Simply Red</Artist>	<Country>EU</Country>	<Company>Elektra</Company>	<Price>7.20</Price>	<Year>1985</Year></Cd><Cd>	<Title>Red</Title>	<Artist>The Communards</Artist>	<Country>UK</Country>	<Company>London</Company>	<Price>7.80</Price>	<Year>1987</Year></Cd><Cd>	<Title>Unchain my heart</Title>	<Artist>Joe Cocker</Artist>	<Country>USA</Country>	<Company>EMI</Company>	<Price>8.20</Price>	<Year>1987</Year></Cd></Catalog>";

        final XmlFieldNode<?> node = xmlToXmlFieldNode(xml);

        final Catalog catalog = binder.bind(node, Catalog.class);

        int numberOfCds = catalog.getCd().length;
        assertEquals(26, numberOfCds);

        assertEquals(26, catalog.sizeOfCd());
        for (int i = 0; i < numberOfCds; i++) {
            Cd cd = catalog.getCd()[i];
            // log.info(cd.toString());
            assertNotNull(cd.getTitle());
            assertTrue(cd.getPrice() > 0);
            cd.setTitle("toto");
            assertEquals("toto", cd.getTitle());
        }

        String result = xmlFieldNodeToXml(node);
        log.info(result);
        assertEquals(
                "<Catalog><Cd>	<Title>toto</Title>	<Artist>Bob Dylan</Artist>	<Country>USA</Country>	<Company>Columbia</Company>	<Price>10.90</Price>	<Year>1985</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Bonnie Tyler</Artist>	<Country>UK</Country>	<Company>CBS Records</Company>	<Price>9.90</Price>	<Year>1988</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Dolly Parton</Artist>	<Country>USA</Country>	<Company>RCA</Company>	<Price>9.90</Price>	<Year>1982</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Gary Moore</Artist>	<Country>UK</Country>	<Company>Virgin records</Company>	<Price>10.20</Price>	<Year>1990</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Eros Ramazzotti</Artist>	<Country>EU</Country>	<Company>BMG</Company>	<Price>9.90</Price>	<Year>1997</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Bee Gees</Artist>	<Country>UK</Country>	<Company>Polydor</Company>	<Price>10.90</Price>	<Year>1998</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Dr.Hook</Artist>	<Country>UK</Country>	<Company>CBS</Company>	<Price>8.10</Price>	<Year>1973</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Rod Stewart</Artist>	<Country>UK</Country>	<Company>Pickwick</Company>	<Price>8.50</Price>	<Year>1990</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Andrea Bocelli</Artist>	<Country>EU</Country>	<Company>Polydor</Company>	<Price>10.80</Price>	<Year>1996</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Percy Sledge</Artist>	<Country>USA</Country>	<Company>Atlantic</Company>	<Price>8.70</Price>	<Year>1987</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Savage Rose</Artist>	<Country>EU</Country>	<Company>Mega</Company>	<Price>10.90</Price>	<Year>1995</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Many</Artist>	<Country>USA</Country>	<Company>Grammy</Company>	<Price>10.20</Price>	<Year>1999</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Kenny Rogers</Artist>	<Country>UK</Country>	<Company>Mucik Master</Company>	<Price>8.70</Price>	<Year>1995</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Will Smith</Artist>	<Country>USA</Country>	<Company>Columbia</Company>	<Price>9.90</Price>	<Year>1997</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Van Morrison</Artist>	<Country>UK</Country>	<Company>Polydor</Company>	<Price>8.20</Price>	<Year>1971</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Jorn Hoel</Artist>	<Country>Norway</Country>	<Company>WEA</Company>	<Price>7.90</Price>	<Year>1996</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Cat Stevens</Artist>	<Country>UK</Country>	<Company>Island</Company>	<Price>8.90</Price>	<Year>1990</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Sam Brown</Artist>	<Country>UK</Country>	<Company>A and M</Company>	<Price>8.90</Price>	<Year>1988</Year></Cd><Cd>	<Title>toto</Title>	<Artist>T'Pau</Artist>	<Country>UK</Country>	<Company>Siren</Company>	<Price>7.90</Price>	<Year>1987</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Tina Turner</Artist>	<Country>UK</Country>	<Company>Capitol</Company>	<Price>8.90</Price>	<Year>1983</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Kim Larsen</Artist>	<Country>EU</Country>	<Company>Medley</Company>	<Price>7.80</Price>	<Year>1983</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Luciano Pavarotti</Artist>	<Country>UK</Country>	<Company>DECCA</Company>	<Price>9.90</Price>	<Year>1991</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Otis Redding</Artist>	<Country>USA</Country>	<Company>Atlantic</Company>	<Price>7.90</Price>	<Year>1987</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Simply Red</Artist>	<Country>EU</Country>	<Company>Elektra</Company>	<Price>7.20</Price>	<Year>1985</Year></Cd><Cd>	<Title>toto</Title>	<Artist>The Communards</Artist>	<Country>UK</Country>	<Company>London</Company>	<Price>7.80</Price>	<Year>1987</Year></Cd><Cd>	<Title>toto</Title>	<Artist>Joe Cocker</Artist>	<Country>USA</Country>	<Company>EMI</Company>	<Price>8.20</Price>	<Year>1987</Year></Cd></Catalog>",
                result);

        // No assert because toString output is not clearly specified right now.
        // No one should rely on this for correct operation.
        log.info(catalog.toString());

    }
}
