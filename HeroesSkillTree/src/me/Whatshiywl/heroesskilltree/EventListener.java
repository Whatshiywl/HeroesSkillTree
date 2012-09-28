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
		//plugin.loadPlayer(hero.getPlayer());
		plugin.Points.put(hero.getPlayer(), plugin.Points.get(hero.getPlayer()) + (event.getTo() - event.getFrom()));
		plugin.savePlayer(hero.getPlayer());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		if (!plugin.Points.containsKey(player)) plugin.Points.put(player, 0); //Creates new Map if player doesn't have one.
		plugin.loadPlayer(player);
		plugin.savePlayer(player);
		//Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero(player);
		//hero.getHeroClass().getParents();
	}
	
	@EventHandler
	public void onPlayerReset(ClassChangeEvent event){
		if(event.getTo().isDefault()){
			plugin.Points.put(event.getHero().getPlayer(), 0);
			plugin.savePlayer(event.getHero().getPlayer());
		}
	}
}