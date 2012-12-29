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
import java.util.logging.Level;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HeroesSkillTree extends JavaPlugin {

    //version = 1.5.1-b
    public final int VERSION = 1;
    public final double SUBVERSION = 5.1;

    public static final Logger logger = Logger.getLogger("Minecraft");
    public final EventListener HEventListener = new EventListener(this);

    public HeroesSkillTree plugin;
    public static Heroes heroes = (Heroes) Bukkit.getServer().getPluginManager().getPlugin("Heroes");
    public List<Skill> SkillStrongParents = new ArrayList<>();
    public List<Skill> SkillWeakParents = new ArrayList<>();
    private HashMap<String,HashMap<String,HashMap<String, Integer>>> playerSkills = new HashMap<>();
    private int pointsPerLevel = 1;
    private HashMap<String, FileConfiguration> hConfigs = new HashMap<>();

    @Override
    public void onDisable() {
        saveAll();
        logger.info("[HeroesSkillTree] Has Been Disabled!");
    }

    @Override
    public void onEnable() {
        String message = "[HeroesSkillTree] Version " + VERSION + "." + SUBVERSION + "-Beta Has Been Enabled!";
        logger.info(message);
        PluginManager pm = getServer().getPluginManager();
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadConfig();
        pm.registerEvents(HEventListener, this);
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            loadPlayerConfig(p.getName());
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

    public void resetPlayer(Player player){
        String name = player.getName();
        playerSkills.put(name, new HashMap<String, HashMap<String, Integer>>());
        resetPlayerConfig(name);
    }

    private void resetPlayerConfig(String name) {
        File playerFolder = new File(getDataFolder(), "data");
        if (!playerFolder.exists()) {
            playerFolder.mkdir();
        }
        File playerFile = new File(playerFolder, name + ".yml");
        if (playerFile.exists() && !playerFile.delete()) {
            logger.log(Level.SEVERE, "[HeroesSkillTree] failed to delete " + name + ".yml");
            return;
        }
        try {
            playerFile.createNewFile();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "[HeroesSkillTree] failed to create new " + name + ".yml");
        }
    }

    /**
     * Calculates the unspent points by looking at all of the spent points
     * and comparing them to the hero's current level.
     * @param hero hero to calculate
     * @return points left to spend
     */
    public int getPlayerPoints(Hero hero) {
        String playername = hero.getPlayer().getName();
        String className = hero.getHeroClass().getName();
        if (!playerSkills.containsKey(playername)) {
            return 0;
        }
        if (playerSkills.get(playername).get(className) == null) {
            return 0;
        }
        
        int total = 0;
        for (String skillName : playerSkills.get(playername).get(className).keySet()) {
            Integer points = playerSkills.get(playername).get(className).get(skillName);
            if (points != null && points > 0) {
                total += points;
            } 
        }
        return hero.getLevel() - total;
    }

    public int getSkillLevel(Hero hero, Skill skill){
        //return getPlayerSkillConfig(hero.getPlayer()).getInt(skill.getName());
        if (playerSkills.get(hero.getPlayer().getName()) == null ||
                playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()) == null ||
                playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()).get(skill.getName()) == null) {
            return 0;
        }
        return playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()).get(skill.getName());
    }

    public void setSkillLevel(Hero hero, Skill skill, int i) {
        if (playerSkills.get(hero.getPlayer().getName()) == null) {
            playerSkills.put(hero.getPlayer().getName(), new HashMap<String, HashMap<String, Integer>>());
        }
        if (playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()) == null) {
            playerSkills.get(hero.getPlayer().getName()).put(hero.getHeroClass().getName(), new HashMap<String, Integer>());
        }
        playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()).put(skill.getName(), i);
    }

    public int getSkillMaxLevel(Hero hero, Skill skill) {
        return SkillConfigManager.getSetting(hero.getHeroClass(), skill, "master-level", -1) == -1 ?
                SkillConfigManager.getUseSetting(hero, skill, "master-level", -1, false) :
                SkillConfigManager.getSetting(hero.getHeroClass(), skill, "master-level", -1);
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
        if(skill != null && hero.canUseSkill(skill)) {
            boolean skillLevel = getSkillLevel(hero, skill) < 1;
            List<String> strongParents = getStrongParentSkills(hero, skill);
            boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
            List<String> weakParents = getWeakParentSkills(hero, skill);
            boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
            //String message = skill.getName() + ": " + skillLevel + "," + hasStrongParents + ":" + hasWeakParents;
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

    public void loadPlayerConfig(String name) {
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
                logger.severe("[HeroesSkillTree] failed to create new " + name + ".yml");
                return;
            }
        }
        try {
            playerConfig.load(playerConfigFile);
            for (String s : playerConfig.getKeys(false)) {
                if (!playerSkills.containsKey(s)) {
                    playerSkills.put(name, new HashMap<String, HashMap<String, Integer>>());
                }
                if (!playerSkills.get(name).containsKey(s)) {
                    playerSkills.get(name).put(s, new HashMap<String, Integer>());
                }
                if (playerConfig.getConfigurationSection(s + ".skills") == null) {
                    continue;
                }
                for (String st : playerConfig.getConfigurationSection(s + ".skills").getKeys(false)) {
                    playerSkills.get(name).get(s).put(st, playerConfig.getInt(s + ".skills." + st, 0));
                }
            }
        } catch (Exception e) {
            logger.severe("[HeroesSkillTree] failed to load " + name + ".yml");
        }
    }

    public FileConfiguration getHeroesClassConfig(HeroClass hClass) {
        if (hConfigs.containsKey(hClass.getName())) {
            return hConfigs.get(hClass.getName());
        }
        File classFolder = new File(heroes.getDataFolder(), "classes");
        for (File f : classFolder.listFiles()) {
            FileConfiguration config = new YamlConfiguration();
            try {
                config.load(f);
                String currentClassName = config.getString("name");
                if (currentClassName.equalsIgnoreCase(hClass.getName())) {
                    hConfigs.put(hClass.getName(), config);
                    return config;
                } else if (!hConfigs.containsKey(currentClassName)) {
                    hConfigs.put(currentClassName, config);
                }
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    private void saveAll() {
        for (String s : playerSkills.keySet()) {
            savePlayerConfig(s);
        }
    }

    public void savePlayerConfig(String s) {
        FileConfiguration playerConfig = new YamlConfiguration();
        File playerDataFolder = new File(getDataFolder(), "data");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdir();
        }

        File playerFile = new File(getDataFolder() + "/data", s + ".yml");
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException ioe) {
                String message = "[HeroesSkillTree] failed to save " + s + ".yml";
                logger.severe(message);
                return;
            }
        }
        try {
            playerConfig.load(playerFile);
            for (String className : playerSkills.get(s).keySet()) {
                playerConfig.set(className + ".points", playerSkills.get(s).get(className));
                if (!playerSkills.containsKey(s)) {
                    continue;
                }
                if (!playerSkills.get(s).containsKey(className)) {
                    continue;
                }
                for (String skillName : playerSkills.get(s).get(className).keySet()) {
                    playerConfig.set(className + ".skills." + skillName, playerSkills.get(s).get(className).get(skillName));
                }
            }

            playerConfig.save(playerFile);
        } catch (Exception e) {
            String message = "[HeroesSkillTree] failed to save " + s + ".yml";
            logger.severe(message);
        }
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException ioe) {
                logger.severe("[HeroesSkillTree] failed to create new config.yml");
                return;
            }
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
            pointsPerLevel = config.getInt("points-per-level", 1);
        } catch (Exception e) {
            logger.severe("[HeroesSkillTree] failed to load config.yml");
        }
    }

    public int getPointsPerLevel() {
        return pointsPerLevel;
    }
}