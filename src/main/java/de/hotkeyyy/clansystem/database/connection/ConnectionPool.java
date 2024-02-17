package de.hotkeyyy.clansystem.database.connection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents a connection pool.
 */
public interface ConnectionPool {
    /**
     * Retrieves a connection from the pool.
     *
     * @return The connection.
     * @throws SQLException If an SQL error occurs.
     */
    Connection getConnection() throws SQLException;

    /**
     * Releases the connection back to the pool.
     *
     * @param connection The connection to release.
     * @throws SQLException If an SQL error occurs.
     */
    void releaseConnection(Connection connection) throws SQLException;

    /**
     * Closes the connection pool.
     */
    void close();
}
