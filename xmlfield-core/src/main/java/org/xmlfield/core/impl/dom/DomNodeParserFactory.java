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
package org.xmlfield.core.impl.dom;

import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xmlfield.core.api.XmlFieldNodeParser;
import org.xmlfield.core.api.XmlFieldNodeParserFactory;

/**
 * Default xml field node parser factory implementation.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * @author Nicolas Richeton
 * 
 */
public class DomNodeParserFactory extends XmlFieldNodeParserFactory {

	@Override
	public XmlFieldNodeParser<?> newParser(Map<String, String> configuration) {
		try {
			return new DomNodeParser(configuration);
		} catch (TransformerConfigurationException e) {
			throw new IllegalStateException(
					"Unable to create XmlField xml document parser", e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new IllegalStateException(
					"Unable to create XmlField xml document parser", e);
		}

	}

}
