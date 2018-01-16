package org.vxmlriot.jvoicexml;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvoicexml.DtmfInput;
import org.jvoicexml.Session;
import org.jvoicexml.client.text.TextServer;
import org.jvoicexml.event.ErrorEvent;
import org.jvoicexml.event.JVoiceXMLEvent;
import org.jvoicexml.event.error.BadFetchError;
import org.jvoicexml.event.error.NoresourceError;
import org.jvoicexml.event.plain.ConnectionDisconnectHangupEvent;
import org.jvoicexml.xml.ssml.SsmlDocument;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vxmlriot.jvoicexml.exception.JVoiceXmlErrorEventException;
import org.vxmlriot.jvoicexml.exception.JVoiceXmlInvalidStateException;
import org.vxmlriot.jvoicexml.listener.InputStateListener;
import org.vxmlriot.jvoicexml.listener.ResponseListener;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.vxmlriot.stubs.SsmlDocumentBuilder.ssmlDocument;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextServer.class)
public class CallTest {

    private static final URI VXML_URI = URI.create("http://example.com/start.vxml");

    @Mock private Session session;
    @Mock private TextServer textServer;
    @Mock private ResponseListener responseListener;
    @Mock private InputStateListener inputStateListener;
    @Mock private DtmfInput dtmfInput;
    @InjectMocks private Call call;

    @Before
    public void setUp() throws JVoiceXMLEvent {
        call.responseListener = responseListener;
        call.inputState = inputStateListener;
        when(session.getDtmfInput()).thenReturn(dtmfInput);
    }

    @Test
    public void call_makesACall() throws Exception, ErrorEvent {
        call.call(VXML_URI);
        verify(session).call(VXML_URI);
    }

    @Test
    public void call_resetsResponseState() throws Exception {
        call.call(VXML_URI);
        verify(responseListener).clear();
    }

    @Test(expected = JVoiceXmlErrorEventException.class)
    public void callHasError_exceptionIsThrown() throws Exception, ErrorEvent {
        when(session.call(any(URI.class)))
                .thenThrow(new BadFetchError("Simulated bad fetch error"));
        call.call(VXML_URI);
    }

    @Test
    public void getSsmlResponse_returnsSsmlDocuments() throws Exception {
        final List<SsmlDocument> ssmlDocumentResponses = Arrays.asList(
                ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build(),
                ssmlDocument().withFilename("ssmlTextResponse_goodbye.xml").build()
        );
        when(responseListener.getCapturedResponses()).thenReturn(ssmlDocumentResponses);

        List<SsmlDocument> responses = call.getSsmlResponse();
        assertEquals(ssmlDocumentResponses, responses);
    }

    @Test
    public void enterDtmf_addsAllDtmfDigitsToSessionInput() throws Exception {
        call.enterDtmf("123#*");
        InOrder dtmfInputOrder = inOrder(dtmfInput);
        dtmfInputOrder.verify(dtmfInput).addDtmf('1');
        dtmfInputOrder.verify(dtmfInput).addDtmf('2');
        dtmfInputOrder.verify(dtmfInput).addDtmf('3');
        dtmfInputOrder.verify(dtmfInput).addDtmf('#');
        dtmfInputOrder.verify(dtmfInput).addDtmf('*');
    }

    @Test
    public void enterDtmf_waitsTillInputExpected() throws Exception {
        call.enterDtmf("#");
        InOrder waitThenInput = inOrder(dtmfInput, inputStateListener);
        waitThenInput.verify(inputStateListener).waitUntilReadyForInput();
        waitThenInput.verify(dtmfInput).addDtmf('#');
    }

    @Test
    public void enterDtmf_resetsResponseState() throws Exception {
        call.enterDtmf("#");
        verify(responseListener).clear();
    }

    @Test(expected = JVoiceXmlInvalidStateException.class)
    public void enterDtmfWhenCallIsHungUp_throwsException() throws Exception, JVoiceXMLEvent {
        when(session.getDtmfInput()).thenThrow(new ConnectionDisconnectHangupEvent());
        call.enterDtmf("#");
    }

    @Test(expected = JVoiceXmlErrorEventException.class)
    public void enterDtmfWhenNoResource_throwsException() throws Exception, JVoiceXMLEvent{
        when(session.getDtmfInput()).thenThrow(new NoresourceError());
        call.enterDtmf("#");
    }

    @Test
    public void sayUtterance_sendsAllUtterancesToTextServer() throws Exception {
        call.sendUtterance("One", "Two", "Three");
        InOrder utteranceInputOrder = inOrder(textServer);
        utteranceInputOrder.verify(textServer).sendInput("One");
        utteranceInputOrder.verify(textServer).sendInput("Two");
        utteranceInputOrder.verify(textServer).sendInput("Three");
    }

    @Test
    public void sayUtterance_waitsTillInputExpected() throws Exception {
        call.sendUtterance("Hiya");
        InOrder waitThenInput = inOrder(textServer, inputStateListener);
        waitThenInput.verify(inputStateListener).waitUntilReadyForInput();
        waitThenInput.verify(textServer).sendInput("Hiya");
    }

    @Test
    public void sayUtterance_resetsResponseState() throws Exception {
        call.sendUtterance("Hiya");
        verify(responseListener).clear();
    }

    @Test(expected = JVoiceXmlInvalidStateException.class)
    public void sayUtteranceWhenCallIsHungUp_throwsException() throws Exception {
        doThrow(new IOException("Disconnected. No stream to send (simulated exception)"))
                .when(textServer).sendInput(anyString());
        call.sendUtterance("Hiya");
    }

    @Test
    public void shutdown_stopsTextServer() {
        call.shutdown();
        verify(textServer).stopServer();
    }

    @Test
    public void shutdown_endsSession() {
        call.shutdown();
        verify(session).hangup();
    }
}