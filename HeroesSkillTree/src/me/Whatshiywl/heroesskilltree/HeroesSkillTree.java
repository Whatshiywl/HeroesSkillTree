package me.Whatshiywl.heroesskilltree;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;

public class HeroesSkillTree extends JavaPlugin {
	
	public final Logger logger = Logger.getLogger("Minecraft");
	public HeroesSkillTree plugin;
    public final EventListener HEventListener = new EventListener(this);
	public Boolean hasHeroes;
	public FileConfiguration config;
	private FileConfiguration playerConfig = null;
	private File playerConfigFile = null;
	private FileConfiguration heroesClassConfig = null;
	private File heroesClassConfigFile = null;
	public Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");
	public List<Skill> SkillStrongParents = new ArrayList<Skill>();
	public List<Skill> SkillWeakParents = new ArrayList<Skill>();
	
	@Override
	public void onDisable() 
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Has Been Disabled!");
		savePlayerConfig();
	}
	
	@Override	
	public void onEnable() 
	{
		plugin = this;
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " Has Been Enabled!");
		PluginManager pm = getServer().getPluginManager();
		getConfig().options().copyDefaults(true);
		saveConfig();
		getPlayerConfig().options().copyDefaults(true);
		savePlayerConfig();
		final Heroes heroes = (Heroes)plugin.getServer().getPluginManager().getPlugin("Heroes");
		if(heroes.isEnabled())
		{
			pm.registerEvents(this.HEventListener, this);
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){

		final Heroes heroes = (Heroes)plugin.getServer().getPluginManager().getPlugin("Heroes");
		if(sender instanceof Player){
			Player player = (Player) sender;
			Hero hero = heroes.getCharacterManager().getHero(player);
			if(commandLabel.equalsIgnoreCase("skillup"))
			{
				if(args.length > 0){
					Skill skill = heroes.getSkillManager().getSkill(args[0]);
					if(hero.hasAccessToSkill(args[0])){
						int i;
						if(args.length > 1) i = Integer.parseInt(args[1]);
						else i = 1;
						if(getPlayerPoints(hero) >= i){
							if(getSkillMaxLevel(hero, skill) >= getSkillLevel(hero, skill) + i){
								if(isLocked(hero, skill)){
									if(canUnlock(hero, skill)){
										setPlayerPoints(hero, getPlayerPoints(hero) - i);
										setSkillLevel(hero, skill, getSkillLevel(hero, skill) + i);
										savePlayerConfig();
									}
									else player.sendMessage("You can't unlock this skill!");
								}
								else{
									setPlayerPoints(hero, getPlayerPoints(hero) - i);
									setSkillLevel(hero, skill, getSkillLevel(hero, skill) + i);
									savePlayerConfig();
								}
							}
							else player.sendMessage("This skill is already mastered");
						}
						else player.sendMessage("You don't have enough SkillPoints");
					}
					else if(heroes.getSkillManager().getSkills().contains(skill)) player.sendMessage("You don't have this skill");
					else player.sendMessage("This skill doesn't exist");
				}
				else{
					player.sendMessage("No skill given");
				}
			}
			else if(commandLabel.equalsIgnoreCase("skilldown"))
			{
				if(args.length > 0){
					Skill skill = heroes.getSkillManager().getSkill(args[0]);
					if(hero.hasAccessToSkill(args[0])){
						int i;
						if(args.length > 1) i = Integer.parseInt(args[1]);
						else i = 1;
						if(getSkillLevel(hero, skill) >= i){
							//if(getSkillLevel(hero, skill) - i >= 1){ //Won't allow players to re-lock skills 
								setPlayerPoints(hero, getPlayerPoints(hero) + i);
								setSkillLevel(hero, skill, getSkillLevel(hero, skill) - i);
								savePlayerConfig();
							//}
							//else player.sendMessage("This skill is already locked");
						}
						else player.sendMessage("This skill is not a high enough level");
					}
					else if(heroes.getSkillManager().getSkills().contains(skill)) player.sendMessage("You don't have this skill");
					else player.sendMessage("This skill doesn't exist");
				}
				else{
					player.sendMessage("No skill given");
				}
			}
			else if(commandLabel.equalsIgnoreCase("parents")){
				if(args.length > 0){
					player.sendMessage(getStrongParentSkills(hero, heroes.getSkillManager().getSkill(args[0])).toString());
				}
			}
		}
		return false;
	}

	public ConfigurationSection loadPlayer(Player player){
		Hero hero = heroes.getCharacterManager().getHero(player);
		if(!getPlayerConfig().contains(player.getName())){
			//Creates new player section
			getPlayerConfig().createSection(player.getName());
			savePlayerConfig();
		}

		if(!getPlayerConfig().getConfigurationSection(player.getName()).contains("Points")){
			//Creates point recorder for player
			getPlayerConfig().getConfigurationSection(player.getName()).createSection("Points");
			savePlayerConfig();
		}
		
		for(Skill skill : heroes.getSkillManager().getSkills()){
			if(hero.hasAccessToSkill(skill)){
				if(!getPlayerConfig().getConfigurationSection(player.getName()).contains(skill.getName())){
					//Creates new skills to player's section
					getPlayerConfig().getConfigurationSection(player.getName()).set(skill.getName(), 0);
					savePlayerConfig();
				}
			}
		}
		return getPlayerConfig().getConfigurationSection(player.getName());
	}
	
	public void savePlayer(Player player){
		Hero hero = heroes.getCharacterManager().getHero(player);
		setPlayerPoints(hero, getPlayerPoints(hero));
		for(Skill skill : heroes.getSkillManager().getSkills()){
			if(hero.hasAccessToSkill(skill)){
				
			}
		}
		savePlayerConfig();
	}
	
	public int getPlayerPoints(Hero hero){
		return getPlayerConfig().getConfigurationSection(hero.getPlayer().getName()).getInt("Points");
	}
	
	public void setPlayerPoints(Hero hero, int i){
		getPlayerConfig().getConfigurationSection(hero.getPlayer().getName()).set("Points", i);
	}
	
	public int getSkillLevel(Hero hero, Skill skill){
		return getPlayerConfig().getConfigurationSection(hero.getPlayer().getName()).getInt(skill.getName());
	}
	
	public void setSkillLevel(Hero hero, Skill skill, int i){
		getPlayerConfig().getConfigurationSection(hero.getPlayer().getName()).set(skill.getName(), i);
	}
	
	public int getSkillMaxLevel(Hero hero, Skill skill){
		return (int) SkillConfigManager.getUseSetting(hero, skill, "max-level", 1, false);
	}
	
	public List<String> getStrongParentSkills(Hero hero, Skill skill){
		if((getHeroesClassConfig(hero.getHeroClass()).getConfigurationSection("permitted-skills." + skill.getName()).contains("parents")) &&
			(getHeroesClassConfig(hero.getHeroClass()).getConfigurationSection("permitted-skills." + skill.getName() + ".parents").contains("strong"))){
				return getHeroesClassConfig(hero.getHeroClass()).
						getConfigurationSection("permitted-skills." + skill.getName() + ".parents").getStringList("strong");
		}
		else return null;
	}
	
	public List<String> getWeakParentSkills(Hero hero, Skill skill){
		if((getHeroesClassConfig(hero.getHeroClass()).getConfigurationSection("permitted-skills." + skill.getName()).contains("parents")) &&
			(getHeroesClassConfig(hero.getHeroClass()).getConfigurationSection("permitted-skills." + skill.getName() + ".parents").contains("weak"))){
				return getHeroesClassConfig(hero.getHeroClass()).
						getConfigurationSection("permitted-skills." + skill.getName() + ".parents").getStringList("weak");
		}
		else return null;
	}
	
	public boolean isLocked(Hero hero, Skill skill){ if(hero.hasAccessToSkill(skill)) return (getSkillLevel(hero, skill) <= 0); return true;}
	
	public boolean isMastered(Hero hero, Skill skill){ if(hero.hasAccessToSkill(skill)) return (getSkillLevel(hero, skill) >= getSkillMaxLevel(hero, skill)); return false;}
	
	public boolean canUnlock(Hero hero, Skill skill){
		if(hero.hasAccessToSkill(skill) && (hero.canUseSkill(skill))){
			if(getStrongParentSkills(hero, skill) != null) 
				for(String name : getStrongParentSkills(hero, skill)) if(!isMastered(hero, heroes.getSkillManager().getSkill(name))) return false;
			if(getWeakParentSkills(hero, skill) != null) 
				for(String name : getWeakParentSkills(hero, skill)) if(isMastered(hero, heroes.getSkillManager().getSkill(name))) return true;
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
	    if (heroesClassConfigFile == null) {
	    	heroesClassConfigFile = new File(heroes.getDataFolder() + "/classes", HClass.getName() + ".yml");
	    }
	    heroesClassConfig = YamlConfiguration.loadConfiguration(heroesClassConfigFile);
	}
	
	public FileConfiguration getHeroesClassConfig(HeroClass HClass) {
	    if (heroesClassConfig == null) {
	        this.reloadHeroesClassConfig(HClass);
	    }
	    return heroesClassConfig;
	}
}
