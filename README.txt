WHAT IT IS MEANT TO DO:
This plugin intends to implement skill trees to the Heroes plugin.
All the time a player levels up, it earns one SkillPoint.
The player can then use these SkillPoints to leve up the skills they have.
Skills will be unlocked then a player gets a skill to a certain level
defined on the Heroes/classes/classe.yml itself.

WHAT IT CURRENTLY DOES:
As of now, the plugin has the whole SkillPoint system working.
The player earns the points when levels up and loses them when
they level down. The points get reset upon /hero reset usage.
The plugin also grabs all player's skills and create a place
for it on the players.yml file. There it will store the player's
SkillPoints and each skill level for that player.

TODO:
                    BASIC PACKAGE:
- Make the points system for skills acctualy work
- Make skills recognize their parents from Heroes/classes/class.yml
- Use parenting system to cancel skills whenever needed