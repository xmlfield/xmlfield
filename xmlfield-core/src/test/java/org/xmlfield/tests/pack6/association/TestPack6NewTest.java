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
package org.xmlfield.tests.pack6.association;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlfield.core.XmlField;
import org.xmlfield.core.api.XmlFieldNode;

/**
 * Test explicit collection annotation : returning different types in a single
 * get.
 * 
 * @author Olivier Lafon <olivier.lafon@capgemini.com>
 */
public class TestPack6NewTest {

	private final XmlField binder = new XmlField();

	Logger log = LoggerFactory.getLogger(TestPack6NewTest.class);

	@Test
	public void testGet() throws Exception {

		final String xml = "<Catalog> <goods> <Cd>    <Title>Black angel</Title>    <Artist>Savage Rose</Artist>    <Country>EU</Country>    <Company>Mega</Company>    <Price>10.90</Price>    <Year>1995</Year>  </Cd>  <Book>    <Title>Germinal</Title>    <Author>Emile Zola</Author>    <Price>10.20</Price>  </Book>  <Book>    <Title>La Faute de l’abbé Mouret</Title>    <Author>Emile Zola</Author>    <Price>10.20</Price>  </Book><Crayola><Color>Red</Color></Crayola>  <Cd>    <Title>One night only</Title>    <Artist>Many</Artist>    <Country>USA</Country>    <Company>Grammy</Company>    <Price>10.20</Price>    <Year>1999</Year>  </Cd>  <Book>    <Title>La Bête humaine</Title>    <Author>Emile Zola</Author>    <Price>10.20</Price>  </Book></goods></Catalog>";

		final XmlFieldNode node = binder.xmlToNode(xml);

		final Catalog catalog = binder.nodeToObject(node, Catalog.class);
		// the catalog get otherCd must return Cd and Book only (not a Crayola)
		assertEquals(5, catalog.getGoods().length);

		assertTrue(catalog.getGoods()[1] instanceof Book);
		// the second item must be the zola book germinal
		Book livreZola = (Book) catalog.getGoods()[1];
		assertEquals("Germinal", livreZola.getTitle());

		Cd oneNight = (Cd) catalog.getGoods()[3];
		assertEquals("One night only", oneNight.getTitle());

		assertFalse(catalog.getGoods()[2] instanceof Cd);

		for (int i = 0; i < catalog.getGoods().length; i++) {
			log.info(catalog.getGoods()[i].toString());
		}
	}
}
