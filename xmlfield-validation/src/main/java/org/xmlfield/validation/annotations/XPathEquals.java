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
package org.xmlfield.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * XPath equals interface.
 * 
 * @author David Andrianavalontsalama
 * 
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD })
public @interface XPathEquals {

    /**
     * la valeur de référence.
     */
    int[] intValue() default {};

    /**
     * la valeur de référence.
     */
    boolean[] booleanValue() default {};

    /**
     * la valeur de référence.
     */
    String[] stringValue() default {};

    /**
     * le XPath de la valeur de référence.
     */
    String[] xpathRefValue() default {};

    /**
     * l'expression XPath, à partir du sélecteur s'il est présent.
     */
    String xpath();

    /**
     * le sélecteur XPath, éventuellement.
     */
    String selector() default "";

    /**
     * le message à renvoyer en cas d'erreur (expression XPath).
     */
    String message() default "";

    /**
     * les namespaces.
     */
    String[] namespaces() default {};
}
