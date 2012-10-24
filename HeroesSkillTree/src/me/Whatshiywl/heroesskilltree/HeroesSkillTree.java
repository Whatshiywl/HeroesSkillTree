package me.Whatshiywl.heroesskilltree;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import me.Whatshiywl.heroesskilltree.commands.SkillAdminCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillDownCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillInfoCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillListCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillLockedCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillUpCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HeroesSkillTree extends JavaPlugin {

    public final double VERSION = 0.1;
    
    public static final Logger logger = Logger.getLogger("Minecraft");
    public final EventListener HEventListener = new EventListener(this);

    public HeroesSkillTree plugin;
    public static Heroes heroes = (Heroes) Bukkit.getServer().getPluginManager().getPlugin("Heroes");
    public List<Skill> SkillStrongParents = new ArrayList<Skill>();
    public List<Skill> SkillWeakParents = new ArrayList<Skill>();
    private HashMap<String, FileConfiguration> playerConfigs = new HashMap<String, FileConfiguration>();
    private long lastSave = System.currentTimeMillis();

    private FileConfiguration heroesClassConfig = null;
    private File heroesClassConfigFile = null;

    @Override
    public void onDisable() {
        saveAll();
        logger.info("[HeroesSkillLevel] Has Been Disabled!");
    }

    @Override
    public void onEnable() {
        String message = "[HeroesSkillLevel] Version " + VERSION + " Has Been Enabled!";
        logger.info(message);
        PluginManager pm = getServer().getPluginManager();
        getConfig().options().copyDefaults(true);
        saveConfig();
        if(heroes.isEnabled()) {
            pm.registerEvents(this.HEventListener, this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        
        //SKILLUP
        if (commandLabel.equalsIgnoreCase("skillup")) {
            SkillUpCommand.skillUp(this, sender, args);
            return true;
        }

        //SKILLDOWN
        if(commandLabel.equalsIgnoreCase("skilldown")){
            SkillDownCommand.skillDown(this, sender, args);
            return true;
        }

        //SKILLINFO
        if(commandLabel.equalsIgnoreCase("skillinfo")){
            SkillInfoCommand.skillInfo(this, sender, args);
            return true;
        }

        //SKILLPOINTS
        if(commandLabel.equalsIgnoreCase("skillpoints")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
                return true;
            }
            Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player) sender);
            
            if(sender.hasPermission("skilltree.points")) {
                sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You currently have " + getPlayerPoints(hero) + " SkillPoints.");
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have enough permissions!");
            }
            return true;
        }

        //SKILLADMIN
        if(commandLabel.equalsIgnoreCase("skilladmin")){
            SkillAdminCommand.skillAdmin(this, sender, args);
            return true;
        }
        
        //SKILLLIST
        if (commandLabel.equalsIgnoreCase("slist") || commandLabel.equalsIgnoreCase("sl")) {
            SkillListCommand.skillList(this, sender, args);
            return true;
        }
        
        //SKILLUNLOCKABLE
        if (commandLabel.equalsIgnoreCase("unlocks") || commandLabel.equalsIgnoreCase("un")) {
            SkillLockedCommand.skillList(this, sender, args);
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "HeroesSkillTree Help Page:");
        sender.sendMessage(ChatColor.GRAY + "/skillup <skill> [amount] (level up a skill)");
        sender.sendMessage(ChatColor.GRAY + "/skilldown <skill> [amount] (de-levels a skill)");
        sender.sendMessage(ChatColor.GRAY + "/slist (lists all unlocked skills)");
        sender.sendMessage(ChatColor.GRAY + "/unlocks (lists all adjacent unlockable skills)");
        sender.sendMessage(ChatColor.GRAY + "/skillinfo <skill> (all info on a skill)");
        sender.sendMessage(ChatColor.GRAY + "/skilladmin <command> (amount) [player]");
        return true;
    }

    public FileConfiguration loadPlayer(String name) {
        //Creates new classes section
        FileConfiguration playerConfig = getPlayerConfig(name);
        if(!playerConfig.contains("classes")) {
            playerConfig.createSection("classes");
        }
        //Creates new skills section
        if(!playerConfig.contains("skills")) {
            playerConfig.createSection("skills");
        }
        try {
            playerConfig.save(new File(getDataFolder() + "/data", name + ".yml"));
        } catch (Exception e) {
            System.out.println("[HeroesSkillTree] failed to save " + name + ".yml");
        }
        return playerConfig;
    }

    public void recalcPlayer(Player player, HeroClass heroclass){
        Hero hero = heroes.getCharacterManager().getHero(player);
        //If /hero reset
        if(heroclass.isDefault()) {
            resetPlayer(player);
        }
        //If the player already has that class
        else if(!(hero.getLevel(heroclass) > 0)){
            //If the class hasn't been registered yet, register it!
            if(!getPlayerClassConfig(player).contains(heroclass.getName())){
                getPlayerClassConfig(player).createSection(heroclass.getName());
                int i = 0;
                for(Skill skill : heroes.getSkillManager().getSkills()) {
                    if(heroclass.hasSkill(skill.getName())){
                        i += getSkillLevel(hero, skill);
                    }
                }
                if(hero.getLevel(heroclass) - 1 - i < 0) {
                    getPlayerClassConfig(player).set(heroclass.getName(), 0);
                }
                else {
                    getPlayerClassConfig(player).set(heroclass.getName(), hero.getLevel(heroclass) - 1 - i);
                }
            }
        } else { //It's a new class
            FileConfiguration playerConfig = getPlayerConfig(player.getName());
            getPlayerClassConfig(player).createSection(heroclass.getName());
            getPlayerClassConfig(player).set(heroclass.getName(), 0);
            for(Skill skill : heroes.getSkillManager().getSkills()) {
                if(heroclass.hasSkill(skill.getName()) &&
                        !getPlayerSkillConfig(player).contains(skill.getName())) {
                    playerConfig.set("skills." + skill.getName(), 0);
                }
            }
            try {
                playerConfig.save(new File(getDataFolder() + "/data", hero.getPlayer().getName() + ".yml"));
            } catch (Exception e) {
                System.out.println("[HeroesSkillTree] failed to save " + hero.getPlayer().getName() + ".yml");
            }
        }
    }

    public void savePlayer(Player player){
        Hero hero = heroes.getCharacterManager().getHero(player);
        setPlayerPoints(hero, getPlayerPoints(hero));
        for(Skill skill : heroes.getSkillManager().getSkills()){
            if(hero.hasAccessToSkill(skill) &&
                    !getPlayerConfig(player.getName()).getConfigurationSection("skills").contains(skill.getName())){
                    //Creates new skills to player's section
                getPlayerSkillConfig(player).set(skill.getName(), 0);
            }
        }
        try {
            playerConfig.save(new File(getDataFolder() + "/data", hero.getPlayer().getName() + ".yml"));
        } catch (Exception e) {
            System.out.println("[HeroesSkillTree] failed to save " + hero.getPlayer().getName() + ".yml");
        }
    }

    public void resetPlayer(Player player){
        for(Skill skill : heroes.getSkillManager().getSkills()){
            if(getPlayerConfig().getConfigurationSection(player.getName() + ".skills").contains(skill.getName())){
                getPlayerConfig().getConfigurationSection(player.getName() + ".skills").set(skill.getName(), null);
            }
        }
        for(HeroClass heroclass : heroes.getClassManager().getClasses()){
            if(heroclass.isDefault()){
                getPlayerConfig().getConfigurationSection(player.getName() + ".classes").set(heroclass.getName(), 0);
                for(Skill skill : heroes.getSkillManager().getSkills()) {
                    if(heroclass.hasSkill(skill.getName())){
                        getPlayerConfig().getConfigurationSection(player.getName() + ".skills").set(skill.getName(), 0);
                    }
                }
            } else if (getPlayerConfig().getConfigurationSection(player.getName() + ".classes").contains(heroclass.getName())){
                getPlayerConfig().getConfigurationSection(player.getName() + ".classes").set(heroclass.getName(), null);
            }
        }
        savePlayerConfig();
    }

    public int getPlayerPoints(Hero hero){
        return getPlayerClassConfig(hero.getPlayer()).getInt(hero.getHeroClass().getName());
    }

    public void setPlayerPoints(Hero hero, int i){
        FileConfiguration playerConfig = getPlayerConfig(hero.getPlayer().getName());
        playerConfig.set("classes." + hero.getHeroClass().getName(), i);
        if(getPlayerPoints(hero) < 0) {
            getPlayerClassConfig(hero.getPlayer()).set(hero.getHeroClass().getName(), 0);
        }
        try {
            playerConfig.save(new File(getDataFolder() + "/data", hero.getPlayer().getName() + ".yml"));
        } catch (Exception e) {
            System.out.println("[HeroesSkillTree] failed to save " + hero.getPlayer().getName() + ".yml");
        }
    }

    public int getSkillLevel(Hero hero, Skill skill){
        return getPlayerSkillConfig(hero.getPlayer()).getInt(skill.getName());
    }

    public void setSkillLevel(Hero hero, Skill skill, int i){
        FileConfiguration playerConfig = getPlayerConfig(hero.getPlayer().getName());
        playerConfig.set("skills." + skill.getName(), i);
        try {
            playerConfig.save(new File(getDataFolder() + "/data", hero.getPlayer().getName() + ".yml"));
        } catch (Exception e) {
            System.out.println("[HeroesSkillTree] failed to save " + hero.getPlayer().getName() + ".yml");
        }
    }

    public int getSkillMaxLevel(Hero hero, Skill skill) {
        return SkillConfigManager.getSetting(hero.getHeroClass(), skill, "max-level", -1) == -1 ?
                SkillConfigManager.getUseSetting(hero, skill, "max-level", -1, false) :
                SkillConfigManager.getSetting(hero.getHeroClass(), skill, "max-level", -1);
    }

    public List<String> getStrongParentSkills(Hero hero, Skill skill){
        return getParentSkills(hero, skill, "strong");
    }

    public List<String> getWeakParentSkills(Hero hero, Skill skill){
        return getParentSkills(hero, skill, "weak");
    }
    
    public List<String> getParentSkills(Hero hero, Skill skill, String weakOrStrong) {
        FileConfiguration hCConfig = getHeroesClassConfig(hero.getHeroClass());
        if(hCConfig.getConfigurationSection("permitted-skills." + skill.getName() + ".parents") == null) {
            return null;
        }
        return hCConfig.getConfigurationSection("permitted-skills."
                + skill.getName() + ".parents").getStringList(weakOrStrong);
    }

    public boolean isLocked(Hero hero, Skill skill) {
        if(skill != null && hero.hasAccessToSkill(skill)) {
            boolean skillLevel = getSkillLevel(hero, skill) < getSkillMaxLevel(hero, skill);
            List<String> strongParents = getStrongParentSkills(hero, skill);
            boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
            List<String> weakParents = getWeakParentSkills(hero, skill);
            boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
            return skillLevel && (hasStrongParents || hasWeakParents);
        }
        return true;
    }

    public boolean isMastered(Hero hero, Skill skill){
        if(hero.hasAccessToSkill(skill)) {
            return (getSkillLevel(hero, skill) >= getSkillMaxLevel(hero, skill));
        }
        return false;
    }

    public boolean canUnlock(Hero hero, Skill skill){
        if(!hero.hasAccessToSkill(skill) || !hero.canUseSkill(skill)){
            return false;
        }
        List<String> strongParents = getStrongParentSkills(hero, skill);
        boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
        List<String> weakParents = getWeakParentSkills(hero, skill);
        boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
        if(!hasStrongParents && !hasWeakParents) {
            return true;
        }
        if(hasStrongParents){
            for(String name : getStrongParentSkills(hero, skill)){
                if(!isMastered(hero, heroes.getSkillManager().getSkill(name))) {
                    return false;
                }
            }
        }
        if(hasWeakParents){
            for(String name : getWeakParentSkills(hero, skill)) {
                if(isMastered(hero, heroes.getSkillManager().getSkill(name))) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public FileConfiguration loadPlayerConfig(String name) {
        File playerConfigFile;
        FileConfiguration playerConfig = new YamlConfiguration();
        File playerFolder = new File(getDataFolder(), "data");
        if (!playerFolder.exists()) {
            playerFolder.mkdir();
        }
        playerConfigFile = new File(playerFolder, name + ".yml");
        if (!playerConfigFile.exists()) {
            try {
                playerConfigFile.createNewFile();
            } catch (IOException ex) {
                System.out.println("[HeroesSkillTree] failed to create new " + name + ".yml");
                return null;
            }
        }
        try {
            playerConfig.load(playerConfigFile);
        } catch (Exception e) {
            System.out.println("[HeroesSkillTree] failed to load " + name + ".yml");
            return null;
        }
        return playerConfig;
    }

    public void reloadHeroesClassConfig(HeroClass HClass) {
        heroesClassConfigFile = new File(heroes.getDataFolder() + "/classes", HClass.getName() + ".yml");
        heroesClassConfig = YamlConfiguration.loadConfiguration(heroesClassConfigFile);
    }

    public FileConfiguration getHeroesClassConfig(HeroClass HClass) {
        if (heroesClassConfig == null || !heroesClassConfig.getString("name").equals(HClass.getName())) {
            this.reloadHeroesClassConfig(HClass);
        }
        return heroesClassConfig;
    }
    
    private void saveAll() {
        for (String s : playerConfigs.keySet()) {
            savePlayerConfig(s);
        }
    }
    
    public void savePlayerConfig(String s) {
        FileConfiguration playerConfig = new YamlConfiguration();
        File playerFile = new File(getDataFolder() + "/data", s + ".yml");
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException ioe) {
                String message = "[HeroesSkillTree] failed to save " + s + ".yml";
                logger.severe(message);
            }
        }
        try {
            playerConfig.save(playerFile);
        } catch (Exception e) {
            String message = "[HeroesSkillTree] failed to save " + s + ".yml";
            logger.severe(message);
        }
    }
}