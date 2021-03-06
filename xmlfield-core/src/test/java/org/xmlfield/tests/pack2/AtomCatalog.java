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
package org.xmlfield.tests.pack2;

import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.Namespaces;
import org.xmlfield.annotations.ResourceXPath;

/**
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
@Namespaces({ "xmlns:a=http://www.w3.org/2005/Atom",
		"xmlns:x=http://www.w3.org/1999/xhtml" })
@ResourceXPath("/a:entry")
public interface AtomCatalog {

	@FieldXPath("a:title/x:div/x:span[@class='name']")
	String getName();

	@FieldXPath("a:content/x:div/x:div[@class='cd']")
	AtomCd[] getCd();

	AtomCd addToCd();

}
