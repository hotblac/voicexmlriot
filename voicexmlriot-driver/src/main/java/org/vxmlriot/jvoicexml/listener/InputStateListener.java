package org.vxmlriot.jvoicexml.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvoicexml.client.text.TextMessageEvent;

public class InputStateListener extends TextListenerAdapter {

    private static final Logger LOGGER = LogManager.getLogger(InputStateListener.class);
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
    public synchronized void expectingInput(TextMessageEvent textMessageEvent) {
        LOGGER.debug("readyForInput = true");
        notify();
        readyForInput = true;
    }

    @Override
    public synchronized void inputClosed(TextMessageEvent textMessageEvent) {
        LOGGER.debug("readyForInput = false");
        readyForInput = false;
    }
}
