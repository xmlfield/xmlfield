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
package org.xmlfield.tests.array;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlfield.core.XmlField;
import org.xmlfield.core.api.XmlFieldNode;

/**
 * Test getter with array of native types.
 * 
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
public class ToArrayTest {

	Logger log = LoggerFactory.getLogger(ToArrayTest.class);

	private final XmlField xf = new XmlField();

	/**
	 * Test clearing array with null.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testArray() throws Exception {

		// Load initial XML
		final String xml = "<list><string>S</string><integer>1</integer><boolean>true</boolean></list>";
		final XmlFieldNode node = xf.xmlToNode(xml);

		// Attach and assert object values
		final MultipleLists list = xf.nodeToObject(node, MultipleLists.class);
		assertEquals(1, list.getString().length);

		// Fails : see
		// http://sourceforge.net/apps/mantisbt/xmlfield/view.php?id=33
		assertEquals(1, list.getInteger().length);
		assertEquals(1, list.getBoolean().length);

	}

}
