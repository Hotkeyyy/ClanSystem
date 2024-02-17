package de.hotkeyyy.clansystem.commands;

import de.hotkeyyy.clansystem.ClanSystem;
import de.hotkeyyy.clansystem.chat.ChatType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleChatCommand implements CommandExecutor {

    public final ClanSystem plugin = ClanSystem.instance;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        if (!player.hasPermission("clansystem.togglechat")) {
            player.sendMessage("§cDazu hast du keine Rechte.");
            return true;
        }
        if(strings.length != 0) {
            player.sendMessage("§cBenutze: /togglechat");
            return true;
        }
        Bukkit.getScheduler().runTaskAsynchronously(ClanSystem.instance, () -> {
            if (plugin.databaseManager.getPlayerInfo(player).clanID == 0) {
                player.sendMessage("§cDu bist in keinem Clan.");
                return;
            }

            if (!plugin.clanChatManager.currentChat.containsKey(player.getUniqueId())) {
                plugin.clanChatManager.currentChat.put(player.getUniqueId(), ChatType.CLAN_CHAT);
                player.sendMessage("§aDu bist nun im Clan Chat.");
                return;
            }

            ChatType currentChat = plugin.clanChatManager.currentChat.get(player.getUniqueId());

            switch (currentChat) {
                case CLAN_CHAT: {
                    plugin.clanChatManager.currentChat.put(player.getUniqueId(), ChatType.GLOBAL_CHAT);
                    player.sendMessage("§aDu bist nun im globalen Chat.");
                    break;
                }
                case GLOBAL_CHAT: {
                    plugin.clanChatManager.currentChat.put(player.getUniqueId(), ChatType.CLAN_CHAT);
                    player.sendMessage("§aDu bist nun im Clan Chat.");
                    break;
                }
            }
        });


        return true;

    }
}
