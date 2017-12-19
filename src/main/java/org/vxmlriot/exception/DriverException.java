package org.vxmlriot.exception;

/**
 * Exception thrown on failure of the VXML driver
 */
public class DriverException extends Exception {
    public DriverException(String message) {
        super(message);
    }

    public DriverException(String message, Throwable cause) {
        super(message, cause);
    }
}
