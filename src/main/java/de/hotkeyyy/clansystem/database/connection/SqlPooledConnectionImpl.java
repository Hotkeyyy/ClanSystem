package de.hotkeyyy.clansystem.database.connection;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlPooledConnectionImpl implements PooledConnection {
    private final Connection connection;
    private final long expirationTimeMillis;
    private long lastAccessTime;


    public SqlPooledConnectionImpl(Connection connection, long expirationTimeMillis) {
        this.connection = connection;
        this.expirationTimeMillis = expirationTimeMillis;
        this.lastAccessTime = System.currentTimeMillis();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public boolean isExpired(long now) throws SQLException {
        try {
            if (now - lastAccessTime > expirationTimeMillis || !connection.isValid(3)) {
                return true;
            }
        } catch (SQLException e) {
            connection.close();
            return true;
        }
        return false;
    }

    @Override
    public void updateLastAccessTime() {
        lastAccessTime = System.currentTimeMillis();
    }

    @Override
    public void close() {
        try {
            if (connection.isClosed()) return;
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
