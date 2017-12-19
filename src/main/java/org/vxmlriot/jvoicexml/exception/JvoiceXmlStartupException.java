package org.vxmlriot.jvoicexml.exception;

/**
 * Exception on failure to start JVoiceXML framework or required component
 */
public class JvoiceXmlStartupException extends JvoiceXmlException {
    public JvoiceXmlStartupException(String message) {
        super(message);
    }

    public JvoiceXmlStartupException(String message, Throwable cause) {
        super(message, cause);
    }
}
