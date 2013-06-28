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
package org.xmlfield.core.impl.dom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling invalid XML entities and characters.
 * <p>
 * Invalid characters/entity are replaced by the "unknown" character (\uFFFD).
 * 
 * @author Nicolas Richeton
 */
public class InputSanitizer {

	static Pattern INVALID_XML_CHARS = Pattern
			.compile("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\uD800\uDC00-\uDBFF\uDFFF]");

	static Pattern XML_ENTITY = Pattern.compile("&#([0-9]+);");

	/**
	 * Replace invalid characters by the unknown character (\uFFFD).
	 * 
	 * @param s
	 *            text to sanitize.
	 * @return sanitized text.
	 */
	public static String sanitizeText(String s) {
		if (s == null) {
			return null;
		}
		return INVALID_XML_CHARS.matcher(s).replaceAll("\uFFFD");
	}

	/**
	 * Replace invalid entities by the entity corresponding to the unknown
	 * character (\uFFFD).
	 * 
	 * @param xml
	 *            XML input to sanitize.
	 * @return sanitized XML input
	 */
	public static String sanitizeXml(String xml) {
		if (xml == null) {
			return null;
		}
		Matcher m = XML_ENTITY.matcher(xml);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {

			String entityValue = m.group(1);
			int entity = Integer.parseInt(entityValue);

			if (!(entity == 9 || entity == 10 || entity == 13 || entity >= 32
					&& entity <= 55295 || entity >= 57344 && entity <= 65533
					|| entity == 55296 || entity >= 56320 && entity <= 56319 || entity == 57343)) {
				entityValue = "65533";
			}
			m.appendReplacement(sb, "&#" + entityValue + ";");
		}
		m.appendTail(sb);

		return sb.toString();
	}
}
