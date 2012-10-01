WHAT IT IS MEANT TO DO:
This plugin intends to implement skill trees to the Heroes plugin.
All the time a player levels up, it earns one SkillPoint.
The player can then use these SkillPoints to leve up the skills they have.
Skills will be unlocked then a player gets a skill to a certain level
defined on the Heroes/classes/classe.yml itself.

WHAT IT CURRENTLY DOES:
As of now, the plugin has the whole SkillPoint system working.
The player earns the points when levels up and loses them when
they level down. 
The points get reset upon /hero reset usage.
The plugin also grabs all player's skills and create a place
for it on the players.yml file. 
It also stores skill levels for on individual player.
/skillup (skill) [amount] and /skilldown (skill) [amount] to
change skill levels.
SkillPoints are properly used.
Skills "master" at "max-level" defined on Heroes/classes/class.yml
just like classes do.
Parenting system works.
Locked skills can't be used (doesn't work with PassiveSkill due to lack of related API)
Permissions nodes now allow admins to set their best configuration.

TODO:
                    BASIC PACKAGE:
- Use parenting system to cancel skills whenever needed (only PassiveSkills left to be done)
- Create recognizable config nodes and handle their usage 
  (eg. mana, reagent-cost, stamina, cooldown, health, amount, etc)
- Improve the way skills are saved/loaded in/from players.yml
  
                    OTHER THINGS:
- Add configurable cost to unlock/reset/leveup/leveldown skills
- Add configurable amount if SkillPoints to be given every level
- Add /skilladmin wipe (player) command to earase player from config