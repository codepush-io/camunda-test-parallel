package io.codepush.testing.parallel.schema;

/**
 * Strategy interface for managing isolated database schemas in parallel test execution.
 *
 * <p>Implementations are responsible for creating unique schemas, managing their lifecycle,
 * and cleaning up resources after test completion.</p>
 */
public interface SchemaIsolationStrategy {

    /**
     * Creates a new isolated schema with a unique identifier.
     *
     * @return the name of the created schema
     * @throws SchemaIsolationException if schema creation fails
     */
    String createSchema();

    /**
     * Returns the current schema name for this test execution context.
     *
     * @return the schema name, or null if no schema has been created
     */
    String getCurrentSchemaName();

    /**
     * Removes the isolated schema and all its objects.
     *
     * <p>This method should be idempotent and safe to call multiple times.</p>
     *
     * @throws SchemaIsolationException if schema cleanup fails
     */
    void cleanupSchema();
}
