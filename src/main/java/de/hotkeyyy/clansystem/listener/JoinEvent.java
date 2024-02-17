package de.hotkeyyy.clansystem.listener;

import de.hotkeyyy.clansystem.ClanSystem;
import de.hotkeyyy.clansystem.chat.ChatType;
import de.hotkeyyy.clansystem.data.ClanPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {

    ClanSystem plugin = ClanSystem.instance;

    /**
     * This method is called when a player joins the server.
     * It checks if the player is already in the database and if not, it creates a new entry for the player.
     * @param event The event that is called when a player joins the server.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!plugin.clanChatManager.currentChat.containsKey(player.getUniqueId())) {
            plugin.clanChatManager.currentChat.put(player.getUniqueId(), ChatType.GLOBAL_CHAT);
        }

        Bukkit.getScheduler().runTaskAsynchronously(de.hotkeyyy.clansystem.ClanSystem.instance, () -> {
            if (!plugin.databaseManager.existPlayer(event.getPlayer())) {
                ClanPlayerInfo playerInfo = plugin.databaseManager.createPlayer(event.getPlayer());
                plugin.databaseManager.playerInfoCache.put(playerInfo.id, playerInfo);
            } else {
                ClanPlayerInfo playerInfo = plugin.databaseManager.getPlayerInfo(event.getPlayer());
                plugin.databaseManager.playerInfoCache.put(playerInfo.id, playerInfo);
            }
        });
    }

}
