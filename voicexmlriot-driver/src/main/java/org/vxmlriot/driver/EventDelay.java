package org.vxmlriot.driver;

import org.apache.log4j.Logger;

import static java.lang.Thread.sleep;

/**
 * Manages standard delays on telephony events.
 * All delays are in milliseconds.
 */
public class EventDelay {

    private static final Logger LOGGER = Logger.getLogger(EventDelay.class);

    /**
     * Delay between requesting a response from the VXML browser and
     * checking the response. This delay allows the VXML browser
     * time to build the response before we ask for it.
     */
    private long responseDelay = 0;

    /**
     * Delay between input and pushing DTMF / utterance to the VXML browser.
     * This allows time for the VXML browser to get into a state that its
     * ready to accept input.
     */
    private long inputDelay = 0;

    public void delayBeforeResponse() {
        try {
            sleep(responseDelay);
        } catch (InterruptedException e) {
            LOGGER.warn("Thread interrupted while delaying for response", e);
        }
    }

    public void delayBeforeInput() {
        try {
            sleep(inputDelay);
        } catch (InterruptedException e) {
            LOGGER.warn("Thread interrupted while delaying for input", e);
        }
    }

    public void setResponseDelay(long responseDelay) {
        this.responseDelay = responseDelay;
    }

    public void setInputDelay(long inputDelay) {
        this.inputDelay = inputDelay;
    }
}
