package org.vxmlriot.jvoicexml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
/**
 * Build instances of JVoiceXmlDriver.
 * Defaults are provided for dependent objects and can be overridden using the Builder pattern.
 */
public class JVoiceXmlDriverBuilder implements VxmlDriverBuilder {

    private static final Logger LOGGER = LogManager.getLogger(JVoiceXmlDriverBuilder.class);

    static final String DEFAULT_CONFIG_ROOT = "jvoicexml.xml";

    /**
     * Default repository files to be loaded as classpath resources.
     */
    static final List<String> DEFAULT_REPOSITORY_FILES = Arrays.asList(
            "ecmascript-datamodel.xml",
            "jvxml-grammar.xml",
            "text-implementation.xml",
            "vxml2.1-profile.xml"
    );

    private Configuration config = null;
    private String configRoot = DEFAULT_CONFIG_ROOT;
    private List<String> repositoryFiles = DEFAULT_REPOSITORY_FILES;
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
     * Configure JVoiceXML using an alternative JVoiceXML config file.
     * @param configRoot name of config file, relative to classpath
     * @return this builder for method chaining
     */
    public JVoiceXmlDriverBuilder configRoot(String configRoot) {
        this.configRoot = configRoot;
        return this;
    }

    /**
     * Configure JVoiceXML using an alternative set of repository files.
     * @param repositoryFiles names of repository files, relative to classpath
     * @return this builder for method chaining
     */
    public JVoiceXmlDriverBuilder repositoryFiles(List<String> repositoryFiles) {
        this.repositoryFiles = repositoryFiles;
        return this;
    }

    /**
     * Use a custom UriBuilder implementation. This is used to customise
     * resolution of VoiceXML page URLs.
     * @param uriBuilder
     * @return this builder for method chaining
     */
    public JVoiceXmlDriverBuilder uriBuilder(UriBuilder uriBuilder) {
        this.uriBuilder = uriBuilder;
        return this;
    }

    /**
     * Use a custom set of event delays.
     * @param delays
     * @return this builder for method chaining
     */
    public JVoiceXmlDriverBuilder eventDelays(EventDelay delays) {
        this.delays = delays;
        return this;
    }

    public JVoiceXmlDriver build() {

        if (config == null) {
            config = new JVoiceXmlConfiguration(configRoot, repositoryFiles);
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

    private static File resourceAsFile(String resourceName) {
        final URL resource = JVoiceXmlDriverBuilder.class.getClassLoader().getResource(resourceName);
        try {
            return new File(resource.toURI());
        } catch (URISyntaxException | IllegalArgumentException e) {
            LOGGER.warn("Failed to parse resource URL " + resource);
            return new File(resource.getFile());

        }
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
