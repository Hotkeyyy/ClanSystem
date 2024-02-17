package de.hotkeyyy.clansystem.commands;

import de.hotkeyyy.clansystem.ClanSystem;
import de.hotkeyyy.clansystem.data.ClanInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClanCommand implements CommandExecutor {

    public ClanSystem plugin = ClanSystem.instance;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        Bukkit.getScheduler().runTaskAsynchronously(ClanSystem.instance, () -> {

            switch (args.length) {
                case 0: {
                    sendHelpMessage(player);
                    break;
                }
                case 1: {
                    switch (args[0].toUpperCase()) {
                        case "LEAVE": {
                            plugin.clanManager.handleLeave(player);
                            break;
                        }
                        case "DELETE": {
                            plugin.clanManager.handleDelete(player);
                            break;
                        }
                        case "INFO": {
                            plugin.clanManager.handleInfo(player);
                            break;
                        }
                        case "JOIN": {
                            plugin.clanManager.handleJoin(player);
                            break;
                        }
                        case "DENY": {
                            plugin.clanManager.handleDeny(player);
                            break;
                        }
                        default:
                            sendHelpMessage(player);
                    }
                    break;
                }
                case 2: {
                    switch (args[0].toUpperCase()) {
                        case "CREATE": {
                            String clanName = args[1];
                            plugin.clanManager.handleCreate(player, clanName);
                            break;
                        }
                        case "INVITE": {
                            plugin.clanManager.handleInvite(player, args[1]);
                            break;
                        }
                        case "KICK": {
                            plugin.clanManager.handleKick(player, args[1]);
                            break;
                        }
                        default:
                            sendHelpMessage(player);
                    }
                    break;
                }

                default:
                    sendHelpMessage(player);
            }

//            if (args.length == 0) {
//                sendHelpMessage(player);
//
//            } else if (args.length == 1) {
//                if (args[0].equalsIgnoreCase("leave")) {
//                    //leave clan
//                    plugin.clanManager.handleLeave(player);
//                } else if (args[0].equalsIgnoreCase("delete")) {
//                    plugin.clanManager.handleDelete(player);
//                    //delete clan
//                } else if (args[0].equalsIgnoreCase("info")) {
//
//                    plugin.clanManager.handleInfo(player);
//                }
//            } else if (args.length == 2) {
//                if (args[0].equalsIgnoreCase("create")) {
//                    //create clan
//                    String clanName = args[1];
//                    //    ClanSystem.databasemanager.createClan(clanName, player);
//                    plugin.clanManager.handleCreate(player, clanName);
//                } else if (args[0].equalsIgnoreCase("invite")) {
//                    //invite player
//                    plugin.clanManager.handleInvite(player, args[1]);
//                } else if (args[0].equalsIgnoreCase("kick")) {
//                    //kick player
//                    plugin.clanManager.handleKick(player, args[1]);
//                } else if (args[0].equalsIgnoreCase("join")) {
//                    ClanInfo clanInfo = plugin.clanManager.pendingInvites.get(player);
//                    if (clanInfo == null) {
//                        player.sendMessage("§cDu hast keine Einladung!");
//                        return;
//                    }
//                    plugin.clanManager.pendingInvites.remove(player);
//                    plugin.databaseManager.addPlayerToClan(player, clanInfo);
//                    player.sendMessage("§aDu bist dem Clan beigetreten!");
//                } else if (args[0].equalsIgnoreCase("deny")) {
//                    ClanInfo invitedClan = plugin.clanManager.pendingInvites.get(player);
//                    if (invitedClan == null) {
//                        player.sendMessage("§cDu hast keine Einladung!");
//                        return;
//                    }
//                    Player inviteSender = Bukkit.getPlayer(UUID.fromString(invitedClan.ownerID));
//                    if (inviteSender != null) {
//                        inviteSender.sendMessage("§c" + player.getName() + " hat die Einladung abgelehnt!");
//                    }
//                    plugin.clanManager.pendingInvites.remove(player);
//                    player.sendMessage("§cDu hast die Einladung abgelehnt!");
//
//                } else sendHelpMessage(player);
         //   }
        });
        return true;

    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§c/clan create <name>");
        player.sendMessage("§c/clan invite <player>");
        player.sendMessage("§c/clan kick <player>");
        player.sendMessage("§c/clan leave");
        player.sendMessage("§c/clan delete");
        player.sendMessage("§c/clan info");
    }


}
