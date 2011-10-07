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
package org.xmlfield.tests.date;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlfield.core.XmlFieldBinder;
import org.xmlfield.utils.XmlUtils;

/**
 * Test basic xmlfield usage, with a simple xml file (no namespace, no
 * attribute).
 * 
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
public class DateTest {

	Logger log = LoggerFactory.getLogger(DateTest.class);

	/**
	 * Check read and update abilities (get/set).
	 * 
	 * @throws Exception
	 */
	@Test
//	@Ignore
	public void testSimple() throws Exception {
		XmlFieldBinder binder = new XmlFieldBinder();
		DateStorage xml = binder.instantiate(DateStorage.class);

		// Création et relecture
		DateTime dateTime = new DateTime();
		xml.setDate(dateTime);
		xml.setDateDefault(dateTime);
		DateTime dateTime2 = xml.getDate();
		DateTime dateTime2Default = xml.getDateDefault();

		System.out.println( XmlUtils.xmlFieldNodeToXml(xml));
		
	
		Assert.assertEquals(dateTime.getHourOfDay(), dateTime2Default.getHourOfDay());
		//Assert.assertEquals(dateTime.getHourOfDay(), dateTime2.getHourOfDay());

		// Nouveau cycle avec une date lue par XmlField, écrite puis relue
		xml.setDate(dateTime2);
		
		DateTime dateTime3 = xml.getDate();
		
		Assert.assertEquals(dateTime2.getHourOfDay(), dateTime3.getHourOfDay());
	}

}
