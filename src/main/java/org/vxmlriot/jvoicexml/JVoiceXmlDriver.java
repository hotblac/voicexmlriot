package org.vxmlriot.jvoicexml;

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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Drives VXML interactions, implemented by the JVoiceXML library.
 */
public class JVoiceXmlDriver implements VxmlDriver {

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

        if (callIsActive()) {
            throw new CallIsActiveException("Cannot start a new call - another call is active");
        }

        final URI uri = uriBuilder.build(resource);
        try {
            call = callBuilder.build();
            call.call(uri);
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
        return parseDocuments(responseDocuments, textResponseParser);
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
        return parseDocuments(responseDocuments, audioSrcParser);
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

}
