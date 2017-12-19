package org.vxmlriot.jvoicexml.exception;

/**
 * Exception on failure of JVoiceXml framework
 */
public class JvoiceXmlException extends Exception {
    public JvoiceXmlException(String message) {
        super(message);
    }

    public JvoiceXmlException(String message, Throwable cause) {
        super(message, cause);
    }
}
