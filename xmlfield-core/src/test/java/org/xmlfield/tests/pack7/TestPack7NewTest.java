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

import org.junit.Test;
import org.xmlfield.core.XmlField;
import org.xmlfield.core.api.XmlFieldNode;

/**
 * @author Jean-Pierre Grillon
 */
public class TestPack7NewTest {

	private final XmlField binder = new XmlField();

	@Test
	public void testNull() throws Exception {
		final String xml = "<Catalog></Catalog>";
		final XmlFieldNode node = binder.xmlToNode(xml);

		final Catalog catalog = binder.nodeToObject(node, Catalog.class);
		assertNull(catalog.getCd());
	}

	@Test
	public void testSet() throws Exception {
		String xml = "<Catalog></Catalog>";
		String title = "Space Oddity";
		XmlFieldNode node = binder.xmlToNode(xml);

		final Catalog catalog = binder.nodeToObject(node, Catalog.class);

		Cd cd = catalog.newCd();
		cd.setTitle(title);

		assertNotNull(catalog.getCd());
		xml = binder.objectToXml(catalog);
		assertEquals(
				"<Catalog><Cd><Title>" + title + "</Title></Cd></Catalog>", xml);

		catalog.setCd(cd);
		assertEquals(
				"<Catalog><Cd><Title>" + title + "</Title></Cd></Catalog>", xml);
	}

	@Test
	public void testSet2() throws Exception {
		String title = "Ça fait rire les oiseaux";
		String xml = "<Catalog><Cd><Title>" + title + "</Title></Cd></Catalog>";
		XmlFieldNode node = binder.xmlToNode(xml);

		final Catalog catalog = binder.nodeToObject(node, Catalog.class);

		Cd cd = catalog.newCd();

		assertNotNull(catalog.getCd());
		assertEquals(cd.getTitle(), title);

	}

	@Test
	public void testSimple() throws Exception {
		String xml = "<Catalog><Cd /></Catalog>";
		final XmlFieldNode node = binder.xmlToNode(xml);

		final Catalog catalog = binder.nodeToObject(node, Catalog.class);
		assertNotNull(catalog.getCd());
		assertEquals(null, catalog.getCd().getTitle());

		catalog.getCd().setTitle("Wolfmother");

		xml = binder.objectToXml(catalog);
		assertEquals("<Catalog><Cd><Title>Wolfmother</Title></Cd></Catalog>",
				xml);
	}

}
