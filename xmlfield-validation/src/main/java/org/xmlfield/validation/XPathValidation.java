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
package org.xmlfield.validation;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.toArray;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.xmlfield.core.internal.XmlFieldUtils.getXmlFieldNode;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.xmlfield.core.api.XmlFieldNode;
import org.xmlfield.core.api.XmlFieldNodeList;
import org.xmlfield.core.api.XmlFieldSelector;
import org.xmlfield.core.api.XmlFieldSelectorFactory;
import org.xmlfield.core.exception.XmlFieldXPathException;
import org.xmlfield.core.internal.XmlFieldUtils.NamespaceMap;
import org.xmlfield.validation.annotations.XPathEquals;

/**
 * XPath Validation Utils tool.
 * 
 * @author David Andrianavalontsalama
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 */
@Deprecated
public class XPathValidation {

    private static class ExplosiveValidatorInvocationHandler implements InvocationHandler {

        private final XmlFieldNode node;

        public ExplosiveValidatorInvocationHandler(final XmlFieldNode node) {

            this.node = checkNotNull(node, "node");
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

            handleXPathEquals(EXPLOSIVE_CALLBACK, method, method.getName(), null, node);

            return null;
        }
    }

    private static interface HandleXPathCallback {

        void handleXPathEquals(String message);
    }

    private static class ValidatorWithErrorsInvocationHandler implements InvocationHandler {

        private final XmlFieldNode node;

        public ValidatorWithErrorsInvocationHandler(final XmlFieldNode node) {

            this.node = checkNotNull(node, "node");
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

            final String[] error = new String[1];

            handleXPathEquals(new HandleXPathCallback() {

                @Override
                public void handleXPathEquals(final String message) {

                    error[0] = message;
                }

            }, method, method.getName(), null, node);

            return error[0];
        }
    }

    private static final HandleXPathCallback EXPLOSIVE_CALLBACK = new HandleXPathCallback() {

        @Override
        public void handleXPathEquals(final String message) {

            throw new AssertionError(message);
        }
    };

    public static <T> T getExplosiveValidator(final Object object, final Class<T> validatorClass) {

        return getExplosiveValidator(getXmlFieldNode(object), validatorClass);
    }

    public static <T> T getExplosiveValidator(final XmlFieldNode node, final Class<T> validatorClass) {

        final Object proxy = Proxy.newProxyInstance(getClassLoader(), new Class<?>[] { validatorClass },
                new ExplosiveValidatorInvocationHandler(node));

        return validatorClass.cast(proxy);

    }

    public static String[] getValidationErrors(final Object object, final Class<?>... validationClasses)
            throws XmlFieldXPathException {

        return getValidationErrors(getXmlFieldNode(object), validationClasses);
    }

    public static String[] getValidationErrors(final XmlFieldNode node, final Class<?>... validationClasses)
            throws XmlFieldXPathException {

        final List<String> errors = new ArrayList<String>();

        validate(new HandleXPathCallback() {

            @Override
            public void handleXPathEquals(final String message) {

                errors.add(message);
            }

        }, node, validationClasses);

        return toArray(errors, String.class);
    }

    public static <T> T getValidatorWithErrors(final Object object, final Class<T> validatorClass) {
        return getValidatorWithErrors(getXmlFieldNode(object), validatorClass);
    }

    public static <T> T getValidatorWithErrors(final XmlFieldNode node, final Class<T> validatorClass) {

        final Object proxy = Proxy.newProxyInstance(getClassLoader(), new Class<?>[] { validatorClass },
                new ValidatorWithErrorsInvocationHandler(node));

        return validatorClass.cast(proxy);
    }

    public static void validateExplosively(final Object object, final Class<?>... validationClasses)
            throws XmlFieldXPathException {

        validateExplosively(getXmlFieldNode(object), validationClasses);
    }

    public static void validateExplosively(final XmlFieldNode node, final Class<?>... validationClasses)
            throws XmlFieldXPathException {

        validate(EXPLOSIVE_CALLBACK, node, validationClasses);
    }

    private static void assertXPath(final NamespaceMap namespaces, final XmlFieldSelector xpath,
            final HandleXPathCallback handleXPathCallback, final String methodName, final String message,
            final String refValueParam, final boolean isXPathRefValue, final String xpathExpression,
            final XmlFieldNode node) throws XmlFieldXPathException {

        final String result = xpath.selectXPathToString(namespaces, xpathExpression, node);

        if (result == null && refValueParam == null) {
            return;
        }

        final String refValue;

        if (isXPathRefValue) {

            refValue = xpath.selectXPathToString(namespaces, refValueParam, node);

        } else {

            refValue = refValueParam;
        }

        if (result == null || refValue == null || !refValue.equals(result)) {

            final String error = isBlank(message) ? "" : xpath.selectXPathToString(namespaces, message, node);

            String m = "Error in the following assertion: " + methodName + "/" + error + ": XPath=" + xpathExpression
                    + ", expected: ";

            if (isXPathRefValue) {

                m += "XPath=" + refValueParam + ": ";
            }

            m += refValue + ", but was: " + result;

            handleXPathCallback.handleXPathEquals(m);
        }
    }

    private static ClassLoader getClassLoader() {

        return XPathValidation.class.getClassLoader();
    }

    private static XmlFieldSelector getSelector() {
        return XmlFieldSelectorFactory.newInstance().newSelector();
    }

    private static void handleXPathEquals(final HandleXPathCallback handleXPathCallback, final AccessibleObject object,
            final String name, final Object objectValue, final XmlFieldNode node) throws XmlFieldXPathException {

        final XPathEquals xpathEquals = object.getAnnotation(XPathEquals.class);

        if (xpathEquals == null) {
            return;
        }

        final int[] intValues = xpathEquals.intValue();

        final boolean[] booleanValues = xpathEquals.booleanValue();

        final String[] namespaces = xpathEquals.namespaces();

        final String[] stringValues = xpathEquals.stringValue();

        final String[] xpathRefValues = xpathEquals.xpathRefValue();

        final String selector = xpathEquals.selector();

        final String message = xpathEquals.message();

        final String xpathExpression = xpathEquals.xpath();

        final String declValue;

        final boolean isXPathRefValue;

        if (objectValue != null) {

            declValue = objectValue.toString();

            isXPathRefValue = false;

        } else if (xpathRefValues != null && xpathRefValues.length == 1) {

            declValue = xpathRefValues[0];

            isXPathRefValue = true;

        } else if (intValues != null && intValues.length == 1) {

            declValue = Integer.toString(intValues[0]);

            isXPathRefValue = false;

        } else if (booleanValues != null && booleanValues.length == 1) {

            declValue = Boolean.toString(booleanValues[0]);

            isXPathRefValue = false;

        } else if (stringValues != null && stringValues.length == 1) {

            declValue = stringValues[0];

            isXPathRefValue = false;

        } else {

            declValue = null;

            isXPathRefValue = false;
        }

        final XmlFieldSelector xpathSelector = getSelector();

        final NamespaceMap namespaceMap = new NamespaceMap(namespaces);

        if (isBlank(selector)) {

            assertXPath(namespaceMap, xpathSelector, handleXPathCallback, name, message, declValue, isXPathRefValue,
                    xpathExpression, node);

        } else {

            final XmlFieldNodeList nodeList = xpathSelector.selectXPathToNodeList(namespaceMap, selector, node);

            final int count = nodeList.getLength();

            for (int i = 0; i < count; ++i) {

                final XmlFieldNode n = nodeList.item(i);

                assertXPath(namespaceMap, xpathSelector, handleXPathCallback, name, message, declValue,
                        isXPathRefValue, xpathExpression, n);
            }
        }
    }

    private static void validate(final HandleXPathCallback handleXPathCallback, final XmlFieldNode node,
            final Class<?>[] validationClasses) throws XmlFieldXPathException {

        for (final Class<?> validationClass : validationClasses) {

            for (final Method method : validationClass.getMethods()) {

                handleXPathEquals(handleXPathCallback, method, method.getName(), null, node);
            }

            for (final Field field : validationClass.getFields()) {

                Object objectValue;

                try {

                    objectValue = field.get(null);

                } catch (final IllegalAccessException e) {

                    objectValue = null;
                }

                handleXPathEquals(handleXPathCallback, field, field.getName(), objectValue, node);
            }
        }
    }
}