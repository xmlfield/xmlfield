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

import javax.xml.xpath.XPathConstants;

import org.joda.time.DateTime;

/**
 * utiliser cette annotation sur une méthode de type <em>getter</em> ou
 * <em>setter</em>, pour indiquer à quel sous-emplacement dans le DOM il faut
 * aller chercher ou manipuler le champ correspondant par XPath.
 * 
 * @author David Andrianavalontsalama
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldXPath {

    /**
     * le sous-emplacement dans le DOM il faut aller chercher ou manipuler le
     * champ correspondant par XPath.
     * <p>
     * L'expression XPath ne doit pas commencer par un slash ("<tt>/</tt>").
     */
    String value();

    /**
     * le format de sérialisation/désérialisaiton utiliser entre une valeur
     * récupérée par XPath de type {@link XPathConstants#NODE} et la propriété
     * Java annotée.
     * <p>
     * Exemple de format pour une propriété {@link DateTime} :
     * <tt>"yyyy-MM-dd"</tt>.
     */
    String format() default "";

    /**
     * le type que retournera l'évaluation XPath.
     * <p>
     * Les correspondances sont les suivantes :
     * <ul>
     * <li><tt>Object.class</tt>(par défaut) : {@link XPathConstants#NODESET} ou
     * {@link XPathConstants#NODE}, selon que le type de retour du
     * <em>getter</tt> annoté par <tt>FieldXPath</tt> est un tableau ou non.
     * La propriété Java peut être de n'importe quel type : <em>int</em>,
     * <em>String</em>, objet…
     * <li><tt>Number.class</tt> : {@link XPathConstants#NUMBER}, par exemple
     * pour une expression XPath de type "<tt>count(xxx)</tt>". La valeur sera
     * ensuite convertie dans le type déclaré de la propriété Java (<em>int</em>, <em>long</em>, <em>short</em>, <em>float</em> ou <em>double</em>).
     * <li><tt>String.class</tt> : {@link XPathConstants#STRING}, par exemple
     * pour une expression XPath de type "<tt>substring(xxx)</tt>".
     * </ul>
     */
    Class<?> xpathType() default Object.class;
}
