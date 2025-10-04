package io.codepush.testing.parallel.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PostgresSchemaIsolationStrategyTest {

    private DataSource mockDataSource;
    private Connection mockConnection;
    private Statement mockStatement;
    private PostgresSchemaIsolationStrategy strategy;

    @BeforeEach
    void setUp() throws Exception {
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        strategy = new PostgresSchemaIsolationStrategy(mockDataSource);
    }

    @Test
    void shouldCreateSchemaWithUniqueIdentifier() throws Exception {
        String schemaName = strategy.createSchema();

        assertThat(schemaName)
            .isNotNull()
            .startsWith("camunda_test_")
            .hasSize("camunda_test_".length() + 36); // UUID length

        // Verify UUID format in schema name
        String uuidPart = schemaName.substring("camunda_test_".length());
        UUID parsedUuid = UUID.fromString(uuidPart);
        assertThat(parsedUuid).isNotNull();
    }

    @Test
    void shouldExecuteCreateSchemaStatement() throws Exception {
        String schemaName = strategy.createSchema();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockStatement).execute(sqlCaptor.capture());

        assertThat(sqlCaptor.getValue())
            .isEqualTo("CREATE SCHEMA IF NOT EXISTS " + schemaName);
    }

    @Test
    void shouldCleanupSchemaUsingDropCascade() throws Exception {
        String schemaName = strategy.createSchema();
        strategy.cleanupSchema();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockStatement, times(2)).execute(sqlCaptor.capture());

        String dropStatement = sqlCaptor.getAllValues().get(1);
        assertThat(dropStatement)
            .isEqualTo("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
    }

    @Test
    void shouldCloseConnectionAfterSchemaCreation() throws Exception {
        strategy.createSchema();

        verify(mockConnection).close();
    }

    @Test
    void shouldCloseConnectionAfterSchemaCleanup() throws Exception {
        strategy.createSchema();
        strategy.cleanupSchema();

        verify(mockConnection, times(2)).close();
    }

    @Test
    void shouldCloseStatementAfterSchemaCreation() throws Exception {
        strategy.createSchema();

        verify(mockStatement).close();
    }

    @Test
    void shouldGetCurrentSchemaName() throws Exception {
        String createdSchema = strategy.createSchema();
        String currentSchema = strategy.getCurrentSchemaName();

        assertThat(currentSchema).isEqualTo(createdSchema);
    }

    @Test
    void shouldReturnNullWhenNoSchemaCreated() {
        String currentSchema = strategy.getCurrentSchemaName();

        assertThat(currentSchema).isNull();
    }

    @Test
    void shouldHandleMultipleCreateSchemaCallsIdempotently() throws Exception {
        String firstSchema = strategy.createSchema();
        String secondSchema = strategy.createSchema();

        assertThat(firstSchema).isEqualTo(secondSchema);
        verify(mockStatement, times(1)).execute(anyString());
    }
}
