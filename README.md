# LHF-MUD
School Project


See our class wiki <https://github.com/cs428TAs/f2019/wiki/L.H.F.-M.U.D.>  page.

# The Team:
* Drake Foltz (Project Manager) - I come up with PBIs, keep track of tasks and generating reports, as well as hop in on the coding.
* Daniel Najarro (Front-end Specialist) - Create client interface and make sure client can communicate with the server. 
* Tristan Henderson (Director of Quality Assurance) Oversee official testing, log and track bugs and issues. Develop forms of automated testing.
* Jonathan King (Architect) - Determine design choices, technical standards, and technical goals for the project. Ensure code reviews are happening, and that what's being produced matches our design. Make final technical decisions if disputes arise. Code where needed to help the team produce a quality product.
* Spencer Hastings (Lead Gameplay Designer) - Design and implement gameplay logic for the game server. Work alongside dungeon master to turn story ideas into implementation.
* Pearce Keesling (Back End infastructor) - Build the infrastructure for the backend so that the client can connect and communicate in order to interact with the gameplay logic. I'm also handling the setup and deployment of the server.
* Kendall Despain (Lore/Dungeon Master) - World building, inputting creatures and objects in the database and general story line or lack there of :p

**titles subject to change

# Mermaids

```mermaid
classDiagram
  class Examinable
  class Taggable
  class Container
  class Item
  class Takeable
  class Usable
  class Equipable
  class Stackable
  class Interactable
  class Weapon

  Container o.. Item
  Examinable <-- Item
  Taggable <-- Item

  Item <-- Takeable
%%   Item <-- Usable
%%   Item <-- Equipable
%%   Item <-- Stackable
  Item <-- Interactable
%%   Item <-- Weapon

  Takeable <-- Usable

%%   Takeable <-- Equipable
  Usable <-- Equipable

%%   Takeable <-- Stackable
  Usable <-- Stackable

%%   Takeable <-- Weapon
%%   Usable <-- Weapon
  Equipable <-- Weapon

  class Room
  Container <-- Room
%%   Room o-- Interactable

Examinable <-- Container
Taggable <-- Container

class Creature
class Player
class Monster

Examinable <-- Creature
Taggable <-- Creature
Room o-- Creature

Creature <-- Player
Creature <-- Monster

Container <-- Inventory
Inventory <-- Creature


```

```mermaid
classDiagram
    class Spell
    class CreatureAffector
    class RoomAffector
    class DungeonAffector
    class Buff
    class Debuff
    class Damage
    class Healing
    class Summon
    class Banish

    Spell <-- CreatureAffector
    Spell <-- RoomAffector
    Spell <-- DungeonAffector
    %% Spell <-- Buff
    %% Spell <-- Debuff
    %% Spell <-- Damage
    %% Spell <-- Healing
    %% Spell <-- Summon
    %% Spell <-- Banish

    CreatureAffector <-- Buff
    CreatureAffector <-- Debuff
    CreatureAffector <-- Damage
    CreatureAffector <-- Healing
    CreatureAffector <-- Summon
    CreatureAffector <-- Banish
    RoomAffector <-- Summon
    RoomAffector <-- Banish
    DungeonAffector <-- Summon
    DungeonAffector <-- Banish

```

spells can buff
spells can debuff
spells can damage
spells can heal
spells can summon
spells can banish


spells can require caster ability check
spells can have caster spell DC
spells can require target ability save
spells can require target AC

For casting a spell:
have ability to cast spells
have energy to cast spells
need to invoke spell
-> make caster check
-> provide caster DC
-> target save
-> target too tough
apply effect to target

third power:
|onCast(cubeholder, castCommand) -> Spell:
||spell = SpellBuilder.invoke(castCommand)
||castLevel = spell.getCastingLevel()
||if not cubeholder.hasCastingPower(castLevel):
|||flub = this.createFlub(castLevel)
|||spell.addEffect(flub)
|||spell.addTarget(cubeholder)
|||return spell
||for target in castCommand:
|||spell.addTarget(target)
||for target in this.getRoomOccupants():
|||spell.addTarget(target)
||return spell


onCast():
	if creature is caster:
		if invokeSpell():
			if have energy for level:
				if spell needs check:
					provide check
				if spell needs DC:
					provide DC
				return spell
		return flub
	return watdoje?

onSpell():
	if spell has target:
		if DC provided and target save(dc):
			return noaffect
		if check provided and target too tough(check):
			return noaffect
	apply effects to target