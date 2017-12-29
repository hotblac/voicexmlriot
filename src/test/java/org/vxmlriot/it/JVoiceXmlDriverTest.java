package org.vxmlriot.it;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vxmlriot.exception.CallIsActiveException;
import org.vxmlriot.jvoicexml.JVoiceXmlDriver;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.driver.VxmlDriverFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration test of the JvoiceXml driver implementation
 */
public class JVoiceXmlDriverTest {

    private static final Logger LOGGER = Logger.getLogger(JVoiceXmlDriverTest.class);
    private static VxmlDriver driver = VxmlDriverFactory.getDriver();

    @After
    public void endCall() {
        driver.hangup();
    }

    @BeforeClass
    public static void cleanupDriver() {
        try {
            killTerminationThread();
        } catch (AssertionError assertionError) {
            LOGGER.debug("Driver not yet running", assertionError);
        }
    }

    @AfterClass
    public static void stopDriver() {
        driver.shutdown();
        killTerminationThread();
    }

    @Test
    public void factory_buildsJvoiceXmlDriver() {
        assertThat(driver, instanceOf(JVoiceXmlDriver.class));
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

    @Test
    public void restartDriver_works() throws Exception {

        // Shutdown driver but do not let it terminate the JVM
        driver.shutdown();
        killTerminationThread();

        driver = VxmlDriverFactory.getDriver();
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
        assertThat(audioSrc, contains(
                endsWith("audio-in-block.wav"),
                endsWith("audio-in-prompt.wav")
        ));
    }

    @Test
    public void dtmfMenu_promptIsPlayed() throws Exception {
        driver.get("dtmf.vxml");
        List<String> textResponse = driver.getTextResponse();

        // Specifically check that only 1 prompt is played.
        // JVoiceXML can send duplicate Ssml events for menus.
        System.out.println("textResponse: "  + String.join("|", textResponse));
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

        System.out.println("textResponse: "  + String.join("|", textResponse));
        assertThat(textResponse, hasSize(1));
        assertThat(textResponse, contains("Do you like this example?"));
    }

    @Test
    public void sayYes_selectsMenuOption() throws Exception {
        driver.get("input.vxml");
        driver.say("yes");
        List<String> textResponse = driver.getTextResponse();
        assertThat(textResponse, hasSize(1));
        assertThat(textResponse, contains("You like this example."));
    }

    @Test
    public void sayNo_selectsMenuOption() throws Exception {
        driver.get("input.vxml");
        driver.say("no");
        List<String> textResponse = driver.getTextResponse();
        assertThat(textResponse, hasSize(1));
        assertThat(textResponse, contains("You do not like this example."));
    }

    @Test
    public void sayUnrecognized_triggersReprompt() throws Exception {
        driver.get("input.vxml");
        driver.say("don't know");
        List<String> textResponse = driver.getTextResponse();
        assertThat(textResponse, hasSize(1));
        assertThat(textResponse, contains("Do you like this example?"));
    }

    /**
     * Default behaviour is for TerminationThread to stop the JVM 10s after
     * shutdown request received.
     * This will break unit tests if they're still running so we need to kill
     * the thread.
     */
    private static void killTerminationThread() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();

        // Find the root thread group
        ThreadGroup rootThreadGroup = threadGroup;
        while (rootThreadGroup.getParent() != null) {
            rootThreadGroup = rootThreadGroup.getParent();
        }

        // Find the thread named TerminationThread
        Thread[] threads = new Thread[1024];
        int numThreads = rootThreadGroup.enumerate(threads);
        if (numThreads > 1024) {
            System.out.println("WARN: Number of threads exceeds array size.");
        }
        Thread terminationThread = Arrays.stream(threads)
                .filter(Objects::nonNull)
                .filter(t -> t.getName().equals("TerminationThread"))
                .findAny()
                .orElseThrow(() -> new AssertionError("Failed to find TerminationThread. JVoiceXML may exit JVM before tests complete."));

        // Interrupting the shutdown thread is enough to prevent it exiting the JVM
        System.out.println("Interrupting the TerminationThread to prevent premature JVM exit");
        terminationThread.interrupt();
    }
}
