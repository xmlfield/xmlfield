package org.xmlfield.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.validation.handlers.ConstraintViolation;
import org.xmlfield.validation.handlers.IHandler;
import org.xmlfield.validation.handlers.NotEmptyHandler;
import org.xmlfield.validation.handlers.RangeHandler;
import org.xmlfield.validation.handlers.SizeHandler;
import org.xmlfield.validation.handlers.ValuesHandler;

public class XmlFieldValidator {

    static IHandler[] handlers = new IHandler[] { new NotEmptyHandler(), new SizeHandler(), new ValuesHandler(),
            new RangeHandler() };

    public XmlFieldValidator() {

    }

    public void ensureValidation(Object xmlFieldObject) throws XmlFieldValidationException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        Set<ConstraintViolation<Object>> result = validate(xmlFieldObject, true);

        Iterator<ConstraintViolation<Object>> i = result.iterator();
        while (i.hasNext()) {
            ConstraintViolation<Object> c = i.next();
            throw new XmlFieldValidationException(c.getMethodName(), c.getExpected(), c.getActual());
        }

    }

    public Set<ConstraintViolation<Object>> validate(Object xmlFieldObject) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        return validate(xmlFieldObject, false);
    }

    private Set<ConstraintViolation<Object>> validate(Object xmlFieldObject, boolean returnOnFirstViolation)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        // Prepare result
        Set<ConstraintViolation<Object>> result = new HashSet<ConstraintViolation<Object>>();

        // Get object interfaces.
        Type[] types = xmlFieldObject.getClass().getGenericInterfaces();

        for (Type type : types) {
            Class<?> interfaceClass = (Class<?>) type;
            Method[] methods = interfaceClass.getMethods();

            if (methods != null) {
                for (Method m : methods) {

                    String methodName = m.getName();

                    // We only process annotations on 'get' or 'is' method. All
                    // other methods are ignored.
                    if (!methodName.startsWith("get") && !methodName.startsWith("is"))
                        continue;

                    Annotation[] anos = m.getAnnotations();
                    for (Annotation a : anos) {

                        // Do validation
                        for (IHandler h : handlers) {
                            if (h.handles(a)) {
                                Set<ConstraintViolation<Object>> resultHandler = h.validate(a, m, xmlFieldObject);
                                if (resultHandler != null && resultHandler.size() > 0) {
                                    result.addAll(resultHandler);

                                    if (returnOnFirstViolation)
                                        return result;
                                }
                            }
                        }

                        // If the method returns another XmlField object, do
                        // recursive validation.
                        if (a instanceof FieldXPath) {
                            // Single object
                            if (m.getReturnType() != null && m.getReturnType().isInterface()) {
                                result.addAll(validate(m.invoke(xmlFieldObject)));
                                if (returnOnFirstViolation)
                                    return result;
                            }

                            // Array
                            if (m.getReturnType() != null && m.getReturnType().isArray()
                                    && m.getReturnType().getComponentType().isInterface()) {
                                Object[] arrayResult = (Object[]) m.invoke(xmlFieldObject);
                                // Validate every object
                                if (arrayResult != null) {
                                    for (Object o : arrayResult) {
                                        result.addAll(validate(o));
                                        if (returnOnFirstViolation)
                                            return result;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
