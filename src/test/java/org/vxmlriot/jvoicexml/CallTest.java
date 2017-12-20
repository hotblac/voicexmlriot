package org.vxmlriot.jvoicexml;

import org.junit.Before;
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
import org.vxmlriot.jvoicexml.listener.ResponseListener;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextServer.class)
public class CallTest {

    private static final URI VXML_URI = URI.create("http://example.com/start.vxml");

    @Mock private Session session;
    @Mock private TextServer textServer;
    @Mock private ResponseListener responseListener;
    @InjectMocks private Call call;

    @Before
    public void setUp() {
        call.responseListener = responseListener;
    }

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

        final List<SsmlDocument> ssmlDocumentResponses = Arrays.asList(
                getSsmlDocument("ssmlTextResponse_helloWorld.xml"),
                getSsmlDocument("ssmlTextResponse_goodbye.xml")
        );
        when(responseListener.getCapturedResponses()).thenReturn(ssmlDocumentResponses);

        List<SsmlDocument> responses = call.getSsmlResponse();
        assertEquals(ssmlDocumentResponses, responses);
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