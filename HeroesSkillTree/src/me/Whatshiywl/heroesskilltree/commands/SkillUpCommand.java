package me.Whatshiywl.heroesskilltree.commands;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.skill.Skill;
import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Multitallented
 */
public class SkillUpCommand {
    
    /**
     * Increments the player's skill by 1 point. If that skill is mastered, then
     * it will unlock all child skills.
     * @param sender
     * @param args (skill) [amount]
     */
    public static void skillUp(HeroesSkillTree hst, CommandSender sender, String[] args) {
        //Permission Check
        if(!sender.hasPermission("skilltree.up")){
            sender.sendMessage(ChatColor.RED + "You don't have enough permissions!");
            return;
        }

        //Args Length Check
        if(args.length < 1) {
            sender.sendMessage(ChatColor.RED + "No skill given: /skillup (skill) [amount]");
            return;
        }
        
        //Is Player Check
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
            return;
        }
        Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player) sender);
        
        //Has Access Check
        if(!hero.hasAccessToSkill(args[0])) {
            sender.sendMessage(ChatColor.RED + "You don't have this skill");
            return;
        }
        Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(args[0]);
        
        //Has SkillPoints Check
        if (hst.getSkillMaxLevel(hero, skill) == -1) {
            sender.sendMessage(ChatColor.RED + "This skill can't be increased");
            return;
        }
        int pointsToIncrease;
        try {
            pointsToIncrease = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        } catch (NumberFormatException nfe) {
            sender.sendMessage(ChatColor.RED + "Please enter a number of points to increase");
            return;
        }
        
        //Point Check
        if(hst.getPlayerPoints(hero) < pointsToIncrease) {
            sender.sendMessage(ChatColor.RED + "You don't have enough SkillPoints.");
            return;
        }
        
        //Mastery Check
        if(hst.getSkillMaxLevel(hero, skill) < hst.getSkillLevel(hero, skill) + pointsToIncrease) {
            sender.sendMessage(ChatColor.RED + "This skill has already been mastered.");
            return;
        }
        
        //Lock Check
        if (hst.isLocked(hero, skill) && !hst.canUnlock(hero, skill)) {
            sender.sendMessage(ChatColor.RED + "You can't unlock this skill! /skillinfo (skill) to see requirements.");
            return;
        }
        
        //Override Check
        if (!sender.hasPermission("skilltree.override.usepoints")) {
            hst.setPlayerPoints(hero, hst.getPlayerPoints(hero) - pointsToIncrease);
        }
        
        hst.setSkillLevel(hero, skill, hst.getSkillLevel(hero, skill) + pointsToIncrease);
        hst.savePlayerConfig();
        hero.addEffect(new Effect(skill, skill.getName()));
        
        
        if (hst.isLocked(hero, skill)) {
            sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA +
                    "You have unlocked " + skill.getName() + "! Level: " +
                    hst.getSkillLevel(hero, skill));
        } else if (hst.isMastered(hero, skill)) {
            sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.GREEN +
                    "You have mastered " + skill.getName() + " at level " +
                    hst.getSkillLevel(hero, skill) + "!");
        } else {
            sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + skill.getName() +
                " leveled up: " + hst.getSkillLevel(hero, skill) + "/" + hst.getSkillMaxLevel(hero, skill));
        }
    }
}
