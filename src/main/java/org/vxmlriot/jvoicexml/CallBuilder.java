package org.vxmlriot.jvoicexml;

import org.apache.log4j.Logger;
import org.jvoicexml.*;
import org.jvoicexml.client.text.TextServer;
import org.jvoicexml.event.ErrorEvent;
import org.vxmlriot.jvoicexml.exception.JvoiceXmlStartupException;

import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

/**
 * Build a new call
 */
public class CallBuilder {

    private static final Logger LOGGER = Logger.getLogger(CallBuilder.class);
    static final int TEXT_SERVER_PORT = 4242;

    private Configuration config;
    private JVoiceXmlStartupListener startupListener = new JVoiceXmlStartupListener();

    public void setConfig(Configuration config) {
        this.config = config;
    }

    /**
     * Create a new JVoiceXML call
     * @return a valid, built instance of JvoiceXML Call
     * @throws JvoiceXmlStartupException on failure to start the call session
     */
    public Call build() throws JvoiceXmlStartupException {

        final JVoiceXmlMain jvxmlMain = startJvxmlInterpreter();
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

    private synchronized JVoiceXmlMain startJvxmlInterpreter() {
        final JVoiceXmlMain jvxml = new JVoiceXmlMain(config);

        startupListener.reset();
        jvxml.addListener(startupListener);
        jvxml.start();
        startupListener.waitForStartup();

        return jvxml;
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


    public class JVoiceXmlStartupListener implements JVoiceXmlMainListener {

        private CountDownLatch startupLatch = new CountDownLatch(1);

        void reset() {
            startupLatch = new CountDownLatch(1);
        }

        void waitForStartup() {
            try {
                startupLatch.await();
            } catch (InterruptedException e) {
                LOGGER.warn("Interrupted on waiting for JVoiceXML to start", e);
            }
        }

        @Override
        public void jvxmlStarted() {
            startupLatch.countDown();
        }

        @Override
        public void jvxmlTerminated() {
        }

        @Override
        public void jvxmlStartupError(final Throwable exception) {
            LOGGER.error("error starting JVoiceXML", exception);
            startupLatch.countDown(); // cancel
        }
    }

}
