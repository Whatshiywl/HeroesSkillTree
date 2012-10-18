package me.Whatshiywl.heroesskilltree;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import me.Whatshiywl.heroesskilltree.commands.SkillUpCommand;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.Whatshiywl.heroesskilltree.commands.SkillAdminCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillDownCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillInfoCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HeroesSkillTree extends JavaPlugin {

    public final Logger logger = Logger.getLogger("Minecraft");
    public final EventListener HEventListener = new EventListener(this);

    public HeroesSkillTree plugin;
    public static Heroes heroes = (Heroes) Bukkit.getServer().getPluginManager().getPlugin("Heroes");
    public List<Skill> SkillStrongParents = new ArrayList<Skill>();
    public List<Skill> SkillWeakParents = new ArrayList<Skill>();

    private FileConfiguration playerConfig = null;
    private FileConfiguration heroesClassConfig = null;
    private File playerConfigFile = null;
    private File heroesClassConfigFile = null;

    @Override
    public void onDisable()
    {
        savePlayerConfig();
        for(Player player : Bukkit.getServer().getOnlinePlayers()) savePlayer(player);
        PluginDescriptionFile pdfFile = this.getDescription();
        this.logger.info(pdfFile.getName() + " Has Been Disabled!");
    }

    @Override
    public void onEnable()
    {
        plugin = this;
        PluginDescriptionFile pdfFile = this.getDescription();
        this.logger.info("[" + pdfFile.getName() + "] Version " + pdfFile.getVersion() + " Has Been Enabled!");
        PluginManager pm = getServer().getPluginManager();
        getConfig().options().copyDefaults(true);
        saveConfig();
        getPlayerConfig().options().copyDefaults(true);
        savePlayerConfig();
        final Heroes heroes = (Heroes)plugin.getServer().getPluginManager().getPlugin("Heroes");
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
        }

        //SKILLADMIN
        if(commandLabel.equalsIgnoreCase("skilladmin")){
            SkillAdminCommand.skillAdmin(this, sender, args);
        }
        sender.sendMessage(ChatColor.RED + "/skilladmin <command> (amount) [player]");
        return true;
    }

    public ConfigurationSection loadPlayer(Player player) {
        Hero hero = heroes.getCharacterManager().getHero(player);
        //Creates new player section
        if(!getPlayerConfig().contains(player.getName())) {
            getPlayerConfig().createSection(player.getName());
        }
        //Creates new classes section
        if(!getPlayerConfig().getConfigurationSection(player.getName()).contains("classes")) {
            getPlayerConfig().getConfigurationSection(player.getName()).createSection("classes");
        }
        //Creates new skills section
        if(!getPlayerConfig().getConfigurationSection(player.getName()).contains("skills")) {
            getPlayerConfig().getConfigurationSection(player.getName()).createSection("skills");
        }
        //Creates new classes to player's class section
        if(!getPlayerConfig().getConfigurationSection(player.getName() + ".classes").contains(hero.getHeroClass().getName())){
            getPlayerConfig().getConfigurationSection(player.getName() + ".classes").set(hero.getHeroClass().getName(), hero.getLevel() - 1);
        }//Creates new skills to player's section
        for(Skill skill : heroes.getSkillManager().getSkills()){
            if(hero.hasAccessToSkill(skill)){
                if(!getPlayerConfig().getConfigurationSection(player.getName() + ".skills").contains(skill.getName())){
                    getPlayerConfig().getConfigurationSection(player.getName() + ".skills").set(skill.getName(), 0);
                }
            }
        }
        savePlayerConfig();
        return getPlayerConfig().getConfigurationSection(player.getName());
    }

    public ConfigurationSection getPlayerClassConfig(Player player){
        return loadPlayer(player).getConfigurationSection("classes");
    }

    public ConfigurationSection getPlayerSkillConfig(Player player){
        return loadPlayer(player).getConfigurationSection("skills");
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
        }
        //If it's a new class
        else{
            getPlayerClassConfig(player).createSection(heroclass.getName());
            getPlayerClassConfig(player).set(heroclass.getName(), 0);
            for(Skill skill : heroes.getSkillManager().getSkills()) {
                if(heroclass.hasSkill(skill.getName()) &&
                        !getPlayerSkillConfig(player).contains(skill.getName())) {
                    getPlayerConfig().getConfigurationSection(player.getName() + ".skills").set(skill.getName(), 0);
                }
            }
        }
        savePlayerConfig();
    }

    public void savePlayer(Player player){
        Hero hero = heroes.getCharacterManager().getHero(player);
        setPlayerPoints(hero, getPlayerPoints(hero));
        for(Skill skill : heroes.getSkillManager().getSkills()){
            if(hero.hasAccessToSkill(skill) &&
                    !getPlayerSkillConfig(player).contains(skill.getName())){
                    //Creates new skills to player's section
                getPlayerSkillConfig(player).set(skill.getName(), 0);
            }
        }
        savePlayerConfig();
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
        getPlayerClassConfig(hero.getPlayer()).set(hero.getHeroClass().getName(), i);
        if(getPlayerPoints(hero) < 0) {
            getPlayerClassConfig(hero.getPlayer()).set(hero.getHeroClass().getName(), 0);
        }
        savePlayerConfig();
    }

    public int getSkillLevel(Hero hero, Skill skill){
        return getPlayerSkillConfig(hero.getPlayer()).getInt(skill.getName());
    }

    public void setSkillLevel(Hero hero, Skill skill, int i){
        getPlayerSkillConfig(hero.getPlayer()).set(skill.getName(), i);
        savePlayerConfig();
    }

    public int getSkillMaxLevel(Hero hero, Skill skill){
        return SkillConfigManager.getUseSetting(hero, skill, "max-level", 0, false);
    }

    public List<String> getStrongParentSkills(Hero hero, Skill skill){
        if(getHeroesClassConfig(hero.getHeroClass()).getConfigurationSection("permitted-skills." + skill.getName() + ".parents") == null) {
            return null;
        }
        return getHeroesClassConfig(hero.getHeroClass()).getConfigurationSection("permitted-skills."
                + skill.getName() + ".parents").getStringList("strong");
    }

    public List<String> getWeakParentSkills(Hero hero, Skill skill){
        if((getHeroesClassConfig(hero.getHeroClass()).getConfigurationSection("permitted-skills." + skill.getName()).contains("parents")) &&
            (getHeroesClassConfig(hero.getHeroClass()).getConfigurationSection("permitted-skills." + skill.getName() + ".parents").contains("weak"))){
                return getHeroesClassConfig(hero.getHeroClass()).
                        getConfigurationSection("permitted-skills." + skill.getName() + ".parents").getStringList("weak");
        } else {
            return null;
        }
    }

    public boolean isLocked(Hero hero, Skill skill){
        if(hero.hasAccessToSkill(skill)) {
            return (getSkillLevel(hero, skill) <= 0);
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
        if(getStrongParentSkills(hero, skill) == null && getWeakParentSkills(hero, skill) == null) {
            return true;
        }
        if(getStrongParentSkills(hero, skill) != null){
            for(String name : getStrongParentSkills(hero, skill)){
                if(!isMastered(hero, heroes.getSkillManager().getSkill(name))) {
                    return false;
                }
            }
            if(getWeakParentSkills(hero, skill) == null) {
                return true;
            }
        }
        if(getWeakParentSkills(hero, skill) != null){
            for(String name : getWeakParentSkills(hero, skill)) {
                if(isMastered(hero, heroes.getSkillManager().getSkill(name))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void reloadPlayerConfig() {
        if (playerConfigFile == null) {
            playerConfigFile = new File(getDataFolder(), "players.yml");
        }
        playerConfig = YamlConfiguration.loadConfiguration(playerConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = this.getResource("players.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            playerConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getPlayerConfig() {
        if (playerConfig == null) {
            this.reloadPlayerConfig();
        }
        return playerConfig;
    }

    public void savePlayerConfig() {
        if (playerConfig == null || playerConfigFile == null) {
        return;
        }
        try {
            getPlayerConfig().save(playerConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + playerConfigFile, ex);
        }
    }

    public void reloadHeroesClassConfig(HeroClass HClass) {
        heroesClassConfigFile = new File(heroes.getDataFolder() + "/classes", HClass.getName() + ".yml");
        heroesClassConfig = YamlConfiguration.loadConfiguration(heroesClassConfigFile);
    }

    public FileConfiguration getHeroesClassConfig(HeroClass HClass) {
        if (heroesClassConfig == null ||
                (heroesClassConfig.getString("name") == null ? HClass.getName() != null : !heroesClassConfig.getString("name").equals(HClass.getName()))) {
            this.reloadHeroesClassConfig(HClass);
        }
        return heroesClassConfig;
    }
}
