package com.lhf.game.creature;

import com.lhf.game.battle.Attack;
import com.lhf.game.creature.inventory.EquipmentOwner;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.inventory.InventoryOwner;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DiceRoller;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.*;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.*;
import com.lhf.game.map.objects.roomobject.Corpse;
import com.lhf.game.map.objects.sharedinterfaces.Taggable;

import java.util.*;

import static com.lhf.game.enums.Attributes.*;

public class Creature implements InventoryOwner, EquipmentOwner, Taggable {

    public class Fist extends Weapon {

        private List<EquipmentSlots> slots;
        private List<EquipmentTypes> types;
        private List<DamageDice> damages;

        Fist() {
            super("Fist", false);

            types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.MONSTERPART);
            slots = Collections.singletonList(EquipmentSlots.WEAPON);
            damages = Arrays.asList(new DamageDice(1, DieType.TWO, this.getMainFlavor()));
        }

        @Override
        public List<EquipmentTypes> getTypes() {
            return types;
        }

        @Override
        public List<EquipmentSlots> getWhichSlots() {
            return slots;
        }

        @Override
        public String printWhichSlots() {
            StringJoiner sj = new StringJoiner(", ");
            sj.setEmptyValue("no slot!");
            for (EquipmentSlots slot : slots) {
                sj.add(slot.toString());
            }
            return sj.toString();
        }

        @Override
        public String printWhichTypes() {
            StringJoiner sj = new StringJoiner(", ");
            sj.setEmptyValue("none needed!");
            for (EquipmentTypes type : types) {
                sj.add(type.toString());
            }
            return sj.toString();
        }

        @Override
        public Map<String, Integer> equip() {
            return new HashMap<>(0); // changes nothing
        }

        @Override
        public Map<String, Integer> unequip() {
            return new HashMap<>(0); // changes nothing
        }

        @Override
        public String getDescription() {
            // sb.append("And best used if you have these proficiencies:
            // ").append(printWhichTypes());
            return "This is a " + getName() + " attached to a " + Creature.this.getName() +
                    " This can be equipped to: " + printWhichSlots();
        }

        @Override
        public DamageFlavor getMainFlavor() {
            return DamageFlavor.BLUDGEONING;
        }

        @Override
        public List<DamageDice> getDamages() {
            return this.damages;
        }

        @Override
        public WeaponSubtype getSubType() {
            return WeaponSubtype.CREATUREPART;
        }
    }

    private String name; // Username for players, description name (e.g., goblin 1) for monsters/NPCs
    private CreatureType creatureType; // See shared enum
    // private MonsterType monsterType; // I dont know if we'll need this

    // attributes and modifiers must in the following order:
    // STR:0, DEX:1, CON:2 , INT:3, WIS:4, CHA:5
    private HashMap<Attributes, Integer> attributes;
    private HashMap<Attributes, Integer> modifiers;

    private HashMap<Stats, Integer> stats; // contains CurrentHp, MaxHp, Xp, proficiencyBonus, AC

    // contains subtypes and items
    // an example subtype would be MARTIAL_WEAPONS or HEAVY_ARMOR
    // and example item would be lightCrossbow
    private HashSet<EquipmentTypes> proficiencies;

    /*
     * TODO: or to add once things are done also needed in statblock
     * Abilities... initial thought was some kind of array
     */
    private Inventory inventory; // This creature's inventory
    private HashMap<EquipmentSlots, Item> equipmentSlots; // See enum for slots

    private boolean inBattle; // Boolean to determine if this creature is in combat

    // Default constructor
    public Creature() {
        // Instantiate creature with no name and type Monster
        this.name = "";
        this.creatureType = CreatureType.MONSTER;

        // Set attributes to default values
        this.attributes = new HashMap<>();
        this.attributes.put(STR, 10);
        this.attributes.put(DEX, 10);
        this.attributes.put(CON, 10);
        this.attributes.put(INT, 10);
        this.attributes.put(WIS, 10);
        this.attributes.put(CHA, 10);

        // Set modifiers to default values
        this.modifiers = new HashMap<>();
        this.modifiers.put(STR, 0);
        this.modifiers.put(DEX, 0);
        this.modifiers.put(CON, 0);
        this.modifiers.put(INT, 0);
        this.modifiers.put(WIS, 0);
        this.modifiers.put(CHA, 0);

        // Set default stats (10 HP, 2 proficiency bonus, etc.)
        this.stats = new HashMap<>();
        this.stats.put(Stats.MAXHP, 10);
        this.stats.put(Stats.CURRENTHP, 10);
        this.stats.put(Stats.AC, 10);
        this.stats.put(Stats.PROFICIENCYBONUS, 2);
        this.stats.put(Stats.XPEARNED, 0);
        this.stats.put(Stats.XPWORTH, 100);

        // Set empty inventory
        this.inventory = new Inventory();

        // Set empty equip slots
        this.equipmentSlots = new HashMap<>();

        // Set empty proficiencies
        this.proficiencies = new HashSet<>();

        // We don't start them in battle
        this.inBattle = false;
    }

    // Statblock-based constructor
    public Creature(String name, Statblock statblock) {
        this.name = name;

        this.creatureType = statblock.creatureType;
        this.attributes = statblock.attributes;
        this.modifiers = statblock.modifiers;
        this.stats = statblock.stats;
        this.proficiencies = statblock.proficiencies;
        // add abilities if we get to it
        this.inventory = statblock.inventory; // uncomment when Inventory class is ready
        this.equipmentSlots = statblock.equipmentSlots;
    }

    public void updateHitpoints(int value) {
        int current = stats.get(Stats.CURRENTHP);
        int max = stats.get(Stats.MAXHP);
        current += value;
        if (current <= 0) {
            current = 0;
            this.die();
        }
        if (current > max) {
            current = max;
        }
        stats.replace(Stats.CURRENTHP, current);
    }

    public void updateAc(int value) {
        int current = stats.get(Stats.AC);
        current += value;
        stats.replace(Stats.AC, current);
    }

    public void updateXp(int value) {
        int current = stats.get(Stats.XPEARNED);
        current += value;
        stats.replace(Stats.XPEARNED, current);
        if (this.canLevelUp(current, current - value)) {
            // this.levelUp();
        }
    }

    public void updateModifier(Attributes modifier, int value) {
        this.modifiers.put(modifier, this.modifiers.get(modifier) + value);
    }

    private void updateAttribute(Attributes attribute, int value) {
        this.attributes.put(attribute, this.attributes.get(attribute) + value);
    }

    private void updateStat(Stats stat, int value) {
        this.stats.put(stat, this.stats.get(stat) + value);
    }

    /* start getters */

    public HashMap<Stats, Integer> getStats() {
        return stats;
    }

    public String getName() {
        return name;
    }

    public CreatureType getCreatureType() {
        return creatureType;
    }

    public HashMap<Attributes, Integer> getAttributes() {
        return attributes;
    }

    public HashMap<Attributes, Integer> getModifiers() {
        return modifiers;
    }

    public HashSet<EquipmentTypes> getProficiencies() {
        return proficiencies;
    }

    public HashMap<EquipmentSlots, Item> getEquipmentSlots() {
        return equipmentSlots;
    }

    public boolean isInBattle() {
        return this.inBattle;
    }

    public Attack attack(Weapon weapon) {
        int attackRoll = DiceRoller.getInstance().d20(1);
        Attack a = new Attack(attackRoll, this.getName()).setTaggedAttacker(this.getColorTaggedName());
        a = weapon.modifyAttack(a);
        for (EquipmentTypes cet : this.getProficiencies()) {
            for (EquipmentTypes wet : weapon.getTypes()) {
                if (cet == wet) {
                    a = a.addToHitBonus(1);
                }
            }
        }
        HashMap<Attributes, Integer> retrieved = this.getAttributes();
        Integer str = retrieved.get(STR);
        Integer dex = retrieved.get(DEX);
        switch (weapon.getSubType()) {
            case CREATUREPART:
                // fallthrough
            case FINESSE:
                if (dex > str) {
                    a = a.addToHitBonus(dex);
                    a = a.addFlavorAndDamage(weapon.getMainFlavor(), dex);
                } else {
                    a = a.addToHitBonus(str);
                    a = a.addFlavorAndDamage(weapon.getMainFlavor(), str);
                }
                break;
            case MARTIAL:
                a = a.addToHitBonus(str);
                a = a.addFlavorAndDamage(weapon.getMainFlavor(), str);
                break;
            case PRECISE:
                a = a.addToHitBonus(dex);
                a = a.addFlavorAndDamage(weapon.getMainFlavor(), dex);
                break;
            default:
                a = a.addToHitBonus(str);
                a = a.addFlavorAndDamage(weapon.getMainFlavor(), str);
                break;
        }
        return a;
    }

    public Attack attack(String itemName, String target) {
        System.out.println(name + " is attempting to attack: " + target);
        Weapon toUse;
        Optional<Item> item = this.fromAllInventory(itemName);
        if (item.isPresent() && item.get() instanceof Weapon) {
            toUse = (Weapon) item.get();
        } else {
            toUse = this.getWeapon();
        }
        return this.attack(toUse);
    }

    public Weapon getWeapon() {
        Weapon weapon = (Weapon) getWhatInSlot(EquipmentSlots.WEAPON);
        return Objects.requireNonNullElseGet(weapon, Fist::new);
    }

    public String applyAttack(Attack attack) {
        // add stuff to calculate if the attack hits or not, and return false if so
        StringBuilder output = new StringBuilder();
        if (this.getStats().get(Stats.AC) > attack.getToHit()) {
            int which = DiceRoller.getInstance().d2(1);
            switch (which) {
                case 1:
                    output.append(attack.getTaggedAttacker()).append(" misses ").append(getColorTaggedName());
                    break;
                case 2:
                    output.append(getColorTaggedName()).append(" dodged the attack from ")
                            .append(attack.getTaggedAttacker());
                    break;
                case 3:
                    output.append(attack.getTaggedAttacker()).append(" whiffed their attack on ")
                            .append(getColorTaggedName());
                    break;
                default:
                    output.append("The attack by ").append(attack.getTaggedAttacker()).append(" on ")
                            .append(getColorTaggedName()).append(" does not land");
                    break;

            }
            output.append('\n');
            return output.toString();
        }
        for (Map.Entry<DamageFlavor, Integer> entry : attack) {
            DamageFlavor flavor = (DamageFlavor) entry.getKey();
            Integer damage = (Integer) entry.getValue();
            updateHitpoints(-damage);
            output.append(attack.getTaggedAttacker()).append(" has dealt ").append(damage).append(" ").append(flavor)
                    .append(" damage to ").append(getColorTaggedName()).append(".\n");
        }
        if (!isAlive()) {
            output.append(getColorTaggedName()).append(" has died.\r\n");
        }
        return output.toString();
    }

    private int getHealth() {
        return stats.get(Stats.CURRENTHP);
    }

    public boolean isAlive() {
        return getHealth() > 0;
    }

    // public void ( Ability ability, String target);

    // private int getAttribute(Attributes attribute) {
    // return this.attributes.getOrDefault(attribute, 10);
    // }

    private Item getWhatInSlot(EquipmentSlots slot) {
        return this.equipmentSlots.get(slot);
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    void setCreatureType(CreatureType creatureType) {
        this.creatureType = creatureType;
    }

    public void setAttributes(HashMap<Attributes, Integer> attributes) {
        this.attributes = attributes;
    }

    public void setModifiers(HashMap<Attributes, Integer> modifiers) {
        this.modifiers = modifiers;
    }

    public void setStats(HashMap<Stats, Integer> stats) {
        this.stats = stats;
    }

    public void setProficiencies(HashSet<EquipmentTypes> proficiencies) {
        this.proficiencies = proficiencies;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setEquipmentSlots(HashMap<EquipmentSlots, Item> equipmentSlots) {
        this.equipmentSlots = equipmentSlots;
    }

    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
    }

    private boolean canLevelUp(int current, int former) {
        // if former is below threshold and current is above or equal.. do things
        // in normal 5e this is where we would add abilities and ASI
        // probablly we would pull them into a pocket dimension and explain
        // what leveling up means for them, allowing them to ASI and
        // any other relevant choices they need to make
        return false;
    }

    public Corpse die() {
        System.out.println(name + "died");
        for (EquipmentSlots slot : EquipmentSlots.values()) {
            if (equipmentSlots.containsKey(slot)) {
                unequipItem(slot, equipmentSlots.get(slot).getName());
            }
        }
        return new Corpse(name + "'s corpse", true);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Creature)) {
            return false;
        }
        Creature c = (Creature) obj;
        return c.getName().equals(getName());
    }

    @Override
    public Optional<Takeable> dropItem(String itemName) {
        Optional<Takeable> item = this.inventory.getItem(itemName);
        if (item.isPresent()) {
            this.inventory.removeItem(item.get());
            return item;
        }

        for (EquipmentSlots slot : this.equipmentSlots.keySet()) {
            Takeable thing = (Takeable) this.equipmentSlots.get(slot);
            if (thing.getName().equalsIgnoreCase(itemName)) {
                this.equipmentSlots.remove(slot);
                return Optional.of(thing);
            }
        }
        return Optional.empty();
    }

    @Override
    public void takeItem(Takeable item) {
        this.inventory.addItem(item);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public String listInventory() {
        StringBuilder sb = new StringBuilder();
        sb.append("INVENTORY:\r\n");
        if (this.inventory.isEmpty()) {
            sb.append("Your inventory is empty.");
        } else {
            sb.append(this.inventory.toString());
        }
        sb.append("\r\n");

        for (EquipmentSlots slot : EquipmentSlots.values()) {
            Item item = this.equipmentSlots.get(slot);

            if (item == null) {
                sb.append(slot.toString()).append(": ").append("empty. ");
            } else {
                sb.append(slot.toString()).append(": ").append(item.getStartTagName()).append(item.getName())
                        .append(item.getEndTagName()).append(". ");
            }
        }

        return sb.toString();
    }

    public Optional<Item> fromAllInventory(String itemName) {
        Optional<Takeable> maybeTakeable = this.inventory.getItem(itemName);
        if (maybeTakeable.isPresent()) {
            return Optional.of((Item) maybeTakeable.get());
        }

        for (Item equipped : this.equipmentSlots.values()) {
            if (equipped.getName().equalsIgnoreCase(itemName)) {
                return Optional.of((equipped));
            }
        }

        return Optional.empty();
    }

    @Override
    public String useItem(String itemName, Object onWhat) {
        // if onWhat is not specified, use this creature
        Object useOn = onWhat;
        if (useOn == null) {
            useOn = this;
        }

        Optional<Item> maybeItem = this.fromAllInventory(itemName);
        if (maybeItem.isPresent()) {
            Item item = maybeItem.get();
            if (item instanceof Usable) {
                String result = ((Usable) item).doUseAction(useOn);
                if (item instanceof Consumable && !((Usable) item).hasUsesLeft()) {
                    inventory.removeItem((Takeable) item);
                }
                return result;
            }
            return item.getStartTagName() + item.getName() + item.getEndTagName() + " is not usable!";
        }
        return "You do not have that '" + itemName + "' to use!";
    }

    private boolean applyUse(Map<String, Integer> applications) {
        for (Map.Entry<String, Integer> p : applications.entrySet()) {
            try {
                Attributes attribute = Attributes.valueOf(p.getKey());
                this.updateAttribute(attribute, p.getValue());
            } catch (IllegalArgumentException e) {
                try {
                    Stats stat = Stats.valueOf(p.getKey());
                    this.updateStat(stat, p.getValue());
                } catch (IllegalArgumentException e2) {
                    e2.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String equipItem(String itemName, EquipmentSlots slot) {
        Optional<Takeable> maybeItem = this.inventory.getItem(itemName);
        if (maybeItem.isPresent()) {
            Takeable fromInventory = maybeItem.get();
            if (fromInventory instanceof Equipable) {
                Equipable equipThing = (Equipable) fromInventory;
                if (slot == null) {
                    slot = equipThing.getWhichSlots().get(0);
                }
                if (equipThing.getWhichSlots().contains(slot)) {
                    String unequipMessage = this.unequipItem(slot, "");
                    this.applyUse(equipThing.equip());
                    this.inventory.removeItem(equipThing);
                    this.equipmentSlots.putIfAbsent(slot, (Item) equipThing);
                    return unequipMessage + ((Item) equipThing).getStartTagName() + equipThing.getName()
                            + ((Item) equipThing).getEndTagName() + " successfully equipped!\r\n";
                }
                String notEquip = "You cannot equip the " + ((Item) equipThing).getStartTagName() + equipThing.getName()
                        + ((Item) equipThing).getEndTagName() + " to " + slot.toString() + "\n";
                return notEquip + "You can equip it to: " + equipThing.printWhichSlots();
            }
            return ((Item) fromInventory).getStartTagName() + fromInventory.getName()
                    + ((Item) fromInventory).getEndTagName() + " is not equippable!\r\n";
        }

        return "'" + itemName + "' is not in your inventory, so you cannot equip it!\r\n";
    }

    @Override
    public String unequipItem(EquipmentSlots slot, String weapon) {
        if (slot == null) {
            // if they specified weapon and not slot // TODO: improve this code
            Optional<Item> maybeItem = fromAllInventory(weapon);
            if (maybeItem.isPresent() && equipmentSlots.containsValue(maybeItem.get())) {
                Equipable thing = (Equipable) maybeItem.get();
                for (EquipmentSlots thingSlot : thing.getWhichSlots()) {
                    if (thing.equals(equipmentSlots.get(thingSlot))) {
                        this.equipmentSlots.remove(thingSlot);
                        this.applyUse(thing.unequip());
                        this.inventory.addItem(thing);
                        return "You have unequipped your " + ((Item) thing).getStartTagName() + thing.getName()
                                + ((Item) thing).getEndTagName() + "\r\n";
                    }
                }
                return "That is not currently equipped!";
            }

            return "That is not a slot.  These are your options: " + Arrays.toString(EquipmentSlots.values()) + "\r\n";
        }
        Equipable thing = (Equipable) getEquipmentSlots().remove(slot);
        if (thing != null) {
            this.applyUse(thing.unequip());
            this.inventory.addItem(thing);
            return "You have unequipped your " + ((Item) thing).getStartTagName() + thing.getName()
                    + ((Item) thing).getEndTagName() + "\r\n";
        }
        return "That slot is empty.\r\n";
    }

    @Override
    public Equipable getEqupped(EquipmentSlots slot) {
        return (Equipable) this.equipmentSlots.get(slot);
    }

    @Override
    public String getStartTagName() {
        return "<creature>";
    }

    @Override
    public String getEndTagName() {
        return "</creature>";
    }

    @Override
    public String getColorTaggedName() {
        return getStartTagName() + getName() + getEndTagName();
    }
}