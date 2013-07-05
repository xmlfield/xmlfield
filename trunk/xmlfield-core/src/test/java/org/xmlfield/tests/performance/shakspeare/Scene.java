package org.xmlfield.tests.performance.shakspeare;

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
