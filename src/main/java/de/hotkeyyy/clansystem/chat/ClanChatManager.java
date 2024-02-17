package de.hotkeyyy.clansystem.chat;

import de.hotkeyyy.clansystem.ClanSystem;
import de.hotkeyyy.clansystem.data.ClanInfo;
import de.hotkeyyy.clansystem.data.ClanPlayerInfo;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ClanChatManager {
    public final HashMap<UUID, ChatType> currentChat = new HashMap<>();

    public final ClanSystem plugin;

    public ClanChatManager() {
        this.plugin = ClanSystem.instance;
    }

    public void sendClanChat(Player player, String message) {
        ClanInfo clan = plugin.databaseManager.getClanByPlayer(player);
        List<ClanPlayerInfo> members = plugin.databaseManager.getClanMembers(clan);
        members.stream().filter(member -> plugin.getServer().getPlayer(member.id) != null).forEach(member -> {
            plugin.getServer().getPlayer(member.id).sendMessage("§7[§aClan§7] §a" + player.getName() + "§7: §f" + message);
        });

    }


}
