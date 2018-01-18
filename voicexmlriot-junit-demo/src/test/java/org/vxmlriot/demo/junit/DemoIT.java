package org.vxmlriot.demo.junit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.driver.VxmlDriverFactory;
import org.vxmlriot.jvoicexml.JVoiceXmlDriverBuilder;
import org.vxmlriot.url.RelativeUrlUriBuilder;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * A demo integration test using a JVoiceXML driver.
 * This requires the application to be up and running at APP_ROOT.
 *
 * In this example project, the application can be started using
 * mvn tomcat7:run
 *
 * To run as an automated unit test, run
 * mvn verify
 * which will start the Tomcat server, run the tests and then shutdown.
 */
public class DemoIT {

    private static final String APP_ROOT = "http://localhost:9090/voicexmlriot-junit-demo/";

    @BeforeClass
    public static void  configureDriver() {
        VxmlDriverFactory.driverBuilder = new JVoiceXmlDriverBuilder()
                .uriBuilder(new RelativeUrlUriBuilder(APP_ROOT));
    }

    @Test
    public void verifyStart() throws Exception {
        VxmlDriver driver = VxmlDriverFactory.getDriver();
        driver.get("start.vxml");
        List<String> responses = driver.getTextResponse();
        assertThat(responses, hasSize(2));
        assertThat(responses, contains("Hello World!", "Goodbye!"));
    }
}
