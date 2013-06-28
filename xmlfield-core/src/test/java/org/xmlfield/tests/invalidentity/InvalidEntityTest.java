/*
 * Copyright 2010-2013 Capgemini
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
package org.xmlfield.tests.invalidentity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.xmlfield.core.XmlField;
import org.xmlfield.core.api.XmlFieldNode;
import org.xmlfield.core.exception.XmlFieldParsingException;
import org.xmlfield.core.impl.dom.DomNodeParser;

public class InvalidEntityTest {

	private final XmlField xmlfield = new XmlField();

	/**
	 * Read an invalid document from an InputStream. Sanitizing is NOT yet
	 * supported from InputSream : this results in parsing exception.
	 * 
	 * @throws Exception
	 */
	@Test(expected = XmlFieldParsingException.class)
	public void testReadInvalidEntityInputStream() throws Exception {

		final InputStream xml = new ByteArrayInputStream(
				"<?xml version=\"1.0\"?><Catalog><test>&#25;</test></Catalog>"
						.getBytes("UTF-8"));
		final XmlFieldNode node = xmlfield.xmlToNode(xml);

		Assert.assertEquals(null, "<Catalog><test>�</test></Catalog>",
				xmlfield.objectToXml(node));
	}

	/**
	 * Read an invalid document from a String. Sanitizing is supported from
	 * String : invalid characters are replaced by the unknown character.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReadInvalidEntityString() throws Exception {

		final String xml = "<?xml version=\"1.0\"?><Catalog><test>&#25;</test></Catalog>";
		final XmlFieldNode node = xmlfield.xmlToNode(xml);

		Assert.assertEquals(null, "<Catalog><test>�</test></Catalog>",
				xmlfield.objectToXml(node));
	}

	/**
	 * Read an invalid document from a String. Sanitizing is disabled.Result is
	 * a parsing exception.
	 * 
	 * @throws Exception
	 */
	@Test(expected = XmlFieldParsingException.class)
	public void testReadInvalidEntityStringNoCleaning() throws Exception {
		Map<String, String> conf = new HashMap<String, String>();
		conf.put(DomNodeParser.CONFIG_CLEANUP_XML, "false");
		XmlField xmlfield = new XmlField(conf);

		final String xml = "<?xml version=\"1.0\"?><Catalog><test>&#25;</test></Catalog>";
		xmlfield.xmlToNode(xml);

	}

	/**
	 * Use invalid character in a set method. Invalid characters are replaced by
	 * the unknown character.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWriteInvalidEntity() throws Exception {
		Catalog node = xmlfield.newObject(Catalog.class);

		// Create invalid character
		node.setTest("Test" + (char) 25 + "");

		String xml = xmlfield.objectToXml(node);

		node = xmlfield.xmlToObject(xml, Catalog.class);
		Assert.assertEquals(null, "<Catalog><test>Test�</test></Catalog>",
				xmlfield.objectToXml(node));
	}
}
