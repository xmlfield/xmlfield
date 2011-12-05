package org.xmlfield.validation.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.xmlfield.validation.annotations.Size;
import org.xmlfield.validation.annotations.Values;

public class SizeHandler implements IHandler {

    @Override
    public boolean handles(Annotation a) {
        return a instanceof Size;
    }

    @Override
    public Set<ConstraintViolation<Object>> validate(Annotation a, Method m, Object o) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {

        Size as = (Size) a;

        int currentValue = 0;

        Object result = m.invoke(o, new Object[] {});
        if (result instanceof Object[]) {
            Object[] array = (Object[]) result;
            currentValue = array.length;
        }

        if (result instanceof String) {
            currentValue = ((String) result).length();
        }

        if (currentValue > as.max() || currentValue < as.min())
            return createResultFromViolation(new ConstraintViolation<Object>(m.getName(), "min/max",
                    String.valueOf(currentValue)));

        return null;
    }

    private <T> Set<ConstraintViolation<T>> createResultFromViolation(ConstraintViolation<T> c) {

        Set<ConstraintViolation<T>> result = new HashSet<ConstraintViolation<T>>();

        result.add(c);
        return result;

    }

}
