package org.vxmlriot.jvoicexml;

import org.apache.log4j.Logger;
import org.jvoicexml.DocumentServer;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.xml.ssml.SsmlDocument;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.exception.CallIsActiveException;
import org.vxmlriot.exception.CallNotActiveException;
import org.vxmlriot.exception.DriverException;
import org.vxmlriot.jvoicexml.exception.JVoiceXmlErrorEventException;
import org.vxmlriot.jvoicexml.exception.JVoiceXmlException;
import org.vxmlriot.jvoicexml.exception.JVoiceXmlStartupException;
import org.vxmlriot.parser.SsmlDocumentParser;
import org.vxmlriot.url.UriBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Drives VXML interactions, implemented by the JVoiceXML library.
 */
public class JVoiceXmlDriver implements VxmlDriver {

    private static final Logger LOGGER = Logger.getLogger(JVoiceXmlDriver.class);

    protected UriBuilder uriBuilder;
    protected CallBuilder callBuilder;
    private SsmlDocumentParser textResponseParser;
    private SsmlDocumentParser audioSrcParser;
    private Call call;

    public JVoiceXmlDriver() {
    }

    public void setUriBuilder(UriBuilder uriBuilder) {
        this.uriBuilder = uriBuilder;
    }

    public void setCallBuilder(CallBuilder callBuilder) {
        this.callBuilder = callBuilder;
    }

    public void setTextResponseParser(SsmlDocumentParser textResponseParser) {
        this.textResponseParser = textResponseParser;
    }

    public void setAudioSrcParser(SsmlDocumentParser audioSrcParser) {
        this.audioSrcParser = audioSrcParser;
    }

    @Override
    public void get(String resource) throws DriverException {

        final URI uri = uriBuilder.build(resource);
        get(uri);
    }

    @Override
    public void get(URI resource) throws DriverException {
        if (callIsActive()) {
            throw new CallIsActiveException("Cannot start a new call - another call is active");
        }

        try {
            call = callBuilder.build();
            call.call(resource);
        } catch (JVoiceXmlStartupException e) {
            throw new DriverException("Failed to start JVoiceXML", e);
        } catch(JVoiceXmlErrorEventException e) {
            throw new DriverException("Failed to make the call", e);
        }
    }

    @Override
    public void enterDtmf(String digits) throws DriverException {
        if (!callIsActive()) {
            throw new CallNotActiveException("Cannot get text response - no call is active");
        }

        try {
            call.enterDtmf(digits);
        } catch (JVoiceXmlException e) {
            throw new DriverException("JVoiceXML driver failed on sending DTMF", e);
        }
    }

    @Override
    public void say(String... utterance) throws DriverException {
        if (!callIsActive()) {
            throw new CallNotActiveException("Cannot get text response - no call is active");
        }

        try {
            call.sendUtterance(utterance);
        } catch (JVoiceXmlException e) {
            throw new DriverException("JVoiceXML driver failed on sending utterance", e);
        }
    }


    @Override
    public void hangup() {
        endCall();
    }

    @Override
    public List<String> getTextResponse() throws DriverException {
        if (!callIsActive()) {
            throw new CallNotActiveException("Cannot get text response - no call is active");
        }

        List<SsmlDocument> responseDocuments = call.getSsmlResponse();
        if (isEmpty(responseDocuments)) {
            throw new DriverException("No response received");
        }
        List<String> textResponse = parseDocuments(responseDocuments, textResponseParser);
        LOGGER.debug("textResponse: " + String.join("|", textResponse));
        return textResponse;
    }

    @Override
    public List<String> getAudioSrc() throws DriverException {
        if (!callIsActive()) {
            throw new CallNotActiveException("Cannot get audio response - no call is active");
        }

        List<SsmlDocument> responseDocuments = call.getSsmlResponse();
        if (isEmpty(responseDocuments)) {
            throw new DriverException("No response received");
        }
        List<String> audioSrc = parseDocuments(responseDocuments, audioSrcParser);
        LOGGER.debug("audioSrc:" + String.join("|", audioSrc));
        return audioSrc;
    }

    @Override
    public void shutdown() {

        endCall();

        JVoiceXmlMain jvxml = callBuilder.getJvxmlMain();

        // Explicitly shutdown document server. JVoiceXML will not do this for us.
        DocumentServer documentServer = jvxml.getDocumentServer();
        if (documentServer != null) {
            documentServer.stop();
        }

        // Shutdown interpreter
        jvxml.shutdown();
        jvxml.waitShutdownComplete();
        preventJvmTermination();
    }

    private void endCall() {
        if (call != null) {
            call.shutdown();
        }
        call = null;
    }

    private boolean callIsActive() {
        return call != null;
    }

    private List<String> parseDocuments(List<SsmlDocument> responseDocuments,
                                        SsmlDocumentParser parser) {
        return responseDocuments.stream()
                .map(parser::parse)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Default behaviour is for TerminationThread to stop the entire JVM
     * 10s after shutdown request received.
     * This will break unit tests if they're still running so we need to kill
     * the thread.
     */
    private void preventJvmTermination() {

        // Find the root thread group
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        while (threadGroup.getParent() != null) {
            threadGroup = threadGroup.getParent();
        }

        // Find the thread named TerminationThread
        Thread[] threads = new Thread[1024];
        int numThreads = threadGroup.enumerate(threads);
        if (numThreads > 1024) {
            LOGGER.warn("Number of threads exceeds array size.");
        }
        Optional<Thread> terminationThread = Arrays.stream(threads)
                .filter(Objects::nonNull)
                .filter(t -> t.getName().equals("TerminationThread"))
                .findAny();

        if (terminationThread.isPresent()) {
            // Interrupting the shutdown thread is enough to prevent it exiting the JVM
            LOGGER.info("Interrupting the TerminationThread to prevent premature JVM exit");
            terminationThread.get().interrupt();
        } else {
            LOGGER.warn("TerminationThread not found. JVoiceXML may still attempt to exit JVM.");
        }
    }

}
