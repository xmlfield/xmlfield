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
package org.xmlfield.tests.xxe;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlfield.core.XmlField;
import org.xmlfield.core.api.XmlFieldNode;

/**
 * Test basic xmlfield usage, with a simple xml file (no namespace, no
 * attribute).
 * 
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
public class XXETest {

	Logger log = LoggerFactory.getLogger(XXETest.class);

	private final XmlField xmlfield = new XmlField();

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPreventXXE() throws Exception {
		// Create initial XML
		final String xml = "<?xml version=\"1.0\"?><!DOCTYPE customer[<!ENTITY name SYSTEM \"/\">]><Catalog><test>&name;</test></Catalog>";
		final XmlFieldNode node = xmlfield.xmlToNode(xml);

		// Get object and do initial asserts
		final Catalog catalog = xmlfield.nodeToObject(node, Catalog.class);

		Assert.assertEquals("<Catalog><test/></Catalog>",
				xmlfield.objectToXml(catalog));

	}

}
