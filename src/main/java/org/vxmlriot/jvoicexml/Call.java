package org.vxmlriot.jvoicexml;

import org.apache.log4j.Logger;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.Session;
import org.jvoicexml.SessionListener;
import org.jvoicexml.client.text.TextServer;

import java.net.URI;

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


    public Call(Session session, TextServer textServer) {
        this.session = session;
        this.textServer = textServer;
        session.addSessionListener(new SessionEndListener());
    }

    /**
     * Call the VXML application at given URI
     * @param uri of the VXML application
     */
    public void call(URI uri) {

    }

    public void shutdown() {
        textServer.stopServer();
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
