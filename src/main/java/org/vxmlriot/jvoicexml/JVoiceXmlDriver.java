package org.vxmlriot.jvoicexml;

import org.apache.commons.lang3.StringUtils;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.jvoicexml.DocumentServer;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.xml.ssml.Speak;
import org.jvoicexml.xml.ssml.SsmlDocument;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.exception.CallIsActiveException;
import org.vxmlriot.exception.CallNotActiveException;
import org.vxmlriot.exception.DriverException;
import org.vxmlriot.jvoicexml.exception.JVoiceXmlErrorEventException;
import org.vxmlriot.jvoicexml.exception.JvoiceXmlStartupException;
import org.vxmlriot.url.UriBuilder;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Drives VXML interactions, implemented by the JVoiceXML library.
 */
public class JVoiceXmlDriver implements VxmlDriver {

    protected UriBuilder uriBuilder;
    protected CallBuilder callBuilder;
    private Call call;

    public JVoiceXmlDriver() {
    }

    public void setUriBuilder(UriBuilder uriBuilder) {
        this.uriBuilder = uriBuilder;
    }

    public void setCallBuilder(CallBuilder callBuilder) {
        this.callBuilder = callBuilder;
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
        } catch (JvoiceXmlStartupException e) {
            throw new DriverException("Failed to start JVoiceXML", e);
        } catch(JVoiceXmlErrorEventException e) {
            throw new DriverException("Failed to make the call", e);
        }
    }

    @Override
    public void enterDtmf(String digits) {

    }

    @Override
    public void say(String... utterance) {

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
        return parseTextResponses(responseDocuments);
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
        return parseAudioResponses(responseDocuments);
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

    private List<String> parseTextResponses(List<SsmlDocument> documents) {
        return documents.stream()
                .map(this::textContent)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    private List<String> parseAudioResponses(List<SsmlDocument> documents) {
        return documents.stream()
                .map(this::audioSrc)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    private String textContent(SsmlDocument document) {
        try {
            return document.getSpeak().getTextContent();
        } catch (Exception e) {
            // Provided parse methods on SsmlDocument are prone to NPE.
            // Interpret these as invalid SSML and ignore
            return null;
        }
    }

    private String audioSrc(SsmlDocument document) {
        try {
            NamespaceContextImpl ns = new NamespaceContextImpl();
            ns.startPrefixMapping("ssml", Speak.DEFAULT_XMLNS);

            final XPathFactory xpathFactory = XPathFactory.newInstance();
            final XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(ns);

            return (String) xpath.evaluate("/ssml:speak/ssml:audio/@src", document.getDocument(), XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Failed to parse SsmlDocument", e);
        }
    }
}
