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

import org.xmlfield.core.api.XmlFieldSelector;
import org.xmlfield.core.api.XmlFieldSelectorFactory;

/**
 * Default xml field selector factory using Jaxen as Xpath implementation.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * @author Nicolas Richeton
 * 
 */
public class DomSelectorFactory extends XmlFieldSelectorFactory {

	static private DomJaxenSelector selector = new DomJaxenSelector();

	@Override
	public XmlFieldSelector newSelector() {
		// DomJaxenSelector is thread safe, we can always return the same
		// object.
		return selector;
	}

}
