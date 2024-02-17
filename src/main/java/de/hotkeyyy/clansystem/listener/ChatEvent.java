package de.hotkeyyy.clansystem.listener;

import de.hotkeyyy.clansystem.ClanSystem;
import de.hotkeyyy.clansystem.chat.ChatType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvent implements Listener {

    final ClanSystem plugin = ClanSystem.instance;

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        ChatType currentChat = plugin.clanChatManager.currentChat.computeIfAbsent(event.getPlayer().getUniqueId(), k -> ChatType.GLOBAL_CHAT);
        if (currentChat.equals(ChatType.CLAN_CHAT)) {
            event.setCancelled(true);
            plugin.clanChatManager.sendClanChat(event.getPlayer(), event.getMessage());
        }
    }
}
