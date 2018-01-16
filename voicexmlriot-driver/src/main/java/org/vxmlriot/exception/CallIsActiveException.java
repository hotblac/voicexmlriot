package org.vxmlriot.exception;

/**
 * Thrown on attempt to create a new call when one is already active
 */
public class CallIsActiveException extends DriverException {
    public CallIsActiveException(String message) {
        super(message);
    }

    public CallIsActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
