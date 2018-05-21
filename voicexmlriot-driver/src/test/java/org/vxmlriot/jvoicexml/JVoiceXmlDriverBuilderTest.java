package org.vxmlriot.jvoicexml;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvoicexml.Configuration;
import org.jvoicexml.ImplementationPlatformFactory;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.config.JVoiceXmlConfiguration;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vxmlriot.driver.EventDelay;
import org.vxmlriot.url.ClasspathFileUriBuilder;
import org.vxmlriot.url.UriBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.vxmlriot.jvoicexml.JVoiceXmlDriverBuilder.DEFAULT_CONFIG_ROOT;
import static org.vxmlriot.jvoicexml.JVoiceXmlDriverBuilder.DEFAULT_REPOSITORY_FILES;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        JVoiceXmlDriverBuilder.class,
        JVoiceXmlConfiguration.class,
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

        final JVoiceXmlConfiguration expectedConfig = mock(JVoiceXmlConfiguration.class);
        PowerMockito.whenNew(JVoiceXmlConfiguration.class)
                .withArguments(DEFAULT_CONFIG_ROOT, DEFAULT_REPOSITORY_FILES)
                .thenReturn(expectedConfig);

        JVoiceXmlDriver jvxmlDriver = builder.build();
        assertNotNull(jvxmlDriver.callBuilder);
        assertNotNull(jvxmlDriver.delays);
        assertThat(jvxmlDriver.uriBuilder, instanceOf(ClasspathFileUriBuilder.class));
        verifyNew(JVoiceXmlMain.class).withArguments(expectedConfig);
    }

    @Test
    public void buildJVoiceXmlDriver_buildsConfigurationFromClasspath() throws Exception {
        builder.build();

        ArgumentCaptor<JVoiceXmlConfiguration> configCaptor = ArgumentCaptor.forClass(JVoiceXmlConfiguration.class);
        verifyNew(JVoiceXmlMain.class).withArguments(configCaptor.capture());
        JVoiceXmlConfiguration config = configCaptor.getValue();

        // Config exists
        assertNotNull(config);

        // Assert we can load something from config
        assertNotNull(config.loadObject(ImplementationPlatformFactory.class));
    }

    @Test
    public void buildJVoiceXmlDriver_startsJvxmlInterpreter() throws Exception {
        builder.build();
        verify(jvxml).start();
    }

    @Test
    public void buildWithConfigClass_usesConfigClass() throws Exception {
        Configuration alternativeConfig = new AlternativeConfiguration();
        builder.config(alternativeConfig).build();
        verifyNew(JVoiceXmlMain.class).withArguments(alternativeConfig);
    }

    @Test
    public void buildWithConfigRoot_usesConfigRoot() throws Exception {
        String customRootConfig = "custom-config.xml";
        final JVoiceXmlConfiguration expectedConfig = mock(JVoiceXmlConfiguration.class);
        PowerMockito.whenNew(JVoiceXmlConfiguration.class)
                .withArguments(eq(customRootConfig), anyListOf(String.class))
                .thenReturn(expectedConfig);

        builder.configRoot(customRootConfig).build();
        verifyNew(JVoiceXmlMain.class).withArguments(expectedConfig);
    }

    @Test
    public void buildWithRepositoryFiles_usesRepositoryFiles() throws Exception {
        final List<String> customRepositoryFiles = Arrays.asList("file1.xml", "file2.xml");
        final JVoiceXmlConfiguration expectedConfig = mock(JVoiceXmlConfiguration.class);
        PowerMockito.whenNew(JVoiceXmlConfiguration.class)
                .withArguments(anyString(), eq(customRepositoryFiles))
                .thenReturn(expectedConfig);

        builder.repositoryFiles(customRepositoryFiles).build();
        verifyNew(JVoiceXmlMain.class).withArguments(expectedConfig);
    }

    @Test
    public void buildWithCustomDelays_usesCustomDelays() {
        final EventDelay delays = new EventDelay();
        JVoiceXmlDriver driver = builder.eventDelays(delays).build();
        assertEquals(delays, driver.delays);
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