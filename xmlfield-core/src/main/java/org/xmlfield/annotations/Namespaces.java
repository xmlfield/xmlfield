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
package org.xmlfield.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * utiliser cette annotation sur une interface de <em>getters</em> ou de
 * <em>setters</em>, pour indiquer quels namespaces utiliser dans la lecture et
 * l'écriture du XML.
 * <p>
 * Les valeurs à passer sont de la forme suivante :
 * <ul>
 * <li> <tt>"xmlns:a=http://aaaa"</tt>,
 * <li>ou <tt>"xmlns:a=\"http://aaaa\""</tt>,
 * </ul>
 * 
 * @author David Andrianavalontsalama
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Namespaces {

    String[] value();
}
