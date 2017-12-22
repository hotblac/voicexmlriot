package org.vxmlriot.jvoicexml.exception;

/**
 * Exception on failure of JVoiceXml framework
 */
public class JVoiceXmlException extends Exception {
    public JVoiceXmlException(String message) {
        super(message);
    }

    public JVoiceXmlException(String message, Throwable cause) {
        super(message, cause);
    }
}
