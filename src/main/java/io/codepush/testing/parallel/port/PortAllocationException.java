package io.codepush.testing.parallel.port;

/**
 * Exception thrown when port allocation operations fail.
 */
public class PortAllocationException extends RuntimeException {

    public PortAllocationException(String message) {
        super(message);
    }

    public PortAllocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
