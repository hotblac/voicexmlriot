package org.vxmlriot.jvoicexml;

import org.junit.After;
import org.junit.Before;
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
import org.vxmlriot.jvoicexml.exception.JvoiceXmlStartupException;
import org.vxmlriot.url.UriBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


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
                .thenThrow(new JvoiceXmlStartupException("Simulated JVoiceXml failure"));
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

        when(call.getLastResponse()).thenReturn(Arrays.asList(
                getSsmlDocument("ssmlTextResponse_helloWorld.xml"),
                getSsmlDocument("ssmlTextResponse_goodbye.xml")
        ));

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
        when(call.getLastResponse()).thenReturn(null);
        driver.get(START);
        driver.getTextResponse();
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

    private SsmlDocument getSsmlDocument(String filename) throws ParserConfigurationException, SAXException, IOException {
        final InputStream vxmlInput = getClass().getClassLoader().getResourceAsStream(filename);
        final InputSource vxmlSource = new InputSource(vxmlInput);
        return new SsmlDocument(vxmlSource);
    }
}