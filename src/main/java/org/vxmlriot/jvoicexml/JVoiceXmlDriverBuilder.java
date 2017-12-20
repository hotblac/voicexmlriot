package org.vxmlriot.jvoicexml;

import org.apache.log4j.Logger;
import org.jvoicexml.Configuration;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.JVoiceXmlMainListener;
import org.vxmlriot.url.ClasspathFileUriBuilder;
import org.vxmlriot.url.UriBuilder;

import java.util.concurrent.CountDownLatch;

/**
 * Build instances of JVoiceXmlDriver.
 * Defaults are provided for dependent objects and can be overridden using the Builder pattern.
 */
public class JVoiceXmlDriverBuilder {

    private static final Logger LOGGER = Logger.getLogger(JVoiceXmlDriverBuilder.class);

    private Configuration config = new EmbeddedTextConfiguration();
    private UriBuilder uriBuilder = new ClasspathFileUriBuilder();
    private JVoiceXmlStartupListener startupListener = new JVoiceXmlStartupListener();

    public JVoiceXmlDriverBuilder config(Configuration config) {
        this.config = config;
        return this;
    }

    public JVoiceXmlDriverBuilder uriBuilder(UriBuilder uriBuilder) {
        this.uriBuilder = uriBuilder;
        return this;
    }

    public JVoiceXmlDriver build() {

        final JVoiceXmlMain jvxmlMain = startJvxmlInterpreter();

        final CallBuilder callBuilder = new CallBuilder();
        callBuilder.setJvxmlMain(jvxmlMain);

        JVoiceXmlDriver driver = new JVoiceXmlDriver();
        driver.setCallBuilder(callBuilder);
        driver.setUriBuilder(uriBuilder);
        return driver;
    }

    private synchronized JVoiceXmlMain startJvxmlInterpreter() {
        final JVoiceXmlMain jvxml = new JVoiceXmlMain(config);

        startupListener.reset();
        jvxml.addListener(startupListener);
        jvxml.start();
        startupListener.waitForStartup();

        return jvxml;
    }

    class JVoiceXmlStartupListener implements JVoiceXmlMainListener {

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
