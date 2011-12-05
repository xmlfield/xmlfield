package org.xmlfield.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Set;

import org.xmlfield.validation.handlers.ConstraintViolation;
import org.xmlfield.validation.handlers.IHandler;
import org.xmlfield.validation.handlers.NotEmptyHandler;
import org.xmlfield.validation.handlers.SizeHandler;
import org.xmlfield.validation.handlers.ValuesHandler;

public class XmlFieldValidator {

    static IHandler[] handlers = new IHandler[] { new NotEmptyHandler(), new SizeHandler(), new ValuesHandler() };

    public XmlFieldValidator() {

    }

    public boolean validate(Object xmlFieldObject) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, XmlFieldValidationException {
        Type[] types = xmlFieldObject.getClass().getGenericInterfaces();

        for (Type t : types) {
            Class<?> ic = (Class<?>) t;
            Method[] methods = ic.getMethods();

            if (methods != null) {
                for (Method m : methods) {

                    Annotation[] anos = m.getAnnotations();

                    for (Annotation a : anos) {

                        for (IHandler h : handlers) {
                            if (h.handles(a)) {
                                Set<ConstraintViolation<Object>> result = h.validate(a, m, xmlFieldObject);
                                if (result != null && result.size() > 0) {
                                    Iterator<ConstraintViolation<Object>> i = result.iterator();
                                    while (i.hasNext()) {
                                        ConstraintViolation<Object> c = i.next();
                                        throw new XmlFieldValidationException(c.getMethodName(), c.getExpected(),
                                                c.getActual());
                                    }
                                }
                                break;
                            }
                        }

                    }
                }

            }
        }

        return true;
    }
}
