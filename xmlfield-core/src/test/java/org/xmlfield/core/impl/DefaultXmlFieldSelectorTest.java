package org.xmlfield.core.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xmlfield.core.XmlFieldNode;
import org.xmlfield.core.XmlFieldNodeList;
import org.xmlfield.core.XmlFieldNodeParser;
import org.xmlfield.core.XmlFieldSelector;
import org.xmlfield.core.exception.XmlFieldXPathException;


public class DefaultXmlFieldSelectorTest {

    private XmlFieldNode<Node> node;

    private final XmlFieldNodeParser<Node> parser = new DefaultXmlFieldNodeParser();

    private final XmlFieldSelector selector = new DefaultXmlFieldJaxenSelector();

    private final String xml = "<Catalog><Cd> <Title>Empire Burlesque</Title> <Artist>Bob Dylan</Artist>  <Country>USA</Country>  <Company>Columbia</Company> <Price>10.90</Price>    <Year>1985</Year></Cd><Cd>  <Title>Hide your heart</Title>  <Artist>Bonnie Tyler</Artist>   <Country>UK</Country>   <Company>CBS Records</Company>  <Price>9.90</Price> <Year>1988</Year></Cd><Cd>  <Title>Greatest Hits</Title>    <Artist>Dolly Parton</Artist>   <Country>USA</Country>  <Company>RCA</Company>  <Price>9.90</Price> <Year>1982</Year></Cd><Cd>  <Title>Still got the blues</Title>  <Artist>Gary Moore</Artist> <Country>UK</Country>   <Company>Virgin records</Company>   <Price>10.20</Price>    <Year>1990</Year></Cd><Cd>  <Title>Eros</Title> <Artist>Eros Ramazzotti</Artist>    <Country>EU</Country>   <Company>BMG</Company>  <Price>9.90</Price> <Year>1997</Year></Cd><Cd>  <Title>One night only</Title>   <Artist>Bee Gees</Artist>   <Country>UK</Country>   <Company>Polydor</Company>  <Price>10.90</Price>    <Year>1998</Year></Cd><Cd>  <Title>Sylvias Mother</Title>   <Artist>Dr.Hook</Artist>    <Country>UK</Country>   <Company>CBS</Company>  <Price>8.10</Price> <Year>1973</Year></Cd><Cd>  <Title>Maggie May</Title>   <Artist>Rod Stewart</Artist>    <Country>UK</Country>   <Company>Pickwick</Company> <Price>8.50</Price> <Year>1990</Year></Cd><Cd>  <Title>Romanza</Title>  <Artist>Andrea Bocelli</Artist> <Country>EU</Country>   <Company>Polydor</Company>  <Price>10.80</Price>    <Year>1996</Year></Cd><Cd>  <Title>When a man loves a woman</Title> <Artist>Percy Sledge</Artist>   <Country>USA</Country>  <Company>Atlantic</Company> <Price>8.70</Price> <Year>1987</Year></Cd><Cd>  <Title>Black angel</Title>  <Artist>Savage Rose</Artist>    <Country>EU</Country>   <Company>Mega</Company> <Price>10.90</Price>    <Year>1995</Year></Cd><Cd>  <Title>1999 Grammy Nominees</Title> <Artist>Many</Artist>   <Country>USA</Country>  <Company>Grammy</Company>   <Price>10.20</Price>    <Year>1999</Year></Cd><Cd>  <Title>For the good times</Title>   <Artist>Kenny Rogers</Artist>   <Country>UK</Country>   <Company>Mucik Master</Company> <Price>8.70</Price> <Year>1995</Year></Cd><Cd>  <Title>Big Willie style</Title> <Artist>Will Smith</Artist> <Country>USA</Country>  <Company>Columbia</Company> <Price>9.90</Price> <Year>1997</Year></Cd><Cd>  <Title>Tupelo Honey</Title> <Artist>Van Morrison</Artist>   <Country>UK</Country>   <Company>Polydor</Company>  <Price>8.20</Price> <Year>1971</Year></Cd><Cd>  <Title>Soulsville</Title>   <Artist>Jorn Hoel</Artist>  <Country>Norway</Country>   <Company>WEA</Company>  <Price>7.90</Price> <Year>1996</Year></Cd><Cd>  <Title>The very best of</Title> <Artist>Cat Stevens</Artist>    <Country>UK</Country>   <Company>Island</Company>   <Price>8.90</Price> <Year>1990</Year></Cd><Cd>  <Title>Stop</Title> <Artist>Sam Brown</Artist>  <Country>UK</Country>   <Company>A and M</Company>  <Price>8.90</Price> <Year>1988</Year></Cd><Cd>  <Title>Bridge of Spies</Title>  <Artist>T'Pau</Artist>  <Country>UK</Country>   <Company>Siren</Company>    <Price>7.90</Price> <Year>1987</Year></Cd><Cd>  <Title>Private Dancer</Title>   <Artist>Tina Turner</Artist>    <Country>UK</Country>   <Company>Capitol</Company>  <Price>8.90</Price> <Year>1983</Year></Cd><Cd>  <Title>Midt om natten</Title>   <Artist>Kim Larsen</Artist> <Country>EU</Country>   <Company>Medley</Company>   <Price>7.80</Price> <Year>1983</Year></Cd><Cd>  <Title>Pavarotti Gala Concert</Title>   <Artist>Luciano Pavarotti</Artist>  <Country>UK</Country>   <Company>DECCA</Company>    <Price>9.90</Price> <Year>1991</Year></Cd><Cd>  <Title>The dock of the bay</Title>  <Artist>Otis Redding</Artist>   <Country>USA</Country>  <Company>Atlantic</Company> <Price>7.90</Price> <Year>1987</Year></Cd><Cd>  <Title>Picture book</Title> <Artist>Simply Red</Artist> <Country>EU</Country>   <Company>Elektra</Company>  <Price>7.20</Price> <Year>1985</Year></Cd><Cd>  <Title>Red</Title>  <Artist>The Communards</Artist> <Country>UK</Country>   <Company>London</Company>   <Price>7.80</Price> <Year>1987</Year></Cd><Cd>  <Title>Unchain my heart</Title> <Artist>Joe Cocker</Artist> <Country>USA</Country>  <Company>EMI</Company>  <Price>8.20</Price> <Year>1987</Year></Cd></Catalog>";

    @Before
    public void setUp() throws Exception {
        node = parser.xmlToNode(xml);
    }

    @Test
    public void testSelectXPathToBoolean() throws Exception {
        String xpath = "/Catalog/Cd/Title[text()='Greatest Hitss']";
        assertThat(selector.selectXPathToBoolean(null, xpath, node), is(false));

        xpath = "/Catalog/Cd/Title[text()='Greatest Hits']";
        assertThat(selector.selectXPathToBoolean(null, xpath, node), is(true));

        xpath = "";
        try {
            selector.selectXPathToBoolean(null, xpath, node);
            fail("No exception thrown");
        } catch (Exception e) {
            assertThat(e.getClass().getName(), is(XmlFieldXPathException.class.getName()));
        }

        xpath = null;
        try {
            selector.selectXPathToBoolean(null, xpath, node);
            fail("No exception thrown");
        } catch (Exception e) {
            assertThat(e.getClass().getName(), is(XmlFieldXPathException.class.getName()));
        }
    }

    @Test
    public void testSelectXPathToNode() throws Exception {
        String xpath = "/Catalog/Cd/Title[text()='Greatest Hitss']";
        assertThat(selector.selectXPathToNode(null, xpath, node), nullValue());

        xpath = "/Catalog/Cd[1]";
        String result = "<Cd> <Title>Empire Burlesque</Title> <Artist>Bob Dylan</Artist>  <Country>USA</Country>  <Company>Columbia</Company> <Price>10.90</Price>    <Year>1985</Year></Cd>";
        assertThat(parser.nodeToXml(selector.selectXPathToNode(null, xpath, node)), is(result));

        xpath = "/Catalog/Cd[Title/text()='Greatest Hits']";
        result = "<Cd>  <Title>Greatest Hits</Title>    <Artist>Dolly Parton</Artist>   <Country>USA</Country>  <Company>RCA</Company>  <Price>9.90</Price> <Year>1982</Year></Cd>";
        assertThat(parser.nodeToXml(selector.selectXPathToNode(null, xpath, node)), is(result));

        xpath = "";
        try {
            selector.selectXPathToNode(null, xpath, node);
            fail("No exception thrown");
        } catch (Exception e) {
            assertThat(e.getClass().getName(), is(XmlFieldXPathException.class.getName()));
        }

        xpath = null;
        try {
            selector.selectXPathToNode(null, xpath, node);
            fail("No exception thrown");
        } catch (Exception e) {
            assertThat(e.getClass().getName(), is(XmlFieldXPathException.class.getName()));
        }
    }

    @Test
    public void testSelectXPathToNodeList() throws Exception {
        String xpath = "/Catalog/Cd/Title[text()='Greatest Hitss']";
        assertThat(selector.selectXPathToNodeList(null, xpath, node).getLength(), is(0));

        xpath = "/Catalog/Cd";
        String result = "<Cd> <Title>Empire Burlesque</Title> <Artist>Bob Dylan</Artist>  <Country>USA</Country>  <Company>Columbia</Company> <Price>10.90</Price>    <Year>1985</Year></Cd>";
        XmlFieldNodeList results = selector.selectXPathToNodeList(null, xpath, node);
        assertThat(results.getLength(), is(26));
        assertThat(parser.nodeToXml(results.item(0)), is(result));

        xpath = "/Catalog/Cd[Country/text()='UK']";
        result = "<Cd>  <Title>Hide your heart</Title>  <Artist>Bonnie Tyler</Artist>   <Country>UK</Country>   <Company>CBS Records</Company>  <Price>9.90</Price> <Year>1988</Year></Cd>";
        results = selector.selectXPathToNodeList(null, xpath, node);
        assertThat(results.getLength(), is(13));
        assertThat(parser.nodeToXml(results.item(0)), is(result));

        xpath = "";
        try {
            selector.selectXPathToNodeList(null, xpath, node);
            fail("No exception thrown");
        } catch (Exception e) {
            assertThat(e.getClass().getName(), is(XmlFieldXPathException.class.getName()));
        }

        xpath = null;
        try {
            selector.selectXPathToNodeList(null, xpath, node);
            fail("No exception thrown");
        } catch (Exception e) {
            assertThat(e.getClass().getName(), is(XmlFieldXPathException.class.getName()));
        }
    }

    @Test
    public void testSelectXPathToNumber() throws Exception {
        String xpath = "/Catalog/Cd/Title[text()='Greatest Hitss']";
        assertThat(selector.selectXPathToNumber(null, xpath, node), is(Double.NaN));

        xpath = "/Catalog/Cd[1]/Price";
        assertThat(selector.selectXPathToNumber(null, xpath, node), is(new Double(10.90)));

        xpath = "/Catalog/Cd[Country/text()='UK']";
        assertThat(selector.selectXPathToNumber(null, xpath, node), is(Double.NaN));

        xpath = "";
        try {
            selector.selectXPathToNumber(null, xpath, node);
            fail("No exception thrown");
        } catch (Exception e) {
            assertThat(e.getClass().getName(), is(XmlFieldXPathException.class.getName()));
        }

        xpath = null;
        try {
            selector.selectXPathToNumber(null, xpath, node);
            fail("No exception thrown");
        } catch (Exception e) {
            assertThat(e.getClass().getName(), is(XmlFieldXPathException.class.getName()));
        }
    }

    @Test
    public void testSelectXPathToString() throws Exception {
        String xpath = "/Catalog/Cd/Title[text()='Greatest Hitss']";
        assertThat(selector.selectXPathToString(null, xpath, node), is(""));

        xpath = "/Catalog/Cd[1]/Price";
        assertThat(selector.selectXPathToString(null, xpath, node), is("10.90"));

        xpath = "/Catalog/Cd[Country/text()='UK'][1]/Title";
        assertThat(selector.selectXPathToString(null, xpath, node), is("Hide your heart"));

        xpath = "";
        try {
            selector.selectXPathToString(null, xpath, node);
            fail("No exception thrown");
        } catch (Exception e) {
            assertThat(e.getClass().getName(), is(XmlFieldXPathException.class.getName()));
        }

        xpath = null;
        try {
            selector.selectXPathToString(null, xpath, node);
            fail("No exception thrown");
        } catch (Exception e) {
            assertThat(e.getClass().getName(), is(XmlFieldXPathException.class.getName()));
        }
    }

}
