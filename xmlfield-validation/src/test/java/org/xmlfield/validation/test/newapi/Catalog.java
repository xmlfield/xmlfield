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
package org.xmlfield.validation.test.newapi;

import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.ResourceXPath;
import org.xmlfield.validation.annotations.NotEmpty;
import org.xmlfield.validation.annotations.Values;

/**
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 */
@ResourceXPath("/item")
public interface Catalog {

    String CD_CONST1 = "value1";
    String CD_CONST2 = "value2";

    @FieldXPath("items/item")
    Item[] getItems();

    Item addToItems();

    int sizeOfItems();

    void setItems(Item[] items);

    void removeFromItems(Item cd);

    @NotEmpty
    @FieldXPath("name")
    String getName();

    void setName(String type);

    @NotEmpty
    @Values(string = { CD_CONST1, CD_CONST2, "toto" })
    @FieldXPath("type")
    String getType();

    
    @Values(integer = { 1,2 })
    @FieldXPath("number")
    Integer getNumber();

    void setNumber( Integer i );
    
    void setType(String type);

}
