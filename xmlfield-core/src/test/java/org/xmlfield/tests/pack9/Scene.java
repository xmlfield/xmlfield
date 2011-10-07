package org.xmlfield.tests.pack9;

import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.ResourceXPath;

@ResourceXPath("/SCENE")
public interface Scene {
    @FieldXPath("SPEECH")
    Speech[] getSpeeches();
    
    @FieldXPath("TITLE")
    String getTitle();
    
    @FieldXPath("STAGEDIR")
    String getStageDir();
}
