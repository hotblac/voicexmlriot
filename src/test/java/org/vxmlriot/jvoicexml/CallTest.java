package org.vxmlriot.jvoicexml;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvoicexml.Session;
import org.jvoicexml.client.text.TextServer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextServer.class)
public class CallTest {

    @Mock private Session session;
    @Mock private TextServer textServer;
    @InjectMocks private Call call;

    @Test
    @Ignore("TODO")
    public void call_makesACall() {
        fail("Test not complete");
    }

    @Test
    public void shutdown_stopsTextServer() {
        call.shutdown();
        verify(textServer).stopServer();
    }
}