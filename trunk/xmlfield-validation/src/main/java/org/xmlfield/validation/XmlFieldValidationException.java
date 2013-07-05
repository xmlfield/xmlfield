package org.xmlfield.validation;

import org.xmlfield.core.exception.XmlFieldException;

public class XmlFieldValidationException extends XmlFieldException {

    private static final long serialVersionUID = 1L;
    String methodName;
    String expected;
    String actual;

    public XmlFieldValidationException(String methodName, String expected, String actual) {
        super("Validation failed on " + methodName + ". Expected: " + expected + " Actual: " + actual);
        this.methodName = methodName;
        this.expected = expected;
        this.actual = actual;

    }

}
