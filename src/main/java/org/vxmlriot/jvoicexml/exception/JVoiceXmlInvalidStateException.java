package org.vxmlriot.jvoicexml.exception;

/**
 * Thrown when JVoiceXML is not in the correct state to process a request.
 * For example, attempting operations on a call that's already ended
 */
public class JVoiceXmlInvalidStateException extends JVoiceXmlException {
    public JVoiceXmlInvalidStateException(String message) {
        super(message);
    }

    public JVoiceXmlInvalidStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
