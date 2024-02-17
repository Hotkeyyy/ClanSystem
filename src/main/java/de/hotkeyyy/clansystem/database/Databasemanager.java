package de.hotkeyyy.clansystem.database;

import de.hotkeyyy.clansystem.data.ClanInfo;
import de.hotkeyyy.clansystem.data.ClanPlayerInfo;
import de.hotkeyyy.clansystem.data.cache.ClanInfoCache;
import de.hotkeyyy.clansystem.data.cache.PlayerInfoCache;
import de.hotkeyyy.clansystem.database.connection.SqlConnectionPoolImpl;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DatabaseManager {

    final SqlConnectionPoolImpl connectionPool;
    public final ClanInfoCache clanInfoCache;
    public final PlayerInfoCache playerInfoCache;


    public DatabaseManager(SqlConnectionPoolImpl connectionPool) {
        this.connectionPool = connectionPool;
        this.clanInfoCache = new ClanInfoCache();
        this.playerInfoCache = new PlayerInfoCache();
    }


    public void shutdown() {
        clanInfoCache.clear();
        playerInfoCache.clear();
        connectionPool.close();
    }

    public void createTables() throws SQLException {
        Connection connection = connectionPool.getConnection();

        PreparedStatement queryStatement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS Player ( player_id VARCHAR(50) PRIMARY KEY, player_name VARCHAR(50) NOT NULL, clan_id INT, UNIQUE KEY (player_id));"
        );
        queryStatement.executeUpdate();

        queryStatement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS Clans ( clan_id INT AUTO_INCREMENT PRIMARY KEY, clan_name VARCHAR(50) NOT NULL, owner_id VARCHAR(50), CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES Player(player_id) );"
        );

        queryStatement.executeUpdate();
        queryStatement.close();
        connectionPool.releaseConnection(connection);
    }

    public void createClan(String name, Player owner) {
        try {

            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Clans (clan_name, owner_id) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, owner.getUniqueId().toString());
            preparedStatement.executeUpdate();
            ResultSet keys = preparedStatement.getGeneratedKeys();
            int clanId = 0;
            if (keys.next()) {
                clanId = keys.getInt(1);
            }

            preparedStatement.close();
            connectionPool.releaseConnection(connection);
            ClanInfo createdClan = new ClanInfo(clanId, name, owner.getUniqueId().toString());
            addPlayerToClan(owner, createdClan);
            clanInfoCache.put(createdClan.id, createdClan);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean existPlayer(Player player) {
        if(playerInfoCache.contains(player.getUniqueId())) return true;
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE player_id = ?");
            preparedStatement.setString(1, player.getUniqueId().toString());
            boolean result = preparedStatement.executeQuery().next();
            preparedStatement.close();
            connectionPool.releaseConnection(connection);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ClanPlayerInfo createPlayer(Player player) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Player (player_id, player_name, clan_id) VALUES (?, ?, ?)");
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.setString(2, player.getName());
            preparedStatement.setInt(3, 0);
            this.executeUpdate(preparedStatement, connection);
            ClanPlayerInfo playerInfo = new ClanPlayerInfo(player.getUniqueId(), player.getName(), 0);
            playerInfoCache.put(player.getUniqueId(), playerInfo);
            return playerInfo;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addPlayerToClan(Player player, ClanInfo clan) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Player SET clan_id = (SELECT clan_id FROM Clans WHERE clan_name = ?) WHERE player_id = ?");
            preparedStatement.setString(1, clan.name);
            preparedStatement.setString(2, player.getUniqueId().toString());
            this.executeUpdate(preparedStatement, connection);
            playerInfoCache.update(new ClanPlayerInfo(player.getUniqueId(), player.getName(), clan.id));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ClanInfo getClanByPlayer(Player player) {
        ClanPlayerInfo playerInfo = playerInfoCache.get(player.getUniqueId()).orElseGet(() -> createPlayer(player));
        Optional<ClanInfo> clanInfoOptional = clanInfoCache.get(playerInfo.clanID);
        if(clanInfoOptional.isPresent()) return clanInfoOptional.get();

        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Clans WHERE clan_id = (SELECT clan_id FROM Player WHERE player_id = ?)");
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet rs = preparedStatement.executeQuery();
            connectionPool.releaseConnection(connection);
            rs.first();
            ClanInfo clanInfo = new ClanInfo(rs.getInt("clan_id"), rs.getString("clan_name"), rs.getString("owner_id"));
            preparedStatement.close();
            rs.close();
            clanInfoCache.put(clanInfo.id, clanInfo);
            return clanInfo;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }




    public void leaveClan(Player player) {
        leaveClan(player.getUniqueId());
    }

    public void leaveClan(UUID uuid) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Player SET clan_id = 0 WHERE player_id = ?");
            preparedStatement.setString(1, uuid.toString());
            this.executeUpdate(preparedStatement, connection);
            playerInfoCache.update(new ClanPlayerInfo(uuid, getPlayerInfo(uuid).name, 0));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public boolean isClanOwner(Player player) {
        try {
            ClanPlayerInfo playerInfo = playerInfoCache.get(player.getUniqueId()).orElseGet(() -> createPlayer(player));

                Optional<ClanInfo> clanInfo = clanInfoCache.get(playerInfo.clanID);
                if(clanInfo.isPresent()) {
                    return clanInfo.get().ownerID.equals(player.getUniqueId().toString());
                }

            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Clans WHERE owner_id = ?");
            preparedStatement.setString(1, player.getUniqueId().toString());
            boolean result = preparedStatement.executeQuery().next();
            preparedStatement.close();
            connectionPool.releaseConnection(connection);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void changeClanOwner(ClanInfo clan, ClanPlayerInfo newOwner) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Clans SET owner_id = ? WHERE clan_id = ?");
            preparedStatement.setString(1, newOwner.id.toString());
            preparedStatement.setInt(2, clan.id);
            this.executeUpdate(preparedStatement, connection);
            clanInfoCache.update(new ClanInfo(clan.id, clan.name, newOwner.id.toString()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ClanPlayerInfo> getClanMembers(ClanInfo clan) {
        List<ClanPlayerInfo> clanMemberNames = new ArrayList<>();

        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE clan_id = ?");
            preparedStatement.setInt(1, clan.id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                clanMemberNames.add(new ClanPlayerInfo(UUID.fromString(rs.getString("player_id")), rs.getString("player_name"), rs.getInt("clan_id")));
            }
            preparedStatement.close();
            rs.close();
            connectionPool.releaseConnection(connection);
            return clanMemberNames;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public ClanPlayerInfo getPlayerInfo(UUID uuid) {
        if(playerInfoCache.contains(uuid)) return playerInfoCache.get(uuid).orElse(null);

        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE player_id = ?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet rs = preparedStatement.executeQuery();
            connectionPool.releaseConnection(connection);
            rs.first();
            ClanPlayerInfo clanPlayerInfo = new ClanPlayerInfo(UUID.fromString(rs.getString("player_id")), rs.getString("player_name"), rs.getInt("clan_id"));
            preparedStatement.close();
            rs.close();
            playerInfoCache.put(uuid, clanPlayerInfo);
            return clanPlayerInfo;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ClanPlayerInfo getPlayerInfo(Player player) {
        return getPlayerInfo(player.getUniqueId());
    }

public ClanPlayerInfo getPlayerInfo(String name) {
    Optional<ClanPlayerInfo> clanPlayerInfoOptional = playerInfoCache.getMap().values().stream().filter(clanPlayerInfo -> clanPlayerInfo.name.equals(name)).findAny();
        if(clanPlayerInfoOptional.isPresent()){
            return clanPlayerInfoOptional.get();
        }

    try {
        Connection connection = connectionPool.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE player_name = ?");
        preparedStatement.setString(1, name);
        ResultSet rs = preparedStatement.executeQuery();
        connectionPool.releaseConnection(connection);
        rs.first();
        ClanPlayerInfo clanPlayerInfo = new ClanPlayerInfo(UUID.fromString(rs.getString("player_id")), rs.getString("player_name"), rs.getInt("clan_id"));
        preparedStatement.close();
        rs.close();
        playerInfoCache.put(clanPlayerInfo.id, clanPlayerInfo);
        return clanPlayerInfo;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

    public boolean existClanName(String name) {
        try {

            if(clanInfoCache.getMap().values().stream().anyMatch(clanInfo -> clanInfo.name.equals(name))) return true;

            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Clans WHERE clan_name = ?");
            preparedStatement.setString(1, name);
            boolean result = preparedStatement.executeQuery().next();
            preparedStatement.close();
            connectionPool.releaseConnection(connection);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existClan(ClanInfo clanInfo) {
        if (clanInfoCache.contains(clanInfo.id)) return true;
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Clans WHERE clan_id = ?");
            preparedStatement.setInt(1, clanInfo.id);
            boolean result = preparedStatement.executeQuery().next();
            preparedStatement.close();
            connectionPool.releaseConnection(connection);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }

       return false;
    }

    public void deleteClan(ClanInfo clan) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Clans WHERE clan_id = ?");
            preparedStatement.setInt(1, clan.id);
            this.executeUpdate(preparedStatement, connection);

            clanInfoCache.remove(clan.id);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void executeUpdate(PreparedStatement preparedStatement, Connection connection) {
        try {
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connectionPool.releaseConnection(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
