package de.hotkeyyy.clansystem.database.connection;

import java.sql.Connection;
import java.sql.SQLException;
/**
 * Represents a pooled connection.
 */
public interface PooledConnection {
    /**
     * Retrieves the connection.
     * @return The connection.
     */
    Connection getConnection();
    /**
     * Checks if the connection is expired based on the provided time.
     * @param now The current time in milliseconds.
     * @return True if the connection is expired, false otherwise.
     */
    boolean isExpired(long now) throws SQLException;
    /**
     * Updates the last access time of the connection to the current time.
     */
    void updateLastAccessTime();
    /**
     * Closes the connection.
     */
    void close();
}
