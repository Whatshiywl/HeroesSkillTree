package me.Whatshiywl.heroesskilltree;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.herocraftonline.heroes.api.events.*;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;

public class EventListener implements Listener 
{
	private static HeroesSkillTree plugin;
	public EventListener(HeroesSkillTree instance) 
	{
		EventListener.plugin = instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		Hero hero = plugin.heroes.getCharacterManager().getHero(player);
		plugin.loadPlayer(player);
		plugin.savePlayer(player);
		for(Skill skill : plugin.heroes.getSkillManager().getSkills()){
			if(plugin.isLocked(hero, skill)) if(hero.hasEffect(skill.getName())){
				//hero.getPlayer().sendMessage("Removing Effect");
				hero.removeEffect(hero.getEffect(skill.getName()));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerReset(ClassChangeEvent event){
		Hero hero = event.getHero();
		if(event.getTo().isDefault()){
			plugin.setPlayerPoints(hero, 0);
			plugin.savePlayer(hero.getPlayer());
		}
		for(Skill skill : plugin.heroes.getSkillManager().getSkills()){
			if(plugin.isLocked(hero, skill)) if(hero.hasEffect(skill.getName())){
				//hero.getPlayer().sendMessage("Removing Effect");
				hero.removeEffect(hero.getEffect(skill.getName()));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLevelChangeEvent(HeroChangeLevelEvent event)
	{
		Hero hero = event.getHero();
		plugin.setPlayerPoints(hero, plugin.getPlayerPoints(hero) + (event.getTo() - event.getFrom()));
		plugin.savePlayer(hero.getPlayer());
		for(Skill skill : plugin.heroes.getSkillManager().getSkills()){
			if(plugin.isLocked(hero, skill)) if(hero.hasEffect(skill.getName())){
				//hero.getPlayer().sendMessage("Removing Effect");
				hero.removeEffect(hero.getEffect(skill.getName()));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClassChangeEvent(ClassChangeEvent event)
	{
		Hero hero = event.getHero();
		plugin.savePlayer(hero.getPlayer());
		for(Skill skill : plugin.heroes.getSkillManager().getSkills()){
			if(plugin.isLocked(hero, skill)) if(hero.hasEffect(skill.getName())){
				//hero.getPlayer().sendMessage("Removing Effect");
				hero.removeEffect(hero.getEffect(skill.getName()));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerUseSkill(SkillUseEvent event){
		if(plugin.isLocked(event.getHero(), event.getSkill()) && !event.getPlayer().hasPermission("skilltree.override.locked")){
			event.getPlayer().sendMessage(ChatColor.RED + "This skill is still locked!");
			event.getHero().hasEffect(event.getSkill().getName());
			event.setCancelled(true);
		}
	}
}