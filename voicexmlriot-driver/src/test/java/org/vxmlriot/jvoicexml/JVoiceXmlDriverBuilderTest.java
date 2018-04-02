package org.vxmlriot.jvoicexml;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.runner.RunWith;
import org.jvoicexml.Configuration;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.config.JVoiceXmlConfiguration;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vxmlriot.url.ClasspathFileUriBuilder;
import org.vxmlriot.url.UriBuilder;

import java.io.File;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.vxmlriot.jvoicexml.JVoiceXmlDriverBuilder.PROPERTY_CONFIG_DIR;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        JVoiceXmlDriverBuilder.class,
        JVoiceXmlMain.class
})
public class JVoiceXmlDriverBuilderTest {

    @Rule public final ClearSystemProperties myPropertyIsCleared
            = new ClearSystemProperties(PROPERTY_CONFIG_DIR);

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
        verifyNew(JVoiceXmlMain.class).withArguments(any(JVoiceXmlConfiguration.class));
    }

    @Test
    public void buildJVoiceXmlDriver_startsJvxmlInterpreter() throws Exception {
        builder.build();
        verify(jvxml).start();
    }

    @Test
    public void buildJVoiceXmlDriver_setsConfigPathSystemProperty() {
        builder.build();

        // Verify that config dir system property is set to the directory containing the config files
        String confDir = System.getProperty(PROPERTY_CONFIG_DIR);
        File configFile = new File(confDir, "jvoicexml.xml");
        assertTrue(configFile.exists());
    }

    @Test
    public void buildWithConfigDir_overridesDefault() {
        final String expectedConfigDir = "/alternativeDir";
        builder.config(expectedConfigDir).build();
        assertEquals(expectedConfigDir, System.getProperty(PROPERTY_CONFIG_DIR));
    }

    @Test
    public void buildWithConfigClass_usesConfigClass() throws Exception {
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