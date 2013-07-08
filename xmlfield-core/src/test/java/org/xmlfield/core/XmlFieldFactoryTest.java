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
package org.xmlfield.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests on {@link XmlFieldFactory}
 * 
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
public class XmlFieldFactoryTest {

	@Test
	public void testFactory() throws Exception {

		XmlFieldFactory factory = new XmlFieldFactory();

		Map<String, String> conf = new HashMap<String, String>();
		conf.put(OutputKeys.INDENT, "true");
		factory.setParserConfiguration(conf);
		factory.setGetterCache(true);

		XmlField xf = factory.getXmlField();

		Assert.assertNotNull(xf);

		Assert.assertEquals("true",
				xf.getParserConfiguration().get(OutputKeys.INDENT));

		Assert.assertTrue(xf.isGetterCache());

		// Ensure new objects are returned each time.
		XmlField xf2 = factory.getXmlField();
		Assert.assertNotNull(xf);
		Assert.assertFalse(xf == xf2);
	}

	@Test
	public void testFactoryThreadLocal() throws Exception {

		XmlFieldFactory factory = new XmlFieldFactory(true);

		Map<String, String> conf = new HashMap<String, String>();
		conf.put(OutputKeys.INDENT, "true");
		factory.setParserConfiguration(conf);
		XmlField xf = factory.getXmlField();

		Assert.assertNotNull(xf);

		Assert.assertEquals("true",
				xf.getParserConfiguration().get(OutputKeys.INDENT));

		// Ensure the same object is returned each time.
		XmlField xf2 = factory.getXmlField();
		Assert.assertNotNull(xf);
		Assert.assertTrue(xf == xf2);
	}
}
