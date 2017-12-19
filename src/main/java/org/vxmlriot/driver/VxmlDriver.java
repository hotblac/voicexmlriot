package org.vxmlriot.driver;

import org.vxmlriot.exception.DriverException;

import java.util.List;

/**
 * Main interface to VoiceXMLRiot.
 * This simulates a VXML browser and user interactions. It allows user input
 * to be simulated and VXML responses to be verified.
 */
public interface VxmlDriver {

    /**
     * Get a VXML document by URL
     * @param resource identifying the VXML to be loaded.
     * @throws DriverException on failure of underlying driver
     * @throws IllegalArgumentException on invalid resource
     */
    void get(String resource) throws DriverException;

    /**
     * Simulate user entering a string of DTMF digits
     * @param digits to be entered
     */
    void enterDtmf(String digits);

    /**
     * Smimulate user saying a sequence of utterances
     * @param utterance spoken by user
     */
    void say(String... utterance);

    /**
     * Text response spoken by VXML document. Typically, a VXML browser would
     * render this via TTS.
     * @return list of text responses of current document
     */
    List<String> getTextResponse();

    /**
     * Audio source files played by VXML document.
     * @return list of audio responses of current document
     */
    List<String> getAudioSrc();

}
