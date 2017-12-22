package org.vxmlriot.jvoicexml;

import org.apache.log4j.Logger;
import org.jvoicexml.DtmfInput;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.Session;
import org.jvoicexml.SessionListener;
import org.jvoicexml.client.text.TextServer;
import org.jvoicexml.event.ErrorEvent;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.event.plain.ConnectionDisconnectHangupEvent;
import org.jvoicexml.xml.ssml.SsmlDocument;
import org.vxmlriot.jvoicexml.exception.JVoiceXmlErrorEventException;
import org.vxmlriot.jvoicexml.exception.JVoiceXmlException;
import org.vxmlriot.jvoicexml.exception.JVoiceXmlInvalidStateException;
import org.vxmlriot.jvoicexml.listener.InputStateListener;
import org.vxmlriot.jvoicexml.listener.ResponseListener;

import java.net.URI;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * A call session
 */
public class Call {

    private static final Logger LOGGER = Logger.getLogger(Call.class);

    /**
     * JVoiceXml interpreter
     */
    private JVoiceXmlMain jvxml;

    /**
     * The active JVoiceXml session.
     * */
    protected Session session;

    /**
     * Socket server to stream text from and to the JVoiceXML interpreter
     */
    protected TextServer textServer;

    protected ResponseListener responseListener;

    protected InputStateListener inputState;


    public Call(Session session, TextServer textServer) {
        this.session = session;
        this.textServer = textServer;
        session.addSessionListener(new SessionEndListener());
    }

    public void setResponseListener(ResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    public void setInputStateListener(InputStateListener inputState) {
        this.inputState = inputState;
    }

    /**
     * Call the VXML application at given URI
     * @param uri of the VXML application
     * @throws JVoiceXmlErrorEventException on JVoiceXML error event
     */
    public void call(URI uri) throws JVoiceXmlErrorEventException {
        try {
            responseListener.clear();
            session.call(uri);
        } catch (ErrorEvent errorEvent) {
            throw new JVoiceXmlErrorEventException(errorEvent);
        }
    }

    /**
     * Get the SSML (speech synthesis) response to the last request.
     * This method will block until all responses are received.
     * @return List of SsmlDocuments. A single VXML page with multiple
     *         speech sections will return multiple responses.
     */
    public List<SsmlDocument> getSsmlResponse() {
        return responseListener.getCapturedResponses();
    }

    public void enterDtmf(String digits) throws JVoiceXmlException {
        inputState.waitUntilReadyForInput();
        responseListener.clear();
        try {
            DtmfInput input = session.getDtmfInput();
            for (char digit : digits.toCharArray()) {
                LOGGER.debug("Entering digit: " + digit);
                input.addDtmf(digit);
            }
        } catch (NoresourceError noresourceError) {
            throw new JVoiceXmlErrorEventException(noresourceError);
        } catch (ConnectionDisconnectHangupEvent connectionDisconnectHangupEvent) {
            throw new JVoiceXmlInvalidStateException("Cannot enter DTMF: call is disconnected", connectionDisconnectHangupEvent);
        }
    }

    public void shutdown() {
        shutdownTextServer();
        shutdownSession();
    }

    private void shutdownTextServer() {
        textServer.stopServer();

        // Kludge! The TextServer port remains in use after shutdown.
        // Wait for it to clear
        try {
            sleep(500);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for TextServer socket to clear", e);
        }
    }

    private void shutdownSession() {
        if (!session.hasEnded()) {
            try {
                session.hangup();
                session.waitSessionEnd();
            } catch (ErrorEvent errorEvent) {
                LOGGER.warn("Error event while waiting for session to end: " + errorEvent.getEventType(), errorEvent);
            }
        }
    }

    class SessionEndListener implements SessionListener {

        @Override
        public void sessionStarted(Session session) {}

        @Override
        public void sessionEnded(Session session) {
            LOGGER.debug("Session ended");
            shutdown();
        }
    }
}
