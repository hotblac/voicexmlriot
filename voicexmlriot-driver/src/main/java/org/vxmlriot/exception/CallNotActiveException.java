package org.vxmlriot.exception;

/**
 * Thrown when attempting a call operation when no call is active
 */
public class CallNotActiveException extends DriverException {
    public CallNotActiveException(String message) {
        super(message);
    }

    public CallNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
