package de.hotkeyyy.clansystem.clan;

import de.hotkeyyy.clansystem.ClanSystem;
import de.hotkeyyy.clansystem.chat.ChatType;
import de.hotkeyyy.clansystem.data.ClanInfo;
import de.hotkeyyy.clansystem.data.ClanPlayerInfo;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClanManager {

    public final ClanSystem plugin = ClanSystem.instance;
    public final HashMap<Player, ClanInfo> pendingInvites = new HashMap<>();

    public void handleLeave(Player player) {
        if (plugin.databaseManager.existPlayer(player) && plugin.databaseManager.getPlayerInfo(player.getUniqueId()).clanID != 0) {
            ClanInfo clan = plugin.databaseManager.getClanByPlayer(player);
            if (plugin.databaseManager.isClanOwner(player)) {
                List<ClanPlayerInfo> members = plugin.databaseManager.getClanMembers(clan);
                if (members.size() > 1) {
                    ClanPlayerInfo newOwner = members.stream()
                            .filter(member -> !Objects.equals(member.name, player.getName()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No new owner found"));
                    plugin.databaseManager.changeClanOwner(clan, newOwner);
                    Player newOwnerPlayer = Bukkit.getPlayer(newOwner.id);
                    if (newOwnerPlayer != null) newOwnerPlayer.sendMessage("§aDu bist nun der Owner des Clans!");
                } else {
                    player.sendMessage("§cDu bist der letzte Spieler in deinem Clan! Lösche den Clan mit /clan delete!");
                    return;
                }
            }
            plugin.clanChatManager.currentChat.put(player.getUniqueId(), ChatType.GLOBAL_CHAT);
            plugin.databaseManager.leaveClan(player);
            player.sendMessage("§aDu hast den Clan erfolgreich verlassen!");
        } else {
            player.sendMessage("§cDu bist in keinem Clan!");
        }
    }


    public void handleDelete(Player player) {
        if (plugin.databaseManager.existPlayer(player)) {
            ClanPlayerInfo playerInfo = plugin.databaseManager.getPlayerInfo(player);
            if (playerInfo.clanID != 0) {
                if (plugin.databaseManager.isClanOwner(player)) {
                    ClanInfo clan = plugin.databaseManager.getClanByPlayer(player);
                    plugin.databaseManager.getClanMembers(clan).forEach(member -> {
                        plugin.databaseManager.leaveClan(member.id);
                        plugin.clanChatManager.currentChat.remove(member.id);
                        Player memberPlayer = Bukkit.getPlayer(member.id);
                        if (memberPlayer != null && memberPlayer.getUniqueId() != player.getUniqueId())
                            memberPlayer.sendMessage("§cDer Clan wurde gelöscht!");
                    });
                    plugin.databaseManager.leaveClan(player);
                    plugin.databaseManager.deleteClan(clan);
                    player.sendMessage("§aDu hast den Clan erfolgreich gelöscht!");
                } else {
                    player.sendMessage("§cDu bist nicht der Owner des Clans!");
                }
            } else {
                player.sendMessage("§cDu bist in keinem Clan!");
            }
        } else {
            player.sendMessage("§cDu bist in keinem Clan!");
        }
    }

    public void handleInfo(Player player) {
        if (plugin.databaseManager.existPlayer(player)) {
            ClanPlayerInfo playerInfo = plugin.databaseManager.getPlayerInfo(player);
            if (playerInfo.clanID != 0) {
                ClanInfo clanInfo = plugin.databaseManager.getClanByPlayer(player);
                ClanPlayerInfo clanOwnerInfo = plugin.databaseManager.getPlayerInfo(UUID.fromString(clanInfo.ownerID));
                List<ClanPlayerInfo> members = plugin.databaseManager.getClanMembers(clanInfo);
                StringBuilder message = new StringBuilder();
                message.append("§aClan: ").append(clanInfo.name);
                message.append("\n§aOwner: ").append(clanOwnerInfo.name);
                members.stream()
                        .filter(member -> !Objects.equals(member.name, clanOwnerInfo.name))
                        .forEach(member -> message.append("\n§aMitglied: ").append(member.name));

                player.sendMessage(message.toString());
            } else {
                player.sendMessage("§cDu bist in keinem Clan!");
            }
        }
    }

    public void handleCreate(Player player, String clanName) {
        if (!plugin.databaseManager.existPlayer(player)) {
            plugin.databaseManager.createPlayer(player);
        }

        if (plugin.databaseManager.getPlayerInfo(player).clanID != 0) {
            player.sendMessage("§cDu bist bereits in einem Clan!");
            return;
        }
        if (plugin.databaseManager.existClanName(clanName)) {
            player.sendMessage("§cDer Clanname existiert bereits!");
            return;
        }
        plugin.databaseManager.createClan(clanName, player);
        player.sendMessage("§aDu hast den Clan erfolgreich erstellt!");

    }

    public void handleInvite(Player player, String playerName) {
        boolean playerExists = plugin.databaseManager.existPlayer(player);
        if (!playerExists) {
            player.sendMessage("§cDu bist in keinem Clan!");
            return;
        }

        ClanPlayerInfo clanPlayerInfo = plugin.databaseManager.getPlayerInfo(player);
        if (clanPlayerInfo.clanID == 0) {
            player.sendMessage("§cDu bist in keinem Clan!");
            return;
        }

        Player receiver = Bukkit.getPlayerExact(playerName);
        if (receiver == null) {
            player.sendMessage("§cDer Spieler ist nicht online!");
            return;
        }

        if (receiver.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cDu kannst dich nicht selbst einladen!");
            return;
        }

        ClanInfo clan = plugin.databaseManager.getClanByPlayer(player);
        ClanPlayerInfo receiverInfo = plugin.databaseManager.getPlayerInfo(receiver);
        if (receiverInfo.clanID != 0) {
            player.sendMessage("§cDer Spieler ist bereits in einem Clan!");
            return;
        }

        pendingInvites.put(receiver, clan);
        player.sendMessage("§aDu hast den Spieler erfolgreich eingeladen!");
        final ComponentBuilder message = new ComponentBuilder("§dDu wurdest von " + player.getName() + " in den Clan eingeladen! ");
        message.append("§l§a[Accept]");
        message.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan join"));
        message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§aClick to accept")}));
        message.append("§4§l[Deny]");
        message.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan deny"));
        message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§4Click to deny")}));

        receiver.spigot().sendMessage(message.create());
    }

    public void handleKick(Player player, String playerName) {
        if (!plugin.databaseManager.existPlayer(player)) {
            player.sendMessage("§cDu bist in keinem Clan!");
            return;
        }

        ClanPlayerInfo clanPlayerInfo = plugin.databaseManager.getPlayerInfo(player);
        if (clanPlayerInfo.clanID == 0) {
            player.sendMessage("§cDu bist in keinem Clan!");
            return;
        }

        ClanInfo clan = plugin.databaseManager.getClanByPlayer(player);
        if (!plugin.databaseManager.isClanOwner(player)) {
            player.sendMessage("§cDu bist nicht der Owner des Clans!");
            return;
        }


        ClanPlayerInfo receiverInfo = plugin.databaseManager.getPlayerInfo(playerName);
        if (receiverInfo.clanID != clan.id) {
            player.sendMessage("§cDer Spieler ist nicht in deinem Clan!");
            return;
        }

        if (Objects.equals(receiverInfo.id, player.getUniqueId())) {
            player.sendMessage("§cDu kannst dich nicht selbst kicken!");
            return;
        }
        plugin.clanChatManager.currentChat.put(receiverInfo.id, ChatType.GLOBAL_CHAT);
        plugin.databaseManager.leaveClan(receiverInfo.id);
        player.sendMessage("§aDu hast den Spieler erfolgreich gekickt!");
        Player receiver = Bukkit.getPlayerExact(playerName);
        if (receiver != null) {
            receiver.sendMessage("§cDu wurdest aus dem Clan gekickt!");
        }
    }

    public void handleJoin(Player player) {
        ClanInfo clanInfo = plugin.clanManager.pendingInvites.get(player);
        if (clanInfo == null) {
            player.sendMessage("§cDu hast keine Einladung!");
            return;
        }
        if(plugin.databaseManager.getPlayerInfo(player).clanID != 0){
            player.sendMessage("§cDu bist bereits in einem Clan!");
            plugin.clanManager.pendingInvites.remove(player);
            return;
        }
        if(!plugin.databaseManager.existClan(clanInfo)){
            player.sendMessage("§cDer Clan existiert nicht mehr!");
            plugin.clanManager.pendingInvites.remove(player);
            return;
        }
        plugin.clanManager.pendingInvites.remove(player);
        plugin.databaseManager.addPlayerToClan(player, clanInfo);
        plugin.databaseManager.getClanMembers(clanInfo)
                .stream()
                .filter(member -> !member.id.equals(player.getUniqueId()) && Bukkit.getPlayer(member.id) != null)
                .map(member -> Bukkit.getPlayer(member.id))
                .forEach(memberPlayer -> memberPlayer.sendMessage("§a" + player.getName() + " ist dem Clan beigetreten!"));
        player.sendMessage("§aDu bist dem Clan beigetreten!");
    }

    public void handleDeny(Player player) {
        ClanInfo invitedClan = plugin.clanManager.pendingInvites.get(player);
        if (invitedClan == null) {
            player.sendMessage("§cDu hast keine Einladung!");
            return;
        }
        Player inviteSender = Bukkit.getPlayer(UUID.fromString(invitedClan.ownerID));
        if (inviteSender != null) {
            inviteSender.sendMessage("§c" + player.getName() + " hat die Einladung abgelehnt!");
        }
        plugin.clanManager.pendingInvites.remove(player);
        player.sendMessage("§cDu hast die Einladung abgelehnt!");
    }
}