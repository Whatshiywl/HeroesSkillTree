package me.Whatshiywl.heroesskilltree.commands;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Multitallented
 */
public class SkillLockedCommand {
    public static void skillList(HeroesSkillTree hst, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
            return;
        }
        Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player) sender);
        
        int j=0;
        HashMap<String, Skill> skills = new HashMap<>();
        ArrayList<String> alphabeticalSkills = new ArrayList<>();
        if (hero.getHeroClass() != null) {
            for (String skillName : hero.getHeroClass().getSkillNames()) {
                Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(skillName);
                if (skill == null || !shouldListSkill(hst, hero, skill)) {
                    continue;
                }
                String message = ChatColor.GREEN + skillName + ChatColor.GRAY;
                if (hst.getStrongParentSkills(hero, skill) != null) {
                    for (String s : hst.getStrongParentSkills(hero, skill)) {
                        message += ", s:" + s;
                    }
                }
                if (hst.getWeakParentSkills(hero, skill) != null) {
                    for (String s : hst.getWeakParentSkills(hero, skill)) {
                        message += ", w:" + s;
                    }
                }
                skills.put(skillName, skill);
                alphabeticalSkills.add(message);
            }
        }
        /*if (hero.getSecondClass() != null) {
            for (String skillName : hero.getSecondClass().getSkillNames()) {
                Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(skillName);
                if (!shouldListSkill(hst, hero, skill)) {
                    continue;
                }
                skills.put(skillName, skill);
                alphabeticalSkills.add(skillName);
            }
        }*/
        Collections.sort(alphabeticalSkills);
        int k = 0;
        int t = 0;
        if (args.length > 0 && !args[0].equalsIgnoreCase("1")) {
            try {
                t = Integer.parseInt(args[0]);
                t = t < 2 ? 1 : t;
                k = (t - 1) * 10;
            } catch (NumberFormatException nfe) {
                k=0;
            }
        } else {
            t=1;
        }
        sender.sendMessage(ChatColor.GOLD + "[HST] Unlockable skills list page " + t + "/" + (Math.round(((double) alphabeticalSkills.size()) / 10)));
        for (int i = k; j < 10 && i < alphabeticalSkills.size(); i++) {
            if (j > 9) {
                break;
            }
            String name = alphabeticalSkills.get(i);
            sender.sendMessage(name);
            /*if (skills.containsKey(name)) {
                sender.sendMessage(ChatColor.GREEN + name + ": " + ChatColor.GRAY + skills.get(name).getDescription(hero));
            }*/
            j++;
        }
    }

    private static boolean shouldListSkill(HeroesSkillTree hst, Hero hero, Skill skill) {
        if (skill == null) {
            return false;
        }
        
        if (!hst.isLocked(hero, skill)) {
            return false;
        }
        
        if(!hero.hasAccessToSkill(skill) || !hero.canUseSkill(skill)){
            return false;
        }
        List<String> strongParents = hst.getStrongParentSkills(hero, skill);
        boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
        List<String> weakParents = hst.getWeakParentSkills(hero, skill);
        boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
        if(!hasStrongParents && !hasWeakParents) {
            return false;
        }
        if(hasStrongParents){
            for(String name : hst.getStrongParentSkills(hero, skill)){
                if(hst.isLocked(hero, HeroesSkillTree.heroes.getSkillManager().getSkill(name))) {
                    return false;
                }
            }
        }
        if(hasWeakParents){
            for(String name : hst.getWeakParentSkills(hero, skill)) {
                if(!hst.isLocked(hero, HeroesSkillTree.heroes.getSkillManager().getSkill(name))) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
