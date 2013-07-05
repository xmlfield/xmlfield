package org.xmlfield.validation.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.xmlfield.validation.annotations.NotEmpty;

public class NotEmptyHandler implements IHandler {

    @Override
    public boolean handles(Annotation a) {
        return a instanceof NotEmpty;
    }

    @Override
    public Set<ConstraintViolation<Object>> validate(Annotation a, Method m, Object o, Class<?> group)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        NotEmpty as = (NotEmpty) a;

        if ((group == null && as.groups().length == 0) || ArrayUtils.contains(as.groups(), group)) {

            Object result = m.invoke(o, new Object[] {});
            if (result == null)
                return createResultFromViolation(new ConstraintViolation<Object>(m.getName(), "<not-empty>", "<null>"));

            if (result instanceof String) {
                if (StringUtils.isEmpty((String) result))
                    return createResultFromViolation(new ConstraintViolation<Object>(m.getName(), "<not-empty>",
                            "<empty>"));
            }

        }
        return null;
    }

    private <T> Set<ConstraintViolation<T>> createResultFromViolation(ConstraintViolation<T> c) {

        Set<ConstraintViolation<T>> result = new HashSet<ConstraintViolation<T>>();

        result.add(c);
        return result;

    }

}
