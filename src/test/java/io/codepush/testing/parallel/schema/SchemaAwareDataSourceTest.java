package io.codepush.testing.parallel.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SchemaAwareDataSourceTest {

    private DataSource mockDelegate;
    private Connection mockConnection;
    private Statement mockStatement;

    @BeforeEach
    void setUp() throws Exception {
        mockDelegate = mock(DataSource.class);
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);

        when(mockDelegate.getConnection()).thenReturn(mockConnection);
        when(mockDelegate.getConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
    }

    @Test
    void shouldSetSearchPathOnGetConnection() throws Exception {
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "camunda_test_abc123");

        Connection connection = dataSource.getConnection();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockStatement).execute(sqlCaptor.capture());
        assertThat(sqlCaptor.getValue()).isEqualTo("SET search_path TO camunda_test_abc123");
        assertThat(connection).isSameAs(mockConnection);
    }

    @Test
    void shouldSetSearchPathOnGetConnectionWithCredentials() throws Exception {
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "camunda_test_abc123");

        Connection connection = dataSource.getConnection("user", "pass");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockStatement).execute(sqlCaptor.capture());
        assertThat(sqlCaptor.getValue()).isEqualTo("SET search_path TO camunda_test_abc123");
        assertThat(connection).isSameAs(mockConnection);
        verify(mockDelegate).getConnection("user", "pass");
    }

    @Test
    void shouldSkipSetSearchPathWhenSchemaIsNull() throws Exception {
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, null);

        Connection connection = dataSource.getConnection();

        assertThat(connection).isSameAs(mockConnection);
        verify(mockConnection, never()).createStatement();
    }

    @Test
    void shouldSkipSetSearchPathWhenSchemaIsEmpty() throws Exception {
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "");

        Connection connection = dataSource.getConnection();

        assertThat(connection).isSameAs(mockConnection);
        verify(mockConnection, never()).createStatement();
    }

    @Test
    void shouldCloseStatementAfterSettingSearchPath() throws Exception {
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "camunda_test_abc123");

        dataSource.getConnection();

        verify(mockStatement).close();
    }

    @Test
    void shouldDelegateGetLogWriter() throws Exception {
        PrintWriter writer = new PrintWriter(System.out);
        when(mockDelegate.getLogWriter()).thenReturn(writer);
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "test_schema");

        assertThat(dataSource.getLogWriter()).isSameAs(writer);
    }

    @Test
    void shouldDelegateSetLogWriter() throws Exception {
        PrintWriter writer = new PrintWriter(System.out);
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "test_schema");

        dataSource.setLogWriter(writer);

        verify(mockDelegate).setLogWriter(writer);
    }

    @Test
    void shouldDelegateGetLoginTimeout() throws Exception {
        when(mockDelegate.getLoginTimeout()).thenReturn(30);
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "test_schema");

        assertThat(dataSource.getLoginTimeout()).isEqualTo(30);
    }

    @Test
    void shouldDelegateSetLoginTimeout() throws Exception {
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "test_schema");

        dataSource.setLoginTimeout(60);

        verify(mockDelegate).setLoginTimeout(60);
    }

    @Test
    void shouldDelegateGetParentLogger() throws Exception {
        Logger logger = Logger.getLogger("test");
        when(mockDelegate.getParentLogger()).thenReturn(logger);
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "test_schema");

        assertThat(dataSource.getParentLogger()).isSameAs(logger);
    }

    @Test
    void shouldDelegateIsWrapperFor() throws Exception {
        when(mockDelegate.isWrapperFor(DataSource.class)).thenReturn(true);
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "test_schema");

        assertThat(dataSource.isWrapperFor(DataSource.class)).isTrue();
        verify(mockDelegate).isWrapperFor(DataSource.class);
    }

    @Test
    void shouldDelegateUnwrap() throws Exception {
        when(mockDelegate.unwrap(DataSource.class)).thenReturn(mockDelegate);
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "test_schema");

        assertThat(dataSource.unwrap(DataSource.class)).isSameAs(mockDelegate);
        verify(mockDelegate).unwrap(DataSource.class);
    }

    @Test
    void shouldPropagateExceptionFromDelegate() throws Exception {
        when(mockDelegate.getConnection()).thenThrow(new SQLException("Connection failed"));
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "test_schema");

        assertThatThrownBy(dataSource::getConnection)
            .isInstanceOf(SQLException.class)
            .hasMessage("Connection failed");
    }

    @Test
    void shouldPropagateExceptionFromSetSearchPath() throws Exception {
        when(mockStatement.execute(anyString())).thenThrow(new SQLException("SET failed"));
        SchemaAwareDataSource dataSource = new SchemaAwareDataSource(mockDelegate, "test_schema");

        assertThatThrownBy(dataSource::getConnection)
            .isInstanceOf(SQLException.class)
            .hasMessage("SET failed");
    }
}
