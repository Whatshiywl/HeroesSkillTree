package me.Whatshiywl.heroesskilltree.commands;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Multitallented
 */
public class SkillAdminCommand {
    public static void skillAdmin(HeroesSkillTree hst, CommandSender sender, String[] args) {
        if(args.length > 1 && args[0].equalsIgnoreCase("reset")){
            if(!sender.hasPermission("skilladmin.reset")) {
                sender.sendMessage(ChatColor.RED + "You don't have enough permissions!");
                return;
            }
            if(args.length == 2){
                if(Bukkit.getPlayer(args[1]) != null){
                    hst.resetPlayer(Bukkit.getPlayer(args[1]));
                    sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA +
                            "You have reset " + args[1]);
                } else {
                    sender.sendMessage(ChatColor.RED + "Sorry, " + args[1] + " is not online.");
                }
            } else{
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
                    return;
                }
                hst.resetPlayer((Player) sender);
                sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You have reset yourself.");
            }
            return;
        }
        sender.sendMessage(ChatColor.RED + "/skilladmin reset <player>");
    }
}
