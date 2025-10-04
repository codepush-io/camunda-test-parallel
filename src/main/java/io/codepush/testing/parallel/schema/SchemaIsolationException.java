package io.codepush.testing.parallel.schema;

/**
 * Exception thrown when schema isolation operations fail.
 */
public class SchemaIsolationException extends RuntimeException {

    public SchemaIsolationException(String message) {
        super(message);
    }

    public SchemaIsolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
