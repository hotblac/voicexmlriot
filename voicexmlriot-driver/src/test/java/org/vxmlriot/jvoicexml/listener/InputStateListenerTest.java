package org.vxmlriot.jvoicexml.listener;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InputStateListenerTest {

    private InputStateListener inputState = new InputStateListener();

    @Test
    public void beforeEventsReceived_notReadyForInput() {
        assertFalse(inputState.isReadyForInput());
    }

    @Test
    public void onExpectingInputEvent_readyForInput() {
        inputState.expectingInput();
        assertTrue(inputState.isReadyForInput());
    }

    @Test
    public void onInputClosed_notReadyForInput() {
        inputState.expectingInput();
        inputState.inputClosed();
        assertFalse(inputState.isReadyForInput());
    }

    @Test(timeout = 100)
    public void waitUntilReadyForInputAndIsCurrentlyReady_returnsImmediately() {
        inputState.expectingInput();
        inputState.waitUntilReadyForInput();
    }

    @Test
    public void waitUntilReadyForInputAndNotCurrentlyReady_waitsForStateChange() throws Exception {

        Thread waitingThread = new Thread(() -> inputState.waitUntilReadyForInput());

        waitingThread.start();
        waitingThread.join(100);
        assertTrue("Thread should still be running as it waits for input", waitingThread.isAlive());

        inputState.expectingInput();
        waitingThread.join(100);
        assertFalse("Thread should have completed - no longer waiting", waitingThread.isAlive());
    }
}