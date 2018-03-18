package org.vxmlriot.demo.junit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.driver.VxmlDriverFactory;
import org.vxmlriot.jvoicexml.JVoiceXmlDriverBuilder;
import org.vxmlriot.url.RelativeUrlUriBuilder;

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
    public void testGoodbyeFlow() throws Exception {
        driver.get("start.vxml");
        assertThat(driver.getTextResponse(), contains(
                "This is a demo VoiceXML application.",
                "Do you want to hear more? Press 1 for yes and 2 for no."
        ));

        driver.enterDtmf("2");
        assertThat(driver.getTextResponse(), contains("Goodbye!"));
    }


    @Test
    public void testHearMoreFlow() throws Exception {
        driver.get("start.vxml");
        assertThat(driver.getTextResponse(), contains(
                "This is a demo VoiceXML application.",
                "Do you want to hear more? Press 1 for yes and 2 for no."
        ));

        driver.enterDtmf("1");
        assertThat(driver.getAudioSrc(), contains(endsWith("voiceMenu.wav")));

        driver.say("sure");
        assertThat(driver.getAudioSrc(), contains(endsWith("yes.wav")));
    }

    @Test
    public void testGrammarMisrec() throws Exception {
        driver.get("start.vxml");
        assertThat(driver.getTextResponse(), hasItem("This is a demo VoiceXML application."));

        driver.enterDtmf("1");
        assertThat(driver.getAudioSrc(), hasItem(endsWith("voiceMenu.wav")));

        driver.say("um...");
        assertThat(driver.getAudioSrc(), hasItem(endsWith("nomatch.wav")));
    }

}
