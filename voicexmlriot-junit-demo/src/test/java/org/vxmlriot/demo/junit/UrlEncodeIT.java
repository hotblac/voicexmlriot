package org.vxmlriot.demo.junit;

import org.apache.http.client.utils.URIBuilder;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.driver.VxmlDriverFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Verify that URL parameters are encoded correctly
 */
public class UrlEncodeIT {

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    private static VxmlDriver driver = VxmlDriverFactory.getDriver();

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
