package org.vxmlriot.demo.junit;

import org.junit.*;
import org.junit.rules.Timeout;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.driver.VxmlDriverFactory;
import org.vxmlriot.jvoicexml.JVoiceXmlDriverBuilder;
import org.vxmlriot.url.RelativeUrlUriBuilder;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class SubdialogIT {

    @Rule public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    private static final String APP_ROOT = "http://localhost:9090/voicexmlriot-junit-demo/";

    private static VxmlDriver driver;

    @BeforeClass
    public static void startDriver() {
        VxmlDriverFactory.driverBuilder = new JVoiceXmlDriverBuilder()
                .uriBuilder(new RelativeUrlUriBuilder(APP_ROOT));
        driver = VxmlDriverFactory.getDriver();
    }

    @AfterClass
    public static void shutdownDriver() {
        driver.shutdown();
    }


    @After
    public void endCall() {
        driver.hangup();
    }

    @Test
    public void testSubdialog() throws Exception {
        driver.get("invokeSubdialog.vxml");
        assertThat(driver.getTextResponse(), contains(
                "Hello from the subdialog!",
                "This happens after the subdialog return."
        ));
    }
}
