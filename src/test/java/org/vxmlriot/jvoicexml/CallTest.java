package org.vxmlriot.jvoicexml;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvoicexml.Session;
import org.jvoicexml.client.text.TextServer;
import org.jvoicexml.event.ErrorEvent;
import org.jvoicexml.event.error.BadFetchError;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vxmlriot.jvoicexml.exception.JVoiceXmlErrorEventException;

import java.net.URI;

import static org.junit.Assert.fail;
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
    @Ignore("TODO")
    public void getLastResponse_returnsSsmlDocument() {
        call.getLastResponse();
        fail("test not complete");
    }

    @Test
    public void shutdown_stopsTextServer() {
        call.shutdown();
        verify(textServer).stopServer();
    }
}