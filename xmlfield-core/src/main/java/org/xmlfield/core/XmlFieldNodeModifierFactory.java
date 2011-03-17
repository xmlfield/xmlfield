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
package org.xmlfield.core;

/**
 * <p>
 * An <code>XmlFieldNodeModifierFactory</code> instance can be used to create {@link XmlFieldParser} objects.
 * </p>
 * 
 * <p>
 * See {@link #newInstance()} for lookup mechanism.
 * </p>
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 */
public abstract class XmlFieldNodeModifierFactory extends XmlFieldFactory {
    /**
     * <p>
     * Get a new <code>XmlFieldNodeModifierFactory</code> instance.
     * 
     * @return Instance of an <code>XmlFieldNodeModifierFactory</code>.
     * 
     * @throws RuntimeException
     *             When there is a failure in creating an <code>XmlFieldNodeModifierFactory</code>
     */
    public static final XmlFieldNodeModifierFactory newInstance() {
        return newInstance(XmlFieldNodeModifierFactory.class);
    }

    /**
     * <p>
     * Return a new <code>XmlFieldNodeModifier</code> using the underlying object model determined when the
     * <code>XmlFieldNodeModifierFactory</code> was instantiated.
     * </p>
     * 
     * @return New instance of an <code>XmlFieldNodeModifier</code>.
     */
    public abstract XmlFieldNodeModifier newModifier();
}
