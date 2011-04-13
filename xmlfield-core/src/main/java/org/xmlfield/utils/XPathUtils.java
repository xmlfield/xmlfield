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
package org.xmlfield.utils;

import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBefore;
import static org.apache.commons.lang.StringUtils.substringBetween;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Xpath manipulation methods.
 * 
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 */
public class XPathUtils {

    public static final int TYPE_ATTRIBUTE = 3;

    public static final int TYPE_TAG = 1;

    public static final int TYPE_TAG_WITH_ATTRIBUTE = 2;

    /**
     * Return element name without any selector.
     * 
     * <p>
     * Attribute names are returned with a starting '@'
     * 
     * @param xPath
     *            the XPath query
     * @return element name
     */
    public static String getElementName(String xPath) {
        String name = XPathUtils.getElementNameWithSelector(xPath);

        // Remove org.xmlfield.tests.attribute selector
        name = substringBefore(name, "[");
        return name;
    }

    public static String getElementNameWithSelector(String xPath) {
        String name = xPath;
        if (name.contains("/")) {
            name = substringAfterLast(name, "/");
        }
        return name;
    }

    /**
     * Return element position if it exist.
     * 
     * @param xPath
     *            the XPath query
     * @return element position
     */
    public static int getElementPosition(String xPath) {
        String name = XPathUtils.getElementNameWithSelector(xPath);

        // Remove org.xmlfield.tests.attribute selector
        name = substringBetween(name, "[", "]");
        if (name == null) {
            return 1;
        }
        return Integer.parseInt(name);
    }

    /**
     * Retrieve the element prefix
     * 
     * @param xPath
     *            the XPath query
     * @return element prefix
     */
    public static String getElementPrefix(String xPath) {
        String name = XPathUtils.getElementNameWithSelector(xPath);

        if (name.contains(":")) {

            return substringBefore(name, ":");
        }

        return null;

    }

    /**
     * Returns the selector attributes of this XPath
     * 
     * @param xPath
     *            XPath
     * @return attributes map
     */
    public static Map<String, String> getElementSelectorAttributes(String xPath) {
        String name = XPathUtils.getElementNameWithSelector(xPath);
        HashMap<String, String> result = null;
        if (name.contains("[@")) {
            result = new HashMap<String, String>();
            String[] attributes = StringUtils.split(name, "[@");
            for (String a : attributes) {
                if (a.contains("=")) {
                    String[] aSplitted = a.split("=");
                    int endIndex = 1;
                    switch (aSplitted[1].charAt(0)) {
                    case '\"':
                    case '\'':
                        endIndex++;

                        break;
                    default:
                    }
                    aSplitted[1] = aSplitted[1].substring(1, aSplitted[1].length() - endIndex);

                    result.put(aSplitted[0], aSplitted[1]);
                }

            }

        }

        return result;
    }

    /**
     * Returns the type of the element described by this xPath query. See the following examples :
     * 
     * <ul>
     * <li>/parent/tagname => TYPE_TAG
     * <li>/parent/@attrname => TYPE_ATTRIBUTE
     * <li>/parent/tagname[@attrname=attrvalue] => TYPE_TAG_WITH_ATTRIBUTE
     * </ul>
     * 
     * 
     * @param xPath
     *            the XPath query
     * @return TYPE_TAG, TYPE_TAG_WITH_ATTRIBUTE, TYPE_ATTRIBUTE
     * 
     * @see XMLFieldUtils#getElementSelectorAttributes(String)
     * @see getElementName
     * @see XPathUtils#getElementNameWithSelector(String)
     * 
     */
    public static int getElementType(String xPath) {
        String name = XPathUtils.getElementNameWithSelector(xPath);

        if (name.startsWith("@")) {
            return XPathUtils.TYPE_ATTRIBUTE;
        }

        if (name.contains("[@")) {
            return XPathUtils.TYPE_TAG_WITH_ATTRIBUTE;
        }

        return XPathUtils.TYPE_TAG;
    }

    /**
     * Retrieve the xpath element from the specified xpath.
     * 
     * @param fieldXPath
     *            xpath mapped to a field
     * @return xpath element
     */
    public static String getElementXPath(String fieldXPath) {
        Pattern pattern = Pattern.compile("^(.+)\\/@(.+)$");
        Matcher matcher = pattern.matcher(fieldXPath);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Retrieve the xpath element from the specified xpath.
     * 
     * @param fieldXPath
     *            xpath mapped to a field
     * @return xpath element
     */
    public static boolean isAttributeXPath(String fieldXPath) {
        Pattern pattern = Pattern.compile("^(.*)@(.+)$");
        Matcher matcher = pattern.matcher(fieldXPath);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

}
