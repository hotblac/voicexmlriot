package org.vxmlriot.driver;

import org.vxmlriot.jvoicexml.Call;
import org.vxmlriot.jvoicexml.CallBuilder;
import org.vxmlriot.url.UriBuilder;

import java.net.URI;
import java.util.List;

/**
 * Drives VXML interactions, implemented by the JVoiceXML library.
 */
public class JVoiceXmlDriver implements VxmlDriver {

    private UriBuilder uriBuilder;
    private CallBuilder callBuilder;
    private Call call;

    @Override
    public void get(String resource) {
        final URI uri = uriBuilder.build(resource);
        call = callBuilder.build();
        call.call(uri);
    }

    @Override
    public void enterDtmf(String digits) {

    }

    @Override
    public void say(String... utterance) {

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
