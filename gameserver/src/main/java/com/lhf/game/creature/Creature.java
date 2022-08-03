package com.lhf.game.creature;

import static com.lhf.game.enums.Attributes.DEX;
import static com.lhf.game.enums.Attributes.STR;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.PatternSyntaxException;

import com.lhf.game.battle.Attack;
import com.lhf.game.creature.inventory.EquipmentOwner;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.inventory.InventoryOwner;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.dice.DiceD20;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Item;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.item.interfaces.Equipable;
import com.lhf.game.item.interfaces.Takeable;
import com.lhf.game.item.interfaces.Usable;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.game.magic.interfaces.CreatureAffector;
import com.lhf.game.magic.interfaces.DamageSpell;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.EquipMessage;
import com.lhf.messages.in.UnequipMessage;
import com.lhf.messages.out.AttackDamageMessage;
import com.lhf.messages.out.EquipOutMessage;
import com.lhf.messages.out.EquipOutMessage.EquipResultType;
import com.lhf.messages.out.NotPossessedMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SpellFizzleMessage;
import com.lhf.messages.out.SpellFizzleMessage.SpellFizzleType;
import com.lhf.messages.out.StatusOutMessage;
import com.lhf.messages.out.UnequipOutMessage;
import com.lhf.server.client.ClientID;

public abstract class Creature
        implements InventoryOwner, EquipmentOwner, ClientMessenger, MessageHandler, Comparable<Creature> {

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
        public SeeOutMessage produceMessage() {
            SeeOutMessage seeOutMessage = new SeeOutMessage(this);
            return seeOutMessage;
        }

        @Override
        public String printDescription() {
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
    private CreatureFaction faction; // See shared enum
    private String creatureRace;
    private Optional<Vocation> vocation;
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
    private HashMap<EquipmentSlots, Equipable> equipmentSlots; // See enum for slots

    private boolean inBattle; // Boolean to determine if this creature is in combat
    private transient ClientMessenger controller;
    private transient MessageHandler successor;
    private Map<CommandMessage, String> cmds;

    // Default constructor
    public Creature() {
        this.cmds = this.buildCommands();
        // Instantiate creature with no name and type Monster
        this.name = NameGenerator.GenerateSuffix(NameGenerator.GenerateGiven());
        this.faction = CreatureFaction.NPC;
        this.vocation = Optional.empty();

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
        this.cmds = this.buildCommands();
        this.name = name;
        this.vocation = Optional.empty();

        this.faction = statblock.faction;
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

    private Map<CommandMessage, String> buildCommands() {
        StringJoiner sj = new StringJoiner(" ");
        Map<CommandMessage, String> cmds = new HashMap<>();
        sj.add("\"equip [item]\"").add("Equips the item from your inventory to its default slot").add("\r\n");
        sj.add("\"equip [item] to [slot]\"")
                .add("Equips the item from your inventory to the specified slot, if such exists.");
        sj.add("In the unlikely event that either the item or the slot's name contains 'to', enclose the name in quotation marks.");
        cmds.put(CommandMessage.EQUIP, sj.toString());
        sj = new StringJoiner(" ");
        sj.add("\"unequip [item]\"").add("Unequips the item (if equipped) and places it in your inventory").add("\r\n");
        sj.add("\"unequip [slot]\"")
                .add("Unequips the item that is in the specified slot (if equipped) and places it in your inventory");
        cmds.put(CommandMessage.UNEQUIP, sj.toString());
        sj = new StringJoiner(" ");
        sj.add("\"inventory\"").add("List what you have in your inventory and what you have equipped");
        cmds.put(CommandMessage.INVENTORY, sj.toString());
        sj = new StringJoiner(" ");
        sj.add("\"status\"").add("Show you how much HP you currently have, among other things.");
        cmds.put(CommandMessage.STATUS, sj.toString());
        return cmds;
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

    public boolean checkName(String name) {
        return this.getName().equalsIgnoreCase(name.trim());
    }

    public boolean CheckNameRegex(String possName, Integer minimumLength) {
        Integer min = minimumLength;
        if (min < 0) {
            min = 0;
        }
        if (this.getName().length() < min) {
            min = this.getName().length();
        }
        if (min > this.getName().length()) {
            min = this.getName().length();
        }
        if (possName.length() < min || possName.length() > this.getName().length()) {
            return false;
        }
        if (this.checkName(possName)) {
            return true;
        }
        if (possName.matches("[^ a-zA-Z_-]") || possName.contains("*")) {
            return false;
        }
        try {
            return this.getName().matches("(?i).*" + possName + ".*");
        } catch (PatternSyntaxException pse) {
            pse.printStackTrace();
            return false;
        }
    }

    public CreatureFaction getFaction() {
        return faction;
    }

    public void setFaction(CreatureFaction faction) {
        this.faction = faction;
    }

    public AttributeBlock getAttributes() {
        return this.attributeBlock;
    }

    public HashSet<EquipmentTypes> getProficiencies() {
        return proficiencies;
    }

    public HashMap<EquipmentSlots, Equipable> getEquipmentSlots() {
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
        Attack a = new Attack(this, toHit.addBonus(attributeBonus));
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
        Optional<Item> item = this.getItem(itemName);
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

    protected RollResult adjustDamageByFlavor(DamageFlavor flavor, RollResult roll) {
        switch (flavor) {
            case HEALING:
                return roll;
            default:
                return roll.flipSign();
        }
    }

    public AttackDamageMessage applyAttack(Attack attack) {
        AttackDamageMessage dmOut = new AttackDamageMessage(attack.getAttacker(), this);
        for (Map.Entry<DamageFlavor, RollResult> entry : attack) {
            DamageFlavor flavor = (DamageFlavor) entry.getKey();
            RollResult damage = (RollResult) entry.getValue();
            damage = adjustDamageByFlavor(flavor, damage);
            updateHitpoints(damage.getTotal());
            dmOut.addDamage(damage);
        }
        if (!isAlive()) {
            dmOut.announceDeath();
        }
        return dmOut;
    }

    public OutMessage applySpell(CreatureAffector spell) {
        if (spell instanceof DamageSpell) {
            DamageSpell dSpell = (DamageSpell) spell;
            AttackDamageMessage dmOut = new AttackDamageMessage((Creature) spell.getCaster(), this); // TODO: this is a
                                                                                                     // horrible hack
            for (DamageDice dd : dSpell.getDamages()) {
                RollResult damage = dd.rollDice();
                damage = adjustDamageByFlavor(dd.getFlavor(), damage);
                updateHitpoints(damage.getTotal());
                dmOut.addDamage(damage);
            }
            if (!isAlive()) {
                dmOut.announceDeath();
            }
            return dmOut;
        }

        return new SpellFizzleMessage(SpellFizzleType.OTHER, spell.getCaster(), false);
    }

    private int getHealth() {
        return stats.get(Stats.CURRENTHP);
    }

    public boolean isAlive() {
        return getHealth() > 0;
    }

    private Item getWhatInSlot(EquipmentSlots slot) {
        return this.equipmentSlots.get(slot);
    }

    public String getCreatureRace() {
        return creatureRace;
    }

    public void setCreatureRace(String creatureRace) {
        this.creatureRace = creatureRace;
    }

    public Optional<Vocation> getVocation() {
        return this.vocation;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setVocation(Vocation job) {
        if (this.vocation.isPresent()) {
            this.proficiencies.removeAll(this.vocation.get().getProficiencies());
        }
        if (job == null) {
            this.vocation = Optional.empty();
        } else {
            this.vocation = Optional.of(job);
            this.proficiencies.addAll(job.getProficiencies());
        }
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

    public void setEquipmentSlots(HashMap<EquipmentSlots, Equipable> equipmentSlots) {
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
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public String printInventory() {
        return this.inventory.getInventoryOutMessage().AddEquipment(this.equipmentSlots).toString();
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(new StatusOutMessage(this, false).toString()).append("\r\n");
        if (this.equipmentSlots.get(EquipmentSlots.HAT) != null) {
            sb.append("On their head is:").append(this.equipmentSlots.get(EquipmentSlots.HAT).getColorTaggedName());
        }
        if (this.equipmentSlots.get(EquipmentSlots.ARMOR) != null) {
            sb.append("They are wearing:").append(this.equipmentSlots.get(EquipmentSlots.ARMOR).getColorTaggedName());
        } else {
            if (this.equipmentSlots.get(EquipmentSlots.NECKLACE) != null) {
                sb.append("Around their neck is:")
                        .append(this.equipmentSlots.get(EquipmentSlots.NECKLACE).getColorTaggedName());
            }
        }
        return sb.toString();
    }

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage seeOutMessage = new SeeOutMessage(this);
        return seeOutMessage;
    }

    @Override
    public String useItem(String itemName, Object onWhat) {
        // if onWhat is not specified, use this creature
        Object useOn = onWhat;
        if (useOn == null) {
            useOn = this;
        }

        Optional<Item> maybeItem = this.getItem(itemName);
        if (maybeItem.isPresent()) {
            Item item = maybeItem.get();
            if (item instanceof Usable) {
                String result = ((Usable) item).doUseAction(useOn);
                if (!((Usable) item).hasUsesLeft()) {
                    inventory.removeItem((Takeable) item);
                }
                return result;
            }
            return item.getColorTaggedName() + " is not usable!";
        }
        return "You do not have that '" + itemName + "' to use!";
    }

    public boolean applyUse(Map<String, Integer> applications) {
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
    public OutMessage equipItem(String itemName, EquipmentSlots slot) {
        Optional<Item> maybeItem = this.inventory.getItem(itemName);
        if (maybeItem.isPresent()) {
            Item fromInventory = maybeItem.get();
            if (fromInventory instanceof Equipable) {
                Equipable equipThing = (Equipable) fromInventory;
                if (slot == null) {
                    slot = equipThing.getWhichSlots().get(0);
                }
                if (equipThing.getWhichSlots().contains(slot)) {
                    OutMessage unequipMessage = this.unequipItem(slot, "");
                    this.applyUse(equipThing.onEquippedBy(this));
                    this.inventory.removeItem(equipThing);
                    this.equipmentSlots.putIfAbsent(slot, equipThing);
                    return new EquipOutMessage(unequipMessage, equipThing);
                }
                return new EquipOutMessage(EquipResultType.BADSLOT, equipThing, itemName, slot);
            }
            return new EquipOutMessage(EquipResultType.NOTEQUIPBLE, fromInventory, itemName, slot);
        }
        return new NotPossessedMessage(Item.class.getSimpleName(), itemName);
    }

    @Override
    public OutMessage unequipItem(EquipmentSlots slot, String weapon) {
        if (slot == null) {
            // if they specified weapon and not slot
            Optional<Item> optItem = getItem(weapon);
            if (optItem.isPresent()) {
                if (equipmentSlots.containsValue(optItem.get())) {
                    Equipable equippedThing = (Equipable) optItem.get();
                    for (EquipmentSlots thingSlot : equippedThing.getWhichSlots()) {
                        if (equippedThing.equals(equipmentSlots.get(thingSlot))) {
                            this.equipmentSlots.remove(thingSlot);
                            this.applyUse(equippedThing.onUnequippedBy(this));
                            this.inventory.addItem(equippedThing);
                            return new UnequipOutMessage(thingSlot, equippedThing);
                        }
                    }
                }
                return new UnequipOutMessage(null, optItem.get());
            }

            return new NotPossessedMessage(Item.class.getSimpleName(), weapon);
        }
        Equipable thing = getEquipmentSlots().remove(slot);
        if (thing != null) {
            this.applyUse(thing.onUnequippedBy(this));
            this.inventory.addItem(thing);
            return new UnequipOutMessage(slot, thing);
        }
        return new UnequipOutMessage(slot, thing); // thing is null
    }

    @Override
    public Equipable getEquipped(EquipmentSlots slot) {
        return (Equipable) this.equipmentSlots.get(slot);
    }

    @Override
    public String getStartTag() {
        String tag = "<" + this.getClass().getSimpleName().toLowerCase() + ">";
        CreatureFaction foundFaction = this.getFaction();
        if (foundFaction != null) {
            tag = "<" + foundFaction.name().toLowerCase() + ">";
        }
        return tag;
    }

    @Override
    public String getEndTag() {
        String tag = "</" + this.getClass().getSimpleName().toLowerCase() + ">";
        CreatureFaction foundFaction = this.getFaction();
        if (foundFaction != null) {
            tag = "</" + foundFaction.name().toLowerCase() + ">";
        }
        return tag;
    }

    @Override
    public String getColorTaggedName() {
        return getStartTag() + getName() + getEndTag();
    }

    public ClientMessenger getController() {
        return this.controller;
    }

    public void setController(ClientMessenger cont) {
        this.controller = cont;
    }

    @Override
    public void sendMsg(OutMessage msg) {
        if (this.controller != null) {
            this.controller.sendMsg(msg);
            return;
        }
        // Does nothing silently
    }

    @Override
    public ClientID getClientID() {
        if (this.controller != null) {
            return this.controller.getClientID();
        }
        return null;
    }

    @Override
    public void setSuccessor(MessageHandler successor) {
        this.successor = successor;
    }

    @Override
    public MessageHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public Map<CommandMessage, String> getCommands() {
        return this.cmds;
    }

    @Override
    public Boolean handleMessage(CommandContext ctx, Command msg) {
        boolean handled = false;
        if (msg.getType() == CommandMessage.EQUIP) {
            EquipMessage eqmsg = (EquipMessage) msg;
            ctx.sendMsg(this.equipItem(eqmsg.getItemName(), eqmsg.getEquipSlot()));
            handled = true;
        } else if (msg.getType() == CommandMessage.UNEQUIP) {
            UnequipMessage uneqmsg = (UnequipMessage) msg;
            ctx.sendMsg(this.unequipItem(EquipmentSlots.getEquipmentSlot(uneqmsg.getUnequipWhat()),
                    uneqmsg.getUnequipWhat()));
            handled = true;
        } else if (msg.getType() == CommandMessage.STATUS) {
            ctx.sendMsg(new StatusOutMessage(this, true));
            handled = true;
        } else if (msg.getType() == CommandMessage.INVENTORY) {
            ctx.sendMsg(this.inventory.getInventoryOutMessage().AddEquipment(this.equipmentSlots));
            handled = true;
        }

        if (handled) {
            return handled;
        }
        ctx.setCreature(this);
        return MessageHandler.super.handleMessage(ctx, msg);
    }

    @Override
    public int compareTo(Creature other) {
        return this.getName().compareTo(other.getName());
    }

}