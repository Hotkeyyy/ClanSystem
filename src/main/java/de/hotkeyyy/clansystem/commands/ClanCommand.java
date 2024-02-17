package de.hotkeyyy.clansystem.commands;

import de.hotkeyyy.clansystem.ClanSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class ClanCommand implements CommandExecutor {

    public final ClanSystem plugin = ClanSystem.instance;

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

