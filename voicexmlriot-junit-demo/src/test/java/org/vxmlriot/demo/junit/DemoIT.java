package org.vxmlriot.demo.junit;

import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * A demo integration test using a JVoiceXML driver
 */
public class DemoIT {

    @Test
    public void verifyStart() throws Exception {

        // Quick check that app is running
        URL url = new URL("http://localhost:9090/voicexmlriot-junit-demo/start.vxml");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        assertEquals(200, connection.getResponseCode());
    }
}
