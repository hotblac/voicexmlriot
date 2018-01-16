package org.vxmlriot.jvoicexml.listener;

import org.apache.log4j.Logger;
import org.jvoicexml.xml.ssml.SsmlDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.MapUtils.isEmpty;

public class ResponseListener extends TextListenerAdapter {

    private static final Logger LOGGER = Logger.getLogger(ResponseListener.class);

    /**
     * Time to wait for first response.
     * If we exceed this time with no response, assume no response was sent.
     */
    private static final long TIME_TO_FIRST_RESPONSE_MS = 3000;

    /**
     * Time to wait between responses.
     * If we exceed this time after a response, assume all responses were sent.
     */
    private static final long TIME_TO_NEXT_RESPONSE_MS = 500;

    /**
     * Expect more responses until time since last response has expired
     */
    private boolean awaitingMoreResponses = true;

    /**
     * SsmlDocument responses received in insertion order.
     * Implemented as a Map keyed by the SsmlDocument text (String) so that
     * duplicates are discarded. If two (non-equal) SsmlDocuments are received
     * with the same text, count as a duplicate and keep only one.
     * This is a workaround for JVoiceXML sending duplicate outputSsml
     * events.
     * Synchronized as this will be updated by the TextServer thread and read by the
     * main thread.
     */
    private final Map<String, SsmlDocument> capturedResponses =
            Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    public synchronized void outputSsml(SsmlDocument document) {
        capturedResponses.put(document.toString(), document);
        awaitingMoreResponses = true;
        notify();
    }

    /**
     * Clear the listener state.
     * This should be done before any new responses are expected.
     */
    public void clear() {
        LOGGER.debug("clear");
        capturedResponses.clear();
        awaitingMoreResponses = true;
    }

    /**
     * Return all text response output.
     * This method will block until all responses are received.
     * @return List of SsmlDocuments. A single VXML page with multiple
     *         speech sections will return multiple responses.
     */
    public List<SsmlDocument> getCapturedResponses() {

        // Wait for further responses
        while (awaitingMoreResponses) {
            awaitingMoreResponses = false;
            waitForNextResponse();
        }

        // Assume no more responses
        synchronized (capturedResponses) {
            // Return a copy of the list to prevent concurrent modification
            return new ArrayList<>(capturedResponses.values());
        }
    }

    private synchronized void waitForNextResponse() {
        long timeout = isEmpty(capturedResponses) ? TIME_TO_FIRST_RESPONSE_MS : TIME_TO_NEXT_RESPONSE_MS;
        try {
            wait(timeout);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while awaiting text response", e);
        }
    }
}
