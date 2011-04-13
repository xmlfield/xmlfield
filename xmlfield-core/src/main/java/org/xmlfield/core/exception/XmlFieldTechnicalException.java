package org.xmlfield.core.exception;

/**
 * Technical exception
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 * 
 */
public class XmlFieldTechnicalException extends RuntimeException {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = -2132491428040189255L;

    public XmlFieldTechnicalException() {
        super();
    }

    public XmlFieldTechnicalException(String message) {
        super(message);
    }

    public XmlFieldTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public XmlFieldTechnicalException(Throwable cause) {
        super(cause);
    }

}
