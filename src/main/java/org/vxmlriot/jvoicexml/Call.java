package org.vxmlriot.jvoicexml;

import org.apache.log4j.Logger;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.Session;
import org.jvoicexml.SessionListener;
import org.jvoicexml.client.text.TextServer;
import org.jvoicexml.event.ErrorEvent;
import org.jvoicexml.xml.ssml.SsmlDocument;
import org.vxmlriot.jvoicexml.exception.JVoiceXmlErrorEventException;
import org.vxmlriot.jvoicexml.listener.TextListenerAdapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
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

    protected ResponseListener responseListener = new ResponseListener();


    public Call(Session session, TextServer textServer) {
        this.session = session;
        this.textServer = textServer;
        session.addSessionListener(new SessionEndListener());
    }

    /**
     * Call the VXML application at given URI
     * @param uri of the VXML application
     * @throws JVoiceXmlErrorEventException on JVoiceXML error event
     */
    public void call(URI uri) throws JVoiceXmlErrorEventException {
        try {
            session.call(uri);
        } catch (ErrorEvent errorEvent) {
            throw new JVoiceXmlErrorEventException(errorEvent);
        }
    }

    /**
     * Get the SSML (speech synthesis) response to the last request.
     * This method will block until all responses are received.
     * @return List of SsmlDocuments. A single VXML page with multiple
     *         speec sections will return multiple responses.
     */
    public List<SsmlDocument> getSsmlResponse() {
        return responseListener.getCapturedResponses();
    }

    public void shutdown() {
        textServer.stopServer();

        // Kludge! The TextServer port remains in use after shutdown.
        // Wait for it to clear
        try {
            sleep(500);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for TextServer socket to clear", e);
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


    class ResponseListener extends TextListenerAdapter {

        private final List<SsmlDocument> capturedResponses = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void outputSsml(SsmlDocument document) {
            capturedResponses.add(document);
        }

        List<SsmlDocument> getCapturedResponses() {
            synchronized (capturedResponses) {
                // Return a copy of the list to prevent concurrent modification
                return new ArrayList<>(capturedResponses);
            }
        }
    }
}
