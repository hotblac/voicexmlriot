package org.vxmlriot.jvoicexml;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvoicexml.Session;
import org.jvoicexml.client.text.TextServer;
import org.jvoicexml.event.ErrorEvent;
import org.jvoicexml.event.error.BadFetchError;
import org.jvoicexml.xml.ssml.SsmlDocument;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vxmlriot.jvoicexml.exception.JVoiceXmlErrorEventException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextServer.class)
public class CallTest {

    private static final URI VXML_URI = URI.create("http://example.com/start.vxml");

    @Mock private Session session;
    @Mock private TextServer textServer;
    @InjectMocks private Call call;

    @Test
    public void call_makesACall() throws Exception, ErrorEvent {
        call.call(VXML_URI);
        verify(session).call(VXML_URI);
    }

    @Test(expected = JVoiceXmlErrorEventException.class)
    public void callHasError_exceptionIsThrown() throws Exception, ErrorEvent {
        when(session.call(any(URI.class)))
                .thenThrow(new BadFetchError("Simulated bad fetch error"));
        call.call(VXML_URI);
    }

    @Test
    public void getSsmlResponse_returnsSsmlDocuments() throws Exception {

        call.call(VXML_URI);

        final SsmlDocument response1 = getSsmlDocument("ssmlTextResponse_helloWorld.xml");
        final SsmlDocument response2 = getSsmlDocument("ssmlTextResponse_goodbye.xml");
        call.responseListener.outputSsml(response1);
        call.responseListener.outputSsml(response2);

        List<SsmlDocument> responses = call.getSsmlResponse();
        assertThat(responses, contains(response1, response2));
    }

    @Test
    public void shutdown_stopsTextServer() {
        call.shutdown();
        verify(textServer).stopServer();
    }

    private SsmlDocument getSsmlDocument(String filename) throws ParserConfigurationException, SAXException, IOException {
        final InputStream vxmlInput = getClass().getClassLoader().getResourceAsStream(filename);
        final InputSource vxmlSource = new InputSource(vxmlInput);
        return new SsmlDocument(vxmlSource);
    }
}