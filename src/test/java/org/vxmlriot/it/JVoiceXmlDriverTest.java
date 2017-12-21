package org.vxmlriot.it;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.vxmlriot.exception.CallIsActiveException;
import org.vxmlriot.jvoicexml.JVoiceXmlDriver;
import org.vxmlriot.driver.VxmlDriver;
import org.vxmlriot.driver.VxmlDriverFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Integration test of the JvoiceXml driver implementation
 */
public class JVoiceXmlDriverTest {

    private static VxmlDriver driver = VxmlDriverFactory.getDriver();

    @After
    public void endCall() {
        driver.hangup();
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
        driver.shutdown();
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
