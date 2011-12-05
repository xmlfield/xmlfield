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
        Set<ConstraintViolation<Object>> result = validate(xmlFieldObject);

        Iterator<ConstraintViolation<Object>> i = result.iterator();
        while (i.hasNext()) {
            ConstraintViolation<Object> c = i.next();
            throw new XmlFieldValidationException(c.getMethodName(), c.getExpected(), c.getActual());
        }

    }

    public Set<ConstraintViolation<Object>> validate(Object xmlFieldObject) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {

        Set<ConstraintViolation<Object>> result = new HashSet<ConstraintViolation<Object>>();

        Type[] types = xmlFieldObject.getClass().getGenericInterfaces();

        for (Type t : types) {
            Class<?> ic = (Class<?>) t;
            Method[] methods = ic.getMethods();

            if (methods != null) {
                for (Method m : methods) {

                    Annotation[] anos = m.getAnnotations();

                    for (Annotation a : anos) {

                        if (a instanceof FieldXPath) {
                            if (m.getReturnType() != null && m.getReturnType().isInterface()) {
                                validate(m.invoke(xmlFieldObject));
                            }

                            if (m.getReturnType() != null && m.getReturnType().isArray()
                                    && m.getReturnType().getComponentType().isInterface()) {
                                Object[] resultMethod = (Object[]) m.invoke(xmlFieldObject);
                                if (resultMethod != null) {
                                    for (Object o : result) {
                                        validate(o);

                                    }
                                }
                            }
                        } else
                            for (IHandler h : handlers) {
                                if (h.handles(a)) {
                                    Set<ConstraintViolation<Object>> resultHandler = h.validate(a, m, xmlFieldObject);
                                    if (resultHandler != null && resultHandler.size() > 0) {
                                        result.addAll(resultHandler);
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
