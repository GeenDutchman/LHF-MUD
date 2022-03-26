package com.lhf.game.creature;

import com.lhf.game.battle.Attack;
import com.lhf.game.creature.inventory.EquipmentOwner;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.inventory.InventoryOwner;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD20;
import com.lhf.game.dice.DieType;
import com.lhf.game.dice.Dice.RollResult;
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
        private Map<String, Integer> equippingChanges;

        Fist() {
            super("Fist", false);

            types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.MONSTERPART);
            slots = Collections.singletonList(EquipmentSlots.WEAPON);
            damages = Arrays.asList(new DamageDice(1, DieType.TWO, this.getMainFlavor()));
            equippingChanges = new HashMap<>(0); // changes nothing
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
        public Map<String, Integer> getEquippingChanges() {
            return this.equippingChanges;
        }

        @Override
        public String getDescription() {
            // sb.append("And best used if you have these proficiencies:
            // ").append(printWhichTypes());
            return "This is a " + getName() + " attached to a " + Creature.this.getName() + "\n" +
                    this.printStats();
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
    private String creatureRace;
    // private MonsterType monsterType; // I dont know if we'll need this

    // uses attributes STR, DEX, CON, INT, WIS, CHA
    private AttributeBlock attributeBlock;

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
        this.attributeBlock = new AttributeBlock();

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
        this.attributeBlock = statblock.attributes;
        this.stats = statblock.stats;
        this.proficiencies = statblock.proficiencies;
        // add abilities if we get to it
        this.inventory = statblock.inventory; // uncomment when Inventory class is ready
        this.equipmentSlots = statblock.equipmentSlots;
        for (Item item : this.equipmentSlots.values()) {
            Equipable equipped = (Equipable) item;
            this.applyUse(equipped.onEquippedBy(this));
        }
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

    public RollResult check(Attributes attribute) {
        Dice d20 = new DiceD20(1);
        return d20.rollDice().addBonus(this.getAttributes().getMod(attribute));
    }

    public void updateModifier(Attributes modifier, int value) {
        this.attributeBlock.setModBonus(modifier, this.attributeBlock.getModBonus(modifier) + value);
    }

    private void updateAttribute(Attributes attribute, int value) {
        this.attributeBlock.setScoreBonus(attribute, this.attributeBlock.getScoreBonus(attribute) + value);
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

    public AttributeBlock getAttributes() {
        return this.attributeBlock;
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
        RollResult toHit;
        int attributeBonus = 0;
        AttributeBlock retrieved = this.getAttributes();
        Integer str = retrieved.getMod(STR);
        Integer dex = retrieved.getMod(DEX);
        switch (weapon.getSubType()) {
            case CREATUREPART:
                // fallthrough
            case FINESSE:
                if (dex > str) {
                    attributeBonus = dex;
                    toHit = this.check(DEX);
                } else {
                    attributeBonus = str;
                    toHit = this.check(STR);
                }
                break;
            case PRECISE:
                attributeBonus = dex;
                toHit = this.check(DEX);
                break;
            case MARTIAL:
                // fallthrough
            default:
                attributeBonus = str;
                toHit = this.check(STR);
                break;
        }
        Attack a = new Attack(toHit.addBonus(attributeBonus), this.getName())
                .setTaggedAttacker(this.getColorTaggedName());
        a = weapon.modifyAttack(a);
        a = a.addDamageBonus(weapon.getMainFlavor(), attributeBonus);
        // for (EquipmentTypes cet : this.getProficiencies()) {
        // for (EquipmentTypes wet : weapon.getTypes()) {
        // if (cet == wet) {
        // a = a.addToHitBonus(1);
        // }
        // }
        // }

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

    protected int adjustDamageByFlavor(DamageFlavor flavor, int value) {
        switch (flavor) {
            case HEALING:
                return value;
            default:
                return -1 * value;
        }
    }

    public String applyAttack(Attack attack) {
        // add stuff to calculate if the attack hits or not, and return false if so
        StringBuilder output = new StringBuilder();
        for (Map.Entry<DamageFlavor, RollResult> entry : attack) {
            DamageFlavor flavor = (DamageFlavor) entry.getKey();
            RollResult damage = (RollResult) entry.getValue();
            int adjustedDamage = adjustDamageByFlavor(flavor, damage.getTotal());
            updateHitpoints(adjustedDamage);
            output.append(attack.getTaggedAttacker()).append(" has dealt ").append(damage.getColorTaggedName())
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

    public String getCreatureRace() {
        return creatureRace;
    }

    public void setCreatureRace(String creatureRace) {
        this.creatureRace = creatureRace;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    void setCreatureType(CreatureType creatureType) {
        this.creatureType = creatureType;
    }

    public void setAttributes(AttributeBlock attributes) {
        this.attributeBlock = attributes;
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
                sb.append(slot.toString()).append(": ").append(item.getColorTaggedName()).append(". ");
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
            return item.getColorTaggedName() + " is not usable!";
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
                    this.applyUse(equipThing.onEquippedBy(this));
                    this.inventory.removeItem(equipThing);
                    this.equipmentSlots.putIfAbsent(slot, (Item) equipThing);
                    return unequipMessage + ((Item) equipThing).getColorTaggedName() + " successfully equipped!\r\n";
                }
                String notEquip = "You cannot equip the " + ((Item) equipThing).getColorTaggedName() + " to "
                        + slot.toString() + "\n";
                return notEquip + "You can equip it to: " + equipThing.printWhichSlots();
            }
            return ((Item) fromInventory).getColorTaggedName() + " is not equippable!\r\n";
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
                        this.applyUse(thing.onUnequippedBy(this));
                        this.inventory.addItem(thing);
                        return "You have unequipped your " + ((Item) thing).getColorTaggedName() + "\r\n";
                    }
                }
                return "That is not currently equipped!";
            }

            return "That is not a slot.  These are your options: " + Arrays.toString(EquipmentSlots.values()) + "\r\n";
        }
        Equipable thing = (Equipable) getEquipmentSlots().remove(slot);
        if (thing != null) {
            this.applyUse(thing.onUnequippedBy(this));
            this.inventory.addItem(thing);
            return "You have unequipped your " + ((Item) thing).getColorTaggedName() + "\r\n";
        }
        return "That slot is empty.\r\n";
    }

    @Override
    public Equipable getEquipped(EquipmentSlots slot) {
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