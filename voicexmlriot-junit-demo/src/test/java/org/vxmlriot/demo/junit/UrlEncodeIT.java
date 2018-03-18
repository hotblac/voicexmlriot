package org.vxmlriot.demo.junit;

import org.apache.http.client.utils.URIBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.driver.VxmlDriverFactory;
import org.vxmlriot.jvoicexml.JVoiceXmlDriverBuilder;

import java.net.URI;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Verify that URL parameters are encoded correctly
 */
public class UrlEncodeIT {

    private static VxmlDriver driver;

    @BeforeClass
    public static void startDriver() {
        VxmlDriverFactory.driverBuilder = new JVoiceXmlDriverBuilder();
        driver = VxmlDriverFactory.getDriver();
    }

    @AfterClass
    public static void shutdownDriver() {
        driver.shutdown();
    }

    @Test
    public void vxmlContainsUrlEncodedParameters() throws Exception {
        URI uri = new URIBuilder()
                .setScheme("http")
                .setHost("localhost")
                .setPort(9090)
                .setPath("voicexmlriot-junit-demo/dynamicPage.jsp")
                .setParameter("param1", "blah+blah")
                .setParameter("param2", "foo+bar")
                .build();
        driver.get(uri);

        List<String> textResponse = driver.getTextResponse();
        assertThat(textResponse, contains("blah+blah"));

        List<String> audioResponse = driver.getAudioSrc();
        assertThat(audioResponse, contains(endsWith("foo+bar.wav")));
    }
}
