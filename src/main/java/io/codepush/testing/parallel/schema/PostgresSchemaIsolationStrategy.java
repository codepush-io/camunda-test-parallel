package io.codepush.testing.parallel.schema;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * PostgreSQL implementation of schema isolation for parallel test execution.
 *
 * <p>Creates isolated database schemas with unique identifiers (UUID-based) to prevent
 * conflicts when multiple test classes run concurrently against the same database.</p>
 *
 * <p>Thread-safe for single test class execution context.</p>
 */
public final class PostgresSchemaIsolationStrategy implements SchemaIsolationStrategy {

    private static final String SCHEMA_PREFIX = "camunda_test_";

    private final DataSource dataSource;
    private volatile String currentSchema;

    public PostgresSchemaIsolationStrategy(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String createSchema() {
        if (currentSchema != null) {
            return currentSchema;
        }

        String schemaName = SCHEMA_PREFIX + UUID.randomUUID().toString();
        String sql = "CREATE SCHEMA IF NOT EXISTS " + schemaName;

        executeStatement(sql);
        currentSchema = schemaName;

        return schemaName;
    }

    @Override
    public String getCurrentSchemaName() {
        return currentSchema;
    }

    @Override
    public void cleanupSchema() {
        if (currentSchema == null) {
            return;
        }

        String sql = "DROP SCHEMA IF EXISTS " + currentSchema + " CASCADE";
        executeStatement(sql);
    }

    private void executeStatement(String sql) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(sql);

        } catch (SQLException e) {
            throw new SchemaIsolationException("Failed to execute SQL: " + sql, e);
        }
    }
}
