package org.xmlfield.validation.test.newapi;

import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.ResourceXPath;

@ResourceXPath("/item")
public interface Item {
    
    @FieldXPath("name")
    String getName();
    void setName( String name);

}
