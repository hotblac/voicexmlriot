package org.vxmlriot.exception;

public class CallIsActiveException extends DriverException {
    public CallIsActiveException(String message) {
        super(message);
    }

    public CallIsActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
