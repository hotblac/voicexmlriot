package org.vxmlriot.jvoicexml.exception;

/**
 * Exception on failure to start JVoiceXML framework or required component
 */
public class JVoiceXmlStartupException extends JVoiceXmlException {
    public JVoiceXmlStartupException(String message) {
        super(message);
    }

    public JVoiceXmlStartupException(String message, Throwable cause) {
        super(message, cause);
    }
}
