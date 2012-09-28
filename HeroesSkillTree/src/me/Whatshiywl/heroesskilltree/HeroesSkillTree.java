package me.Whatshiywl.heroesskilltree;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;

public class HeroesSkillTree extends JavaPlugin {
	
	public final Logger logger = Logger.getLogger("Minecraft");
	public HeroesSkillTree plugin;
    public final EventListener HEventListener = new EventListener(this);
	public Boolean hasHeroes;
	public Map<Player, Integer> Points = new HashMap<Player, Integer>();
	public Map<Skill, Integer> SkillLevel = new HashMap<Skill, Integer>();
	public FileConfiguration config;
	private FileConfiguration playerConfig = null;
	private File playerConfigFile = null;
	public Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");
	
	@Override
	public void onDisable() 
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Has Been Disabled!");
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
		
		Points.put(player, getPlayerConfig().getConfigurationSection(player.getName()).getInt("Points"));
		return getPlayerConfig().getConfigurationSection(player.getName());
	}
	
	public void savePlayer(Player player){
		player.sendMessage("savePlayer()");		
		getPlayerConfig().getConfigurationSection(player.getName()).set("Points", Points.get(player));
		savePlayerConfig();
	}
	
	public static int getSkillLevel(Hero hero, Skill skill){
		hero.getSkillSettings(skill);
		return 0;
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
}
