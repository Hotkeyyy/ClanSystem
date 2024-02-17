package de.hotkeyyy.clansystem.database.connection;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SqlConnectionPoolImpl implements ConnectionPool {
    private final String url;
    private final String username;
    private final String password;
    public List<PooledConnection> availableConnections;
    public List<PooledConnection> usedConnections;
    private final long expirationTimeMillis;
    private final int poolSize;

    public SqlConnectionPoolImpl(String host, String port, String database, String driver, String username, String password, int poolSize, long expirationTimeMillis) {
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        ;
        this.username = username;
        this.password = password;
        this.poolSize = poolSize;
        this.expirationTimeMillis = expirationTimeMillis;
        this.availableConnections = Collections.synchronizedList(new ArrayList<>(poolSize));
        this.usedConnections = Collections.synchronizedList(new ArrayList<>(poolSize));

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            Bukkit.getConsoleSender().sendMessage("§4Error while loading driver");
        }

        initializePool();
    }

    private void initializePool() {
        for (int i = 0; i < poolSize; i++) {
            availableConnections.add(new SqlPooledConnectionImpl(createConnection(), expirationTimeMillis));
        }
    }

    private Connection createConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        long now = System.currentTimeMillis();

        if (availableConnections.isEmpty()) {
            PooledConnection connection = new SqlPooledConnectionImpl(createConnection(), expirationTimeMillis);
            usedConnections.add(connection);
            return connection.getConnection();
        } else {
            for (Iterator<PooledConnection> iterator = availableConnections.iterator(); iterator.hasNext(); ) {
                PooledConnection pooledConnection = iterator.next();
                if (availableConnections.isEmpty() && pooledConnection.isExpired(now)) {
                    iterator.remove();
                    pooledConnection.close();
                    continue;
                }
                availableConnections.remove(pooledConnection);
                usedConnections.add(pooledConnection);
                return pooledConnection.getConnection();
            }
        }
        return null;
    }

    @Override
    public synchronized void releaseConnection(Connection connection) throws SQLException {
        if (connection == null) return;
        for (PooledConnection pooledConnection : usedConnections) {
            if (pooledConnection.getConnection().equals(connection)) {
                usedConnections.remove(pooledConnection);
                pooledConnection.updateLastAccessTime();
                availableConnections.add(pooledConnection);
                return;
            }
        }
        connection.close();

    }

    @Override
    public void close() {
        for (PooledConnection pooledConnection : availableConnections) {
            pooledConnection.close();
        }
        for (PooledConnection pooledConnection : usedConnections) {
            pooledConnection.close();
        }
    }
}
