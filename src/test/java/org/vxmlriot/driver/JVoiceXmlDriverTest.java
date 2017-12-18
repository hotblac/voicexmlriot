package org.vxmlriot.driver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vxmlriot.jvoicexml.Call;
import org.vxmlriot.jvoicexml.CallBuilder;
import org.vxmlriot.url.UriBuilder;

import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the JVoiceXML implementation of VxmlDriver
 */
@RunWith(MockitoJUnitRunner.class)
public class JVoiceXmlDriverTest {

    private static final String START = "http://example.com/START.vxml";
    private static final URI START_URI = URI.create(START);

    @Mock private Call call;
    @Mock private UriBuilder uriBuilder;
    @Mock private CallBuilder callBuilder;
    @InjectMocks private JVoiceXmlDriver driver;

    @Before
    public void setUp() {
        when(uriBuilder.build(START)).thenReturn(START_URI);
        when(callBuilder.build()).thenReturn(call);
    }

    @Test
    public void get_startsNewCall() {
        driver.get(START);
        verify(callBuilder).build();
    }

    @Test
    public void get_requestsPageByUri() {
        driver.get(START);
        verify(call).call(START_URI);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getInvalidUriString() {
        when(uriBuilder.build("invalidUri"))
                .thenThrow(new IllegalArgumentException("Simulated invalid URI failure"));
        driver.get("invalidUri");
    }
}