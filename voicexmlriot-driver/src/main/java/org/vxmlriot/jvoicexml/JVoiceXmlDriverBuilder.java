package org.vxmlriot.jvoicexml;

import org.apache.log4j.Logger;
import org.jvoicexml.Configuration;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.JVoiceXmlMainListener;
import org.jvoicexml.config.JVoiceXmlConfiguration;
import org.vxmlriot.driver.EventDelay;
import org.vxmlriot.driver.VxmlDriverBuilder;
import org.vxmlriot.parser.AudioSrcResponseParser;
import org.vxmlriot.parser.TextResponseParser;
import org.vxmlriot.url.ClasspathFileUriBuilder;
import org.vxmlriot.url.UriBuilder;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * Build instances of JVoiceXmlDriver.
 * Defaults are provided for dependent objects and can be overridden using the Builder pattern.
 */
public class JVoiceXmlDriverBuilder implements VxmlDriverBuilder {

    private static final Logger LOGGER = Logger.getLogger(JVoiceXmlDriverBuilder.class);

    /**
     * System property used by JVoiceXML to locate the config directory
     */
    static final String PROPERTY_CONFIG_DIR = "jvoicexml.config";

    private Configuration config = null;
    private String confDir = null;
    private UriBuilder uriBuilder = new ClasspathFileUriBuilder();
    private JVoiceXmlStartupListener startupListener = new JVoiceXmlStartupListener();
    private EventDelay delays = defaultEventDelays();

    /**
     * Configure JVoiceXML from a custom Configuration class
     * @param config Implementation of Configuration interface
     * @return this builder for method chaining
     */
    public JVoiceXmlDriverBuilder config(Configuration config) {
        this.config = config;
        return this;
    }

    /**
     * Configure JVoiceXML from an XML config file.
     * @param confDir Directory containing jvoicexml.xml configuration file
     * @return this builder for method chaining
     */
    public JVoiceXmlDriverBuilder config(String confDir) {
        this.confDir = confDir;
        return this;
    }

    public JVoiceXmlDriverBuilder uriBuilder(UriBuilder uriBuilder) {
        this.uriBuilder = uriBuilder;
        return this;
    }

    public JVoiceXmlDriverBuilder eventDelays(EventDelay delays) {
        this.delays = delays;
        return this;
    }

    public JVoiceXmlDriver build() {

        if (config == null) {
            // Use default JVoiceXML XML based config
            if (confDir == null) {
                // Use classpath resources
                final File configFile = new File(JVoiceXmlDriverBuilder.class.getClassLoader().getResource("jvoicexml.xml").getFile());
                confDir = configFile.getParent();
            }
            System.setProperty(PROPERTY_CONFIG_DIR, confDir);
            config = new JVoiceXmlConfiguration();
        }

        final JVoiceXmlMain jvxmlMain = startJvxmlInterpreter();

        final CallBuilder callBuilder = new CallBuilder();
        callBuilder.setJvxmlMain(jvxmlMain);

        JVoiceXmlDriver driver = new JVoiceXmlDriver();
        driver.setCallBuilder(callBuilder);
        driver.setUriBuilder(uriBuilder);
        driver.setTextResponseParser(new TextResponseParser());
        driver.setAudioSrcParser(new AudioSrcResponseParser());
        driver.setDelays(delays);
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

    private static EventDelay defaultEventDelays() {
        EventDelay delays = new EventDelay();
        delays.setInputDelay(500);
        delays.setResponseDelay(0);
        delays.setCallClearDelay(0);
        return delays;
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
