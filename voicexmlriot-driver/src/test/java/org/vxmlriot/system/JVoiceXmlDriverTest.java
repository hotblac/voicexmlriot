package org.vxmlriot.system;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vxmlriot.driver.EventDelay;
import org.vxmlriot.exception.CallIsActiveException;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.driver.VxmlDriverFactory;
import org.vxmlriot.jvoicexml.JVoiceXmlDriverBuilder;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration test of the JvoiceXml driver implementation
 */
public class JVoiceXmlDriverTest {

    private static final Logger LOGGER = Logger.getLogger(JVoiceXmlDriverTest.class);
    private static VxmlDriver driver;

    @BeforeClass
    public static void startDriver() {
        EventDelay delays = new EventDelay();
        delays.setInputDelay(500);
        delays.setResponseDelay(0);
        delays.setCallClearDelay(0);
        VxmlDriverFactory.driverBuilder = new JVoiceXmlDriverBuilder().eventDelays(delays);
        driver = VxmlDriverFactory.getDriver();
    }

    @After
    public void endCall() {
        driver.hangup();
    }

    @AfterClass
    public static void shutdownDriver() {
        driver.shutdown();
    }

    @Test
    public void getLocalFile_works() throws Exception {
        driver.get("hello.vxml");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMissingFile_throwsException() throws Exception {
        driver.get("missing.vxml");
    }

    @Test
    public void getMultipleRequests_works() throws Exception {
        driver.get("hello.vxml");
        driver.hangup();
        driver.get("hello.vxml");
    }

    @Test(expected = CallIsActiveException.class)
    public void getWhileCallIsActive_throwsException() throws Exception {
        driver.get("hello.vxml");
        driver.get("hello.vxml");
    }

    @Test
    public void getTextResponse_returnsAllResponses() throws Exception {
        driver.get("hello.vxml");
        List<String> textResponse = driver.getTextResponse();
        assertThat(textResponse, contains(
                "Hello World!",
                "Goodbye!"
        ));
    }

    @Test
    public void getAudioSrc_returnsAllAudioFilenames() throws Exception {
        driver.get("audio.vxml");
        List<String> audioSrc = driver.getAudioSrc();
        assertThat(audioSrc, hasSize(3));
        assertThat(audioSrc, contains(
                endsWith("audio-in-block.wav"),
                endsWith("audio-in-prompt.wav"),
                endsWith("audio-in-expr.wav")
        ));
    }

    @Test
    public void dtmfMenu_promptIsPlayed() throws Exception {
        driver.get("dtmf.vxml");
        List<String> textResponse = driver.getTextResponse();

        // Specifically check that only 1 prompt is played.
        // JVoiceXML can send duplicate Ssml events for menus.
        LOGGER.debug("textResponse: "  + String.join("|", textResponse));
        assertThat(textResponse, hasSize(1));
        assertThat(textResponse, contains("Do you like this example? Please enter 1 for yes or 2 for no"));
    }

    @Test
    public void enterDtmf_selectsMenuOption() throws Exception {
        driver.get("dtmf.vxml");
        driver.enterDtmf("1");
        List<String> textResponse = driver.getTextResponse();
        assertThat(textResponse, hasSize(1));
        assertThat(textResponse, contains("You like this example."));
    }

    @Test
    public void enterDtmfInvalidOption_triggersReprompt() throws Exception {
        driver.get("dtmf.vxml");
        driver.enterDtmf("9");
        List<String> textResponse = driver.getTextResponse();
        assertThat(textResponse, hasSize(1));
        // Reprompt
        assertThat(textResponse, contains("Do you like this example? Please enter 1 for yes or 2 for no"));
    }

    @Test
    public void speechInputMenu_promptIsPlayed() throws Exception {
        driver.get("input.vxml");
        List<String> textResponse = driver.getTextResponse();
        assertNumberOfResponses(textResponse, 1);
        assertThat(textResponse, contains("Do you like this example?"));
    }

    @Test
    public void sayYes_selectsMenuOption() throws Exception {
        driver.get("input.vxml");
        driver.say("yes");
        List<String> textResponse = driver.getTextResponse();
        assertNumberOfResponses(textResponse, 1);
        assertThat(textResponse, contains("You like this example."));
    }

    @Test
    public void sayNo_selectsMenuOption() throws Exception {
        driver.get("input.vxml");
        driver.say("no");
        List<String> textResponse = driver.getTextResponse();
        assertNumberOfResponses(textResponse, 1);
        assertThat(textResponse, contains("You do not like this example."));
    }

    @Test
    public void sayUnrecognized_triggersReprompt() throws Exception {
        driver.get("input.vxml");
        driver.say("don't know");
        List<String> textResponse = driver.getTextResponse();
        assertNumberOfResponses(textResponse, 1);
        assertThat(textResponse, contains("Do you like this example?"));
    }

    private void assertNumberOfResponses(List<String> textResponse, int expectedSize) {
        assertThat("Unexpected response: " + String.join("|", textResponse),
                textResponse, hasSize(expectedSize));
    }
}
