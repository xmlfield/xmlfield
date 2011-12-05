package org.xmlfield.validation.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;


public interface IHandler {

    boolean  handles( Annotation a );
    
    Set<ConstraintViolation<Object>> validate( Annotation a, Method m, Object o) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
    
}
