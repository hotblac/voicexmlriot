package org.vxmlriot.jvoicexml;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvoicexml.ConnectionInformation;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.Session;
import org.jvoicexml.client.text.TextServer;
import org.jvoicexml.event.ErrorEvent;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vxmlriot.jvoicexml.listener.InputStateListener;
import org.vxmlriot.jvoicexml.listener.LoggingTextListener;
import org.vxmlriot.jvoicexml.listener.ResponseListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.vxmlriot.jvoicexml.CallBuilder.TEXT_SERVER_PORT;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        CallBuilder.class,
        JVoiceXmlMain.class,
        TextServer.class})
public class CallBuilderTest {

    @Mock private JVoiceXmlMain jvxml;
    @Mock private TextServer textServer;
    @Mock private Session session;
    @InjectMocks private CallBuilder builder;

    @Before
    public void initMocks() throws Exception, ErrorEvent {
        PowerMockito.whenNew(TextServer.class).withArguments(TEXT_SERVER_PORT).thenReturn(textServer);
        when(jvxml.createSession(any(ConnectionInformation.class))).thenReturn(session);
    }

    @Test
    public void build_returnsCall() throws Exception {
        Call call = builder.build();
        assertNotNull(call);
        assertNotNull(call.responseListener);
        assertNotNull(call.inputState);
    }

    @Test
    public void build_createsCallSession() throws Exception {
        Call call = builder.build();
        assertEquals(session, call.session);
    }

    @Test
    public void build_startsTextServer() throws Exception {
        Call call = builder.build();
        verify(textServer).start();
        assertEquals(textServer, call.textServer);
    }

    @Test
    public void build_addsTextServerListeners() throws Exception {
        builder.build();
        verify(textServer).addTextListener(isA(ResponseListener.class));
        verify(textServer).addTextListener(isA(LoggingTextListener.class));
        verify(textServer).addTextListener(isA(InputStateListener.class));
    }
}