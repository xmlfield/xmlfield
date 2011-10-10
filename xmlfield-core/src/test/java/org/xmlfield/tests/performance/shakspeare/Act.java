package org.xmlfield.tests.performance.shakspeare;

import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.ResourceXPath;

/**
 * @author Mabrouk Belhout
 */
@ResourceXPath("/ACT")
public interface Act {
    @FieldXPath("SCENE")
    Scene[] getScenes();
    
    @FieldXPath("PROLOGUE/TITLE")
    String getPrologueTitle();
}
