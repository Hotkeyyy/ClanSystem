package de.hotkeyyy.clansystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ClanTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(!command.getName().equalsIgnoreCase("CLAN")) return null;
        if(!(commandSender instanceof Player)) return null;
        List<String> completions = new ArrayList<>();
        if(args.length == 1) {
            completions.add("CREATE");
            completions.add("LEAVE");
            completions.add("DELETE");
            completions.add("INFO");
            completions.add("INVITE");
            completions.add("KICK");
            completions.add("JOIN");
            completions.add("DENY");
        }else if(args.length == 2){
            if(args[0].equalsIgnoreCase("INVITE") || args[0].equalsIgnoreCase("KICK") ){
                commandSender.getServer().getOnlinePlayers().stream().filter(player -> !player.getUniqueId().equals(((Player) commandSender).getUniqueId())).forEach(player -> completions.add(player.getName()));
            }
        }
        return completions;
    }
}
