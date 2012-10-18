WHAT IT IS MEANT TO DO:
This plugin intends to implement skill trees to the Heroes plugin.
All the time a player levels up, it earns one SkillPoint.
The player can then use these SkillPoints to leve up the skills they have.
Skills will be unlocked then a player gets it's parent skills to a certain level
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
Locked skills can't be used
Permissions nodes allow admins to set their best configuration.
Recognizes "hst-health/mana/reagent/stamina/damage" from skill configs for per-skill-level changes
Supports configurable amount of SkillPoints to be earned/loss each level

TODO:
                    BASIC PACKAGE:
- Create support for more nodes (cooldown, amount and more)
  
                    OTHER THINGS:
- Add configurable cost to unlock/reset/leveup/leveldown skills
- Add configurable cost to level up skills in SkillPoints
- Add /skilladmin wipe (player) command to earase player from config
- Add more /skilladmin commands to edit per-skill configs on players.yml
- Suport having certain skill levels as parent reqs
- Add Spout features (far future)............>yeah further then that>...