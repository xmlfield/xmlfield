package org.xmlfield.tests.pack9;


import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.ResourceXPath;

@ResourceXPath("/SPEECH")
public interface Speech {
    @FieldXPath("LINE")
    String[] getLines();
    
    @FieldXPath("SPEAKER")
    String getSpeaker();
}
