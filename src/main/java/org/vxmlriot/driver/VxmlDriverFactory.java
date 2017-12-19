package org.vxmlriot.driver;

import org.apache.log4j.Logger;
import org.jvoicexml.Configuration;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.JVoiceXmlMainListener;
import org.vxmlriot.jvoicexml.CallBuilder;
import org.vxmlriot.jvoicexml.EmbeddedTextConfiguration;
import org.vxmlriot.url.ClasspathFileUriBuilder;

import java.util.concurrent.CountDownLatch;

/**
 * Factory to create a VxmlDriver instance
 */
public class VxmlDriverFactory {

    private static final Logger LOGGER = Logger.getLogger(VxmlDriverFactory.class);

    private Configuration config = new EmbeddedTextConfiguration();
    private JVoiceXmlStartupListener startupListener = new JVoiceXmlStartupListener();

    public static VxmlDriver getDriver() {
        // For now, only the JVoiceXML implementation exists. Build it with default config.
        return new VxmlDriverFactory().createJvoiceXmlDriver();
    }


    protected JvoiceXmlDriver createJvoiceXmlDriver() {
        JvoiceXmlDriver driver = new JvoiceXmlDriver();

        final JVoiceXmlMain jvxmlMain = startJvxmlInterpreter();

        final CallBuilder callBuilder = new CallBuilder();
        callBuilder.setJvxmlMain(jvxmlMain);
        driver.callBuilder = callBuilder;

        driver.uriBuilder = new ClasspathFileUriBuilder();
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
