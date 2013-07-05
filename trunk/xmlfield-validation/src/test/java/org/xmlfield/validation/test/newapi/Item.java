package org.xmlfield.validation.test.newapi;

import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.ResourceXPath;
import org.xmlfield.validation.annotations.NotEmpty;

@ResourceXPath("/item")
public interface Item {
    
    @NotEmpty
    @FieldXPath("name")
    String getName();
    void setName( String name);

}
