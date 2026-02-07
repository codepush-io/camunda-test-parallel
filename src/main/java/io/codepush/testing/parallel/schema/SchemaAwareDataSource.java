package io.codepush.testing.parallel.schema;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * A DataSource decorator that sets the PostgreSQL search_path to an isolated schema
 * on every connection obtained from the delegate.
 *
 * <p>This ensures that all JDBC operations performed through this DataSource target
 * the correct isolated schema, preventing cross-contamination between parallel tests.</p>
 *
 * <p>Thread-safe: thread safety is delegated to the underlying DataSource implementation.</p>
 */
public final class SchemaAwareDataSource implements DataSource {

    private final DataSource delegate;
    private final String schemaName;

    public SchemaAwareDataSource(DataSource delegate, String schemaName) {
        this.delegate = delegate;
        this.schemaName = schemaName;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = delegate.getConnection();
        setSearchPath(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = delegate.getConnection(username, password);
        setSearchPath(connection);
        return connection;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    private void setSearchPath(Connection connection) throws SQLException {
        if (schemaName == null || schemaName.isEmpty()) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + schemaName);
        }
    }
}
