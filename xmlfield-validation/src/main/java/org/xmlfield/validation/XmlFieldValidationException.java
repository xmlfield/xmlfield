package org.xmlfield.validation;

import org.xmlfield.core.exception.XmlFieldException;

public class XmlFieldValidationException extends XmlFieldException {

    String methodName;
    String expected;
    String actual;
    
    public XmlFieldValidationException(String methodName, String expected, String actual) {
        super( "Validation failed on "+methodName+". Expected: "+expected +" Actual: " +actual);
        this.methodName = methodName;
        this.expected = expected ;
        this.actual = actual;
        
        
    }
 
}
