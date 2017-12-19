package org.vxmlriot.jvoicexml;

import org.apache.log4j.Logger;
import org.jvoicexml.*;
import org.jvoicexml.client.text.TextServer;
import org.jvoicexml.event.ErrorEvent;
import org.vxmlriot.jvoicexml.exception.JvoiceXmlStartupException;

import java.net.UnknownHostException;

/**
 * Build a new call
 */
public class CallBuilder {

    private static final Logger LOGGER = Logger.getLogger(CallBuilder.class);
    static final int TEXT_SERVER_PORT = 4242;

    protected JVoiceXmlMain jvxmlMain;

    public void setJvxmlMain(JVoiceXmlMain jvxmlMain) {
        this.jvxmlMain = jvxmlMain;
    }

    /**
     * Create a new JVoiceXML call
     * @return a valid, built instance of JvoiceXML Call
     * @throws JvoiceXmlStartupException on failure to start the call session
     */
    public Call build() throws JvoiceXmlStartupException {

        final TextServer textServer = startTextServer();

        try {
            final ConnectionInformation info = textServer.getConnectionInformation();
            final Session session = jvxmlMain.createSession(info);
            return new Call(session, textServer);
        } catch (UnknownHostException e) {
            throw new JvoiceXmlStartupException("Error connecting to TextServer", e);
        } catch (ErrorEvent errorEvent) {
            throw new JvoiceXmlStartupException("Error starting JVoiceXML interpreter", errorEvent);
        }
    }

    private synchronized TextServer startTextServer() {
        final TextServer textServer = new TextServer(TEXT_SERVER_PORT);
        textServer.start();
        try {
            LOGGER.debug("Waiting for TextServer startup");
            textServer.waitStarted();
            LOGGER.debug("TextServer started");
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted during TextServer startup");
        }
        return textServer;
    }

}
