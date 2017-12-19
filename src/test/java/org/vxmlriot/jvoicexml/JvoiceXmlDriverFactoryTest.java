package org.vxmlriot.jvoicexml;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvoicexml.Configuration;
import org.jvoicexml.JVoiceXmlMain;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        JvoiceXmlDriverFactory.class,
        JVoiceXmlMain.class
})
public class JvoiceXmlDriverFactoryTest {

    @Mock private JVoiceXmlMain jvxml;
    @Mock private JvoiceXmlDriverFactory.JVoiceXmlStartupListener startupListener;
    @InjectMocks private JvoiceXmlDriverFactory factory;

    @Before
    public void initMocks() throws Exception {
        PowerMockito.whenNew(JVoiceXmlMain.class).withArguments(any(Configuration.class)).thenReturn(jvxml);

        // Trigger jvxmlStarted event immediately on startup
        doAnswer(i -> {
            startupListener.jvxmlStarted();
            return null;
        }).when(jvxml).start();
    }
    @Test
    public void createJvoiceXmlDriver_hasRequiredDependencies() {
        JvoiceXmlDriver jvxmlDriver = factory.getDriver();
        assertNotNull(jvxmlDriver.callBuilder);
        assertNotNull(jvxmlDriver.uriBuilder);
    }


    @Test
    public void createJvoiceXmlDriver_startsJvxmlInterpreter() throws Exception {
        JvoiceXmlDriver jvxmlDriver = factory.getDriver();
        verify(jvxml).start();
    }
}