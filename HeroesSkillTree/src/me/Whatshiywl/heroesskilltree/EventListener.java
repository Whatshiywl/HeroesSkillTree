package me.Whatshiywl.heroesskilltree;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.herocraftonline.heroes.api.events.*;
import com.herocraftonline.heroes.characters.Hero;

public class EventListener implements Listener 
{
	private static HeroesSkillTree plugin;
	public EventListener(HeroesSkillTree instance) 
	{
		EventListener.plugin = instance;
	}

	@EventHandler
	public void onLevelChangeEvent(HeroChangeLevelEvent event)
	{
		Hero hero = event.getHero();
		plugin.setPlayerPoints(hero, plugin.getPlayerPoints(hero) + (event.getTo() - event.getFrom()));
		plugin.savePlayer(hero.getPlayer());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		//Hero hero = plugin.heroes.getCharacterManager().getHero(player);
		plugin.loadPlayer(player);
		//plugin.setPlayerPoints(hero, plugin.getPlayerPoints(hero));
		plugin.savePlayer(player);
		//hero.getHeroClass().getParents();
	}
	
	@EventHandler
	public void onPlayerReset(ClassChangeEvent event){
		if(event.getTo().isDefault()){
			plugin.setPlayerPoints(event.getHero(), 0);
			plugin.savePlayer(event.getHero().getPlayer());
		}
	}
}