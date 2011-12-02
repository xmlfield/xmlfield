package org.xmlfield.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.xmlfield.validation.annotations.NotEmpty;
import org.xmlfield.validation.annotations.Values;
import org.xmlfield.validation.test.newapi.Catalog;

public class XmlFieldValidator {
    public XmlFieldValidator() {

    }

    public boolean validate(Catalog c) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, XmlFieldValidationException {
        Type[] types = c.getClass().getGenericInterfaces();

        for (Type t : types) {
            Class<?> ic = (Class<?>) t;
            Method[] methods = ic.getMethods();

            if (methods != null) {
                for (Method m : methods) {

                    Annotation[] anos = m.getAnnotations();

                    for (Annotation a : anos) {

                        if (NotEmpty.class.isInstance(a)) {
                            Object o = m.invoke(c, new Object[] {});
                            if (o == null)
                                throw new XmlFieldValidationException(m.getName(), "<not-empty>", "<null>");

                            if (o instanceof String) {
                                if (StringUtils.isEmpty((String) o))
                                    throw new XmlFieldValidationException(m.getName(), "<not-empty>", "<empty>");
                            }
                        }

                        if (Values.class.isInstance(a)) {
                            Values av = (Values) a;
                            Object o = m.invoke(c, new Object[] {});

                            if( o == null )
                                break;
                            
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
                                    throw new XmlFieldValidationException(m.getName(), StringUtils.join(acceptedValues,","), o==null?"null":o.toString());
              
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
                                    throw new XmlFieldValidationException(m.getName(), ArrayUtils.toString(acceptedValues), o==null?"null":o.toString());
              
                            }

                        }
                    }
                }

            }
        }

        return true;
    }
}
