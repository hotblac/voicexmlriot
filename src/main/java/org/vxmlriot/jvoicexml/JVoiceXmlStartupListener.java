package org.vxmlriot.jvoicexml;

import org.apache.log4j.Logger;
import org.jvoicexml.JVoiceXmlMainListener;

import java.util.concurrent.CountDownLatch;

public class JVoiceXmlStartupListener implements JVoiceXmlMainListener {

    private static final Logger LOGGER = Logger.getLogger(JVoiceXmlStartupListener.class);

    private CountDownLatch startupLatch = new CountDownLatch(1);

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

    public void waitForStartup() {
        try {
            startupLatch.await();
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted on waiting for JVoiceXML to start", e);
        }
    }

}
