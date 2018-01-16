package org.vxmlriot.jvoicexml.listener;

import org.apache.log4j.Logger;

public class InputStateListener extends TextListenerAdapter {

    private static final Logger LOGGER = Logger.getLogger(InputStateListener.class);
    private boolean readyForInput = false;

    public boolean isReadyForInput() {
        return readyForInput;
    }

    public synchronized void waitUntilReadyForInput() {
        if (readyForInput) return;
        try {
            wait();
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for ready");
        }
    }

    @Override
    public synchronized void expectingInput() {
        LOGGER.debug("readyForInput = true");
        notify();
        readyForInput = true;
    }

    @Override
    public synchronized void inputClosed() {
        LOGGER.debug("readyForInput = false");
        readyForInput = false;
    }
}