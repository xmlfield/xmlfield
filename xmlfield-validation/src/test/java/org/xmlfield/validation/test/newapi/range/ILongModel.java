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
package org.xmlfield.validation.test.newapi.range;

import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.ResourceXPath;
import org.xmlfield.validation.annotations.Range;

/**
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
@ResourceXPath("/item")
public interface ILongModel {

    @Range(min = 2, max = 10)
    @FieldXPath("long1")
    Integer getLong1();

    void setLong1(Integer i);

    @Range(min = 2, max = 10)
    @FieldXPath("long0")
    int getLong0();

    void setLong0(int i);

}
