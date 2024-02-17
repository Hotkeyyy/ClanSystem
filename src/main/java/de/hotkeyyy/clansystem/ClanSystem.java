package de.hotkeyyy.clansystem;

import de.hotkeyyy.clansystem.chat.ClanChatManager;
import de.hotkeyyy.clansystem.clan.ClanManager;
import de.hotkeyyy.clansystem.commands.ClanCommand;
import de.hotkeyyy.clansystem.commands.ToggleChatCommand;
import de.hotkeyyy.clansystem.database.Databasemanager;
import de.hotkeyyy.clansystem.database.connection.SqlConnectionPoolImpl;
import de.hotkeyyy.clansystem.listener.ChatEvent;
import de.hotkeyyy.clansystem.listener.JoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * The main class of the plugin.
 */
public final class ClanSystem extends JavaPlugin {

    public static ClanSystem instance;
    public ClanManager clanManager;
    public Databasemanager databaseManager;
    public SqlConnectionPoolImpl connectionPool;
    public ClanChatManager clanChatManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.clanChatManager = new ClanChatManager();
        setConfigDefaults();

        try {
            Class.forName(getConfig().getString("sql.driver"));
        } catch (ClassNotFoundException e) {
            Bukkit.getConsoleSender().sendMessage("§cError while loading driver!!");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        this.connectionPool = new SqlConnectionPoolImpl(
                getConfig().getString("sql.host"),
                getConfig().getString("sql.port"),
                getConfig().getString("sql.database"),
                getConfig().getString("sql.driver"),
                getConfig().getString("sql.username"),
                getConfig().getString("sql.password"),
                getConfig().getInt("sql.pool.poolsize"),
                30000L
        );
        databaseManager = new Databasemanager(
                connectionPool
        );

        try {
            databaseManager.createTables();
        } catch (ExecutionException | SQLException e) {
            throw new RuntimeException(e);
        }
        clanManager = new ClanManager();
        registerCommands();
        registerEvents();

    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.kickPlayer("§lReload");
        });
        databaseManager.shutdown();
    }

    private void setConfigDefaults() {
        FileConfiguration config = this.getConfig();
        config.addDefault("sql.host", "localhost");
        config.addDefault("sql.port", 3306);
        config.addDefault("sql.database", "clansystem");
        config.addDefault("sql.username", "root");
        config.addDefault("sql.password", "password");
        config.addDefault("sql.driver", "com.mysql.jdbc.Driver");
        config.addDefault("sql.pool.poolsize", 3);
        config.options().copyDefaults(true);
        this.saveConfig();
    }

    private void registerCommands() {
        getCommand("clan").setExecutor(new ClanCommand());
        getCommand("togglechat").setExecutor(new ToggleChatCommand());
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new JoinEvent(), this);
        Bukkit.getPluginManager().registerEvents(new ChatEvent(), this);
    }

}

