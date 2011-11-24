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

import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;
import org.xmlfield.core.XmlField;
import org.xmlfield.core.exception.XmlFieldParsingException;
import org.xmlfield.core.internal.XmlFieldNode;
import org.xmlfield.core.internal.impl.XPathUtils;
 
/**
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 * @author Mabrouk Belhout
 */
public class XmlFieldReaderTest {

	Logger log = LoggerFactory.getLogger(XmlFieldReaderTest.class);

	private String sampleXml1() {
		return "<Catalog>" //
				+ "<Cd>"
				+ "  <id>64546</id>"
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
	public void testxmlToObject() throws Exception {
		XmlField reader = new XmlField();
		Catalog catalog = reader.xmlToObject(sampleXml1(), Catalog.class);
		assertNotNull(catalog);
	}

	/**
	 * Check launched exceptions for buggy xml.
	 */
	@Test(expected = XmlFieldParsingException.class)
	public void testxmlToObjectInvalidXml() throws Exception {
	    XmlField reader = new XmlField();
		Catalog catalog = reader
				.xmlToObject(sampleXmlBuggy(), Catalog.class);
		assertNotNull(catalog);
	}

	/**
	 * Test null returned for non matching xml and type.
	 */
	@Test
	public void testxmlToObjectWrongEntityXml() throws Exception {
	    XmlField reader = new XmlField();
		Catalog catalog = reader.xmlToObject(sampleXmlWrongEntity(),
				Catalog.class);
		assertNull(catalog);
	}

	@Test
	public void testGetNodeFromInterface() throws Exception {
		XmlField reader = new XmlField();
		Catalog catalog = reader.xmlToObject(sampleXml1(), Catalog.class);

		assertNotNull(catalog);

		XmlFieldNode<?> node = reader.objectToNode(catalog);
		String xml = reader.nodeToXml(node);

		assertEquals(
				"<Catalog><Cd>  <id>64546</id>  <Title>toto</Title>  <Artist>Bob Dylan</Artist><Country>USA</Country>  <Company>Columbia</Company>  <Price>10.90</Price><Year>1985</Year></Cd><Cd>   <Title>toto</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country>   <Company>CBS Records</Company>   <Price>9.90</Price><Year>1988</Year></Cd></Catalog>",
				xml);
	}

	@Test
	public void testSimple() throws Exception {
		Map<String, String> map = XPathUtils
				.getElementSelectorAttributes("/cd/div[@class=\"title\"]");

		assertEquals("title", map.get("class"));
		assertEquals(1, map.size());

		map = XPathUtils
				.getElementSelectorAttributes("/cd/div[@class=\"title\"][@id=\'1\']");
		assertEquals("title", map.get("class"));
		assertEquals("1", map.get("id"));
		assertEquals(2, map.size());

	}

	@Test
	public void testInstantiate() throws Exception {
		XmlField reader = new XmlField();

		Catalog catalog = reader.newObject(Catalog.class);
		assertNotNull(catalog);
		assertTrue(catalog.getCd().length == 0);

		assertEquals("<Catalog/>", reader.objectToXml(catalog));

		catalog.addToCd().setTitle("title");
		catalog.addToCd().setPrice(987);

		assertEquals(
				"<Catalog><Cd><Title>title</Title></Cd><Cd><Price>987.0</Price></Cd></Catalog>",
				reader.objectToXml(catalog));
	}

	@Test
	public void testInstantiateWithNamespaces() throws Exception {
		XmlField reader = new XmlField();

		AtomCatalog catalog = reader.newObject(AtomCatalog.class);
		assertNotNull(catalog);
		assertTrue(catalog.getCd().length == 0);

		assertEquals(
				"<a:entry xmlns:a=\"http://www.w3.org/2005/Atom\" xmlns:x=\"http://www.w3.org/1999/xhtml\"/>",
				reader.objectToXml(catalog));

		catalog.addToCd().setTitle("title");
		catalog.addToCd().setPrice(987);

		assertEquals(
				"<a:entry xmlns:a=\"http://www.w3.org/2005/Atom\" xmlns:x=\"http://www.w3.org/1999/xhtml\">"
						+ "<content xmlns=\"http://www.w3.org/2005/Atom\"><div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"cd\"><span class=\"title\">title</span></div><div class=\"cd\"><span class=\"price\">987.0</span></div></div></content></a:entry>",
				reader.objectToXml(catalog));
	}

	@Test
	public void testNonPrimitiveNumbers() throws Exception {
		XmlField binder = new XmlField();
		Catalog catalog = binder.xmlToObject(sampleXml1(), Catalog.class);
		assertNotNull(catalog);

		Cd cd1 = catalog.getCd()[0];
		assertNotNull(cd1);
		assertEquals(64546, cd1.getId().intValue());

		Cd cd2 = catalog.getCd()[1];
		assertNotNull(cd2);
		assertNull(cd2.getId());

		cd2.setId(Integer.valueOf(15646984));

		assertEquals(
				"<Cd>   <Title>toto</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country>   <Company>CBS Records</Company>   <Price>9.90</Price><Year>1988</Year><id>15646984</id></Cd>",
				binder.objectToXml(cd2));
	}
}
