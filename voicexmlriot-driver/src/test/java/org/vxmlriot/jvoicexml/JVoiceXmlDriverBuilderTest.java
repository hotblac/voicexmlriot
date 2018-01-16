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
import org.vxmlriot.url.ClasspathFileUriBuilder;
import org.vxmlriot.url.UriBuilder;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        JVoiceXmlDriverBuilder.class,
        JVoiceXmlMain.class
})
public class JVoiceXmlDriverBuilderTest {

    @Mock private JVoiceXmlMain jvxml;
    @Mock private JVoiceXmlDriverBuilder.JVoiceXmlStartupListener startupListener;
    @InjectMocks private JVoiceXmlDriverBuilder builder;

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
    public void buildJVoiceXmlDriver_hasDefaultDependencies() throws Exception {
        JVoiceXmlDriver jvxmlDriver = builder.build();
        assertNotNull(jvxmlDriver.callBuilder);
        assertThat(jvxmlDriver.uriBuilder, instanceOf(ClasspathFileUriBuilder.class));
        verifyNew(JVoiceXmlMain.class).withArguments(any(EmbeddedTextConfiguration.class));
    }

    @Test
    public void buildJVoiceXmlDriver_startsJvxmlInterpreter() throws Exception {
        builder.build();
        verify(jvxml).start();
    }

    @Test
    public void buildJVoiceXmlDriverWithAlternativeConfiguration() throws Exception {
        Configuration alternativeConfig = new AlternativeConfiguration();
        builder.config(alternativeConfig).build();
        verifyNew(JVoiceXmlMain.class).withArguments(alternativeConfig);
    }

    @Test
    public void buildJVoiceXmlDriverWithAlternativeUriBuilder() {
        UriBuilder alternativeUriBuilder = resource -> null;
        JVoiceXmlDriver jvxmlDriver = builder.uriBuilder(alternativeUriBuilder).build();
        assertEquals(alternativeUriBuilder, jvxmlDriver.uriBuilder);
    }

    private class AlternativeConfiguration implements Configuration {
        @Override
        public <T> Collection<T> loadObjects(Class<T> baseClass, String root) {
            return null;
        }
        @Override
        public <T> T loadObject(Class<T> baseClass, String key) {
            return null;
        }
        @Override
        public <T> T loadObject(Class<T> baseClass) {
            return null;
        }
    }
}