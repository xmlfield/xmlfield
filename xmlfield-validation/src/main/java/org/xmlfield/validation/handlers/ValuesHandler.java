package org.xmlfield.validation.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.xmlfield.validation.annotations.Values;

public class ValuesHandler implements IHandler {

    @Override
    public boolean handles(Annotation a) {
        return a instanceof Values;
    }

    @Override
    public Set<ConstraintViolation<Object>> validate(Annotation a, Method m, Object xmlFieldObject, Class<?> group)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        Values av = (Values) a;
        if ((group == null && av.groups().length == 0) || ArrayUtils.contains(av.groups(), group)) {

        Object o = m.invoke(xmlFieldObject, new Object[] {});

        if (o == null)
            return null;

        boolean ok = false;
        if (av.string().length > 0) {
            Object[] acceptedValues = av.string();
            for (Object ao : acceptedValues) {
                if (ao.equals(o)) {
                    ok = true;
                    break;
                }
            }
            if (!ok)
                return createResultFromViolation(new ConstraintViolation<Object>(m.getName(), StringUtils.join(
                        acceptedValues, ","), o == null ? "null" : o.toString()));

        }

        else if (av.integer().length > 0) {
            int[] acceptedValues = av.integer();

            for (int ao : acceptedValues) {
                if (o != null && o.equals(ao)) {
                    ok = true;
                    break;
                }
            }

            if (!ok)
                return createResultFromViolation(new ConstraintViolation<Object>(m.getName(),
                        ArrayUtils.toString(acceptedValues), o == null ? "null" : o.toString()));

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
