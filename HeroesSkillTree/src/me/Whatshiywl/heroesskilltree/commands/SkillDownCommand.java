package me.Whatshiywl.heroesskilltree.commands;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;
import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Multitallented
 */
public class SkillDownCommand {
    public static void skillDown(HeroesSkillTree hst, CommandSender sender, String args[]) {
        if(!sender.hasPermission("skilltree.down")){
            sender.sendMessage(ChatColor.RED + "You don't have enough permissions!");
            return;
        }
        if(args.length < 1) {
            sender.sendMessage(ChatColor.RED + "No skill given: /skilldown (skill) [amount]");
            return;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
            return;
        }
        Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player) sender);
        
        if(!hero.hasAccessToSkill(args[0])) {
            sender.sendMessage(ChatColor.RED + "You don't have this skill");
            return;
        }
        
        Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(args[0]);
        int pointsDecrease;
        try {
            pointsDecrease = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        } catch (NumberFormatException nfe) {
            sender.sendMessage(ChatColor.RED + "Please enter a number of points to increase");
            return;
        }
        
        if(hst.getSkillLevel(hero, skill) < pointsDecrease) {
            sender.sendMessage(ChatColor.RED + "This skill is not a high enough level");
            return;
        }
        
        if(hst.getSkillLevel(hero, skill) - pointsDecrease < 1){
            if(!sender.hasPermission("skilltree.lock")){
                sender.sendMessage(ChatColor.RED + "You don't have enough permissions!");
                return;
            }
            hst.setSkillLevel(hero, skill, hst.getSkillLevel(hero, skill) - pointsDecrease);
            hero.removeEffect(hero.getEffect(skill.getName()));
            hst.savePlayerConfig(sender.getName());
            sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You have locked " + skill.getName() + "!");
        } else {
            if(!sender.hasPermission("skilltree.down")){
                sender.sendMessage(ChatColor.RED + "You don't have enough permissions!");
                return;
            }
            hst.setSkillLevel(hero, skill, hst.getSkillLevel(hero, skill) - pointsDecrease);
            hst.savePlayerConfig(sender.getName());
            sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + skill.getName() +
                    "leveled down: " + hst.getSkillLevel(hero, skill) + "/" + hst.getSkillMaxLevel(hero, skill));
        }
    }
}
