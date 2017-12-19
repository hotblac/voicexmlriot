package org.vxmlriot.driver;

import org.vxmlriot.exception.DriverException;
import org.vxmlriot.jvoicexml.Call;
import org.vxmlriot.jvoicexml.CallBuilder;
import org.vxmlriot.jvoicexml.exception.JvoiceXmlStartupException;
import org.vxmlriot.url.UriBuilder;

import java.net.URI;
import java.util.List;

/**
 * Drives VXML interactions, implemented by the JVoiceXML library.
 */
public class JvoiceXmlDriver implements VxmlDriver {

    protected UriBuilder uriBuilder;
    protected CallBuilder callBuilder;
    private Call call;

    @Override
    public void get(String resource) throws DriverException {
        final URI uri = uriBuilder.build(resource);
        try {
            call = callBuilder.build();
            call.call(uri);
        } catch (JvoiceXmlStartupException e) {
            throw new DriverException("Failed to start JVoiceXML", e);
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
        if (call != null) {
            call.shutdown();
        }
    }

    @Override
    public List<String> getTextResponse() {
        return null;
    }

    @Override
    public List<String> getAudioSrc() {
        return null;
    }
}
