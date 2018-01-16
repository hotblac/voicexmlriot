package org.vxmlriot.jvoicexml;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvoicexml.DocumentServer;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.event.error.BadFetchError;
import org.jvoicexml.xml.ssml.SsmlDocument;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
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
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.vxmlriot.stubs.SsmlDocumentBuilder.ssmlDocument;


/**
 * Tests for the JVoiceXML implementation of VxmlDriver
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JVoiceXmlMain.class)
public class JVoiceXmlDriverTest {

    private static final String START = "http://example.com/START.vxml";
    private static final URI START_URI = URI.create(START);

    private Call call;
    @Mock private UriBuilder uriBuilder;
    @Mock private CallBuilder callBuilder;
    @Mock(name = "textResponseParser") private SsmlDocumentParser textResponseParser;
    @Mock(name = "audioSrcParser") private SsmlDocumentParser audioSrcParser;
    @Mock private JVoiceXmlMain jvxml;
    @Mock private DocumentServer documentServer;
    @InjectMocks private JVoiceXmlDriver driver;

    @Before
    public void setUp() throws Exception {
        call = mock(Call.class);
        when(uriBuilder.build(START)).thenReturn(START_URI);
        when(callBuilder.build()).thenReturn(call);
        when(callBuilder.getJvxmlMain()).thenReturn(jvxml);
        when(jvxml.getDocumentServer()).thenReturn(documentServer);
    }

    @After
    public void tearDown() {
        driver.hangup();
    }

    @Test
    public void get_startsNewCall() throws Exception {
        driver.get(START);
        verify(callBuilder).build();
    }

    @Test
    public void get_requestsPageByUri() throws Exception {
        driver.get(START);
        verify(call).call(START_URI);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getInvalidUriString_throwsException() throws Exception {
        when(uriBuilder.build("invalidUri"))
                .thenThrow(new IllegalArgumentException("Simulated invalid URI failure"));
        driver.get("invalidUri");
    }

    @Test(expected = DriverException.class)
    public void getStartupFailure_throwsException() throws Exception {
        when(callBuilder.build())
                .thenThrow(new JVoiceXmlStartupException("Simulated JVoiceXml failure"));
        driver.get(START);
    }

    @Test(expected = DriverException.class)
    public void getCallFailure_throwsException() throws Exception {
        doThrow(new JVoiceXmlErrorEventException(new BadFetchError("Simulated call failure")))
                .when(call).call(any(URI.class));
        driver.get(START);
    }

    @Test(expected = CallIsActiveException.class)
    public void getWhileAnotherCallIsActive_throwsException() throws Exception {
        driver.get(START);
        driver.get(START);
    }

    @Test
    public void getTextResponse_returnsAllResponses() throws Exception {

        SsmlDocument ssmlHelloWorld = ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build();
        SsmlDocument ssmlGoodbye = ssmlDocument().withFilename("ssmlTextResponse_goodbye.xml").build();
        when(call.getSsmlResponse()).thenReturn(Arrays.asList(ssmlHelloWorld, ssmlGoodbye));
        when(textResponseParser.parse(ssmlHelloWorld)).thenReturn(Optional.of("Hello World!"));
        when(textResponseParser.parse(ssmlGoodbye)).thenReturn(Optional.of("Goodbye!"));

        driver.get(START);
        List<String> response = driver.getTextResponse();
        assertThat(response, contains("Hello World!", "Goodbye!"));
    }

    @Test(expected = CallNotActiveException.class)
    public void getTextResponseWhenNoCallActive_throwsException() throws Exception {
        driver.getTextResponse();
    }

    @Test(expected = DriverException.class)
    public void getTextResponseWhenNoResponseReceived_throwsException() throws Exception {
        when(call.getSsmlResponse()).thenReturn(null);
        driver.get(START);
        driver.getTextResponse();
    }

    /**
     * Any SSML that can't be parsed to a text response should be ignored.
     * Remaining valid SSML responses should still be interpreted.
     */
    @Test
    public void getTextResponseWhenSsmlIsInvalid_ignoresInvalidResponses() throws Exception {
        SsmlDocument ssmlInvalid = ssmlDocument().withFilename("ssmlInvalidResponse.xml").build();
        SsmlDocument ssmlHelloWorld = ssmlDocument().withFilename("ssmlTextResponse_helloWorld.xml").build();
        when(call.getSsmlResponse()).thenReturn(Arrays.asList(ssmlInvalid, ssmlHelloWorld));
        when(textResponseParser.parse(ssmlInvalid)).thenReturn(Optional.empty());
        when(textResponseParser.parse(ssmlHelloWorld)).thenReturn(Optional.of("Hello World!"));

        driver.get(START);
        List<String> audioSrc = driver.getTextResponse();
        assertThat(audioSrc, contains("Hello World!"));
    }

    @Test
    public void getAudioSrc_returnsAllAudioFilenames() throws Exception {

        SsmlDocument ssmlWelcome = ssmlDocument().withFilename("ssmlAudioResponse_welcomeMessage.xml").build();
        SsmlDocument ssmlDisclaimer = ssmlDocument().withFilename("ssmlAudioResponse_disclaimerMessage.xml").build();
        when(call.getSsmlResponse()).thenReturn(Arrays.asList(ssmlWelcome, ssmlDisclaimer));
        when(audioSrcParser.parse(ssmlWelcome)).thenReturn(Optional.of("welcomeMessage.wav"));
        when(audioSrcParser.parse(ssmlDisclaimer)).thenReturn(Optional.of("disclaimerMessage.wav"));

        driver.get(START);
        List<String> audioSrc = driver.getAudioSrc();
        assertThat(audioSrc, contains("welcomeMessage.wav", "disclaimerMessage.wav"));
    }

    @Test(expected = CallNotActiveException.class)
    public void getAudioSrcWhenNoCallActive_throwsException() throws Exception {
        driver.getAudioSrc();
    }

    @Test(expected = DriverException.class)
    public void getAudioSrcWhenNoResponseReceived_throwsException() throws Exception {
        driver.get(START);
        driver.getAudioSrc();
    }

    /**
     * Any SSML that can't be parsed to a text response should be ignored.
     * Remaining valid SSML responses should still be interpreted.
     */
    @Test
    public void getAudioSrcWhenSsmlIsInvalid_ignoresInvalidResponses() throws Exception {
        SsmlDocument ssmlInvalid = ssmlDocument().withFilename("ssmlInvalidResponse.xml").build();
        SsmlDocument ssmlWelcome = ssmlDocument().withFilename("ssmlAudioResponse_welcomeMessage.xml").build();
        when(call.getSsmlResponse()).thenReturn(Arrays.asList(ssmlInvalid, ssmlWelcome));
        when(audioSrcParser.parse(ssmlInvalid)).thenReturn(Optional.empty());
        when(audioSrcParser.parse(ssmlWelcome)).thenReturn(Optional.of("welcomeMessage.wav"));

        driver.get(START);
        List<String> audioSrc = driver.getAudioSrc();
        assertThat(audioSrc, contains("welcomeMessage.wav"));
    }

    @Test
    public void enterDtmf_sendsDtmfToCall() throws Exception {
        driver.get(START);
        driver.enterDtmf("1234");
        verify(call).enterDtmf("1234");
    }

    @Test(expected = CallNotActiveException.class)
    public void enterDtmfWhenNoCallActive_throwsException() throws Exception {
        driver.enterDtmf("1234");
    }

    @Test(expected = IllegalArgumentException.class)
    public void enterInvalidDtmfDigit_throwsException() throws Exception {
        // Expect the driver to throw an exception on invalid DTMF input
        doThrow(new IllegalArgumentException("Simulated driver error"))
                .when(call).enterDtmf("!");
        driver.get(START);
        driver.enterDtmf("!");
    }

    @Test(expected = DriverException.class)
    public void enterDtmfCausesJVoiceXmlException_throwsException() throws Exception {
        doThrow(new JVoiceXmlException("Simulated driver error"))
                .when(call).enterDtmf("!");
        driver.get(START);
        driver.enterDtmf("!");
    }

    @Test
    public void sayUtterance_sendsUtteranceToCall() throws Exception {
        driver.get(START);
        driver.say("Hiya");
        verify(call).sendUtterance("Hiya");
    }

    @Test(expected = CallNotActiveException.class)
    public void sayUtteranceWhenNoCallActive_throwsException() throws Exception {
        driver.say("Hiya");
    }

    @Test(expected = DriverException.class)
    public void sayUtteranceCausesJVoiceeXmlException_throwsException() throws Exception {
        doThrow(new JVoiceXmlException("Simulated driver error"))
                .when(call).sendUtterance(any(String[].class));
        driver.get(START);
        driver.say("Hiya");
    }

    @Test
    public void shutdown_clearsDownCall() throws Exception {
        driver.get(START);
        driver.shutdown();
        verify(call).shutdown();
    }

    @Test
    public void shutdown_stopsJvxmlInterpreter() {
        driver.shutdown();
        verify(jvxml).shutdown();
    }

    @Test
    public void shutdown_waitsForJvxmlShutdown() {
        driver.shutdown();
        verify(jvxml).waitShutdownComplete();
    }

    /**
     * Document storage is not automatically stopped on interpreter shutdown.
     * Ensure this happens explicitly.
     */
    @Test
    public void shutdown_stopsDocumentServer() {
        driver.shutdown();
        verify(documentServer).stop();
    }

    @Test
    @Ignore("Fails intermittently due to race condition")
    public void shutdown_preventsJvmExit() {
        Thread terminationThread = new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("Termination thread interrupted");
            }
        });
        terminationThread.setName("TerminationThread");
        terminationThread.start();

        driver.shutdown();
        assertTrue(terminationThread.isInterrupted() || !terminationThread.isAlive());
    }
}