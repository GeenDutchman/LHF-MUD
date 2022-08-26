package com.lhf.game.creature;

import java.util.*;
import java.util.regex.PatternSyntaxException;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.battle.Attack;
import com.lhf.game.creature.inventory.EquipmentOwner;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.inventory.InventoryOwner;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DamageDice.FlavoredRollResult;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.dice.DiceD20;
import com.lhf.game.dice.DieType;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Item;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.EquipMessage;
import com.lhf.messages.in.UnequipMessage;
import com.lhf.messages.out.CreatureAffectedMessage;
import com.lhf.messages.out.EquipOutMessage;
import com.lhf.messages.out.EquipOutMessage.EquipResultType;
import com.lhf.messages.out.NotPossessedMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.StatusOutMessage;
import com.lhf.messages.out.UnequipOutMessage;
import com.lhf.server.client.ClientID;

public abstract class Creature
        implements InventoryOwner, EquipmentOwner, ClientMessenger, MessageHandler, Comparable<Creature> {

    public class Fist extends Weapon {

        Fist() {
            super("Fist", false, Set.of(
                    new CreatureEffectSource("Punch", new EffectPersistence(TickType.INSTANT),
                            new EffectResistance(EnumSet.of(Attributes.STR, Attributes.DEX), Stats.AC),
                            "Fists punch things", false)
                            .addDamage(new DamageDice(1, DieType.TWO, DamageFlavor.BLUDGEONING))),
                    DamageFlavor.BLUDGEONING, WeaponSubtype.CREATUREPART);

            this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.MONSTERPART);
            this.slots = List.of(EquipmentSlots.WEAPON);
            this.descriptionString = "This is a " + getName() + " attached to a " + Creature.this.getName() + "\n";
        }

    }

    private String name; // Username for players, description name (e.g., goblin 1) for monsters/NPCs
    private CreatureFaction faction; // See shared enum
    private String creatureRace;
    private Vocation vocation;
    // private MonsterType monsterType; // I dont know if we'll need this

    private Set<CreatureEffect> effects;
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
        this.vocation = null;

        // Set attributes to default values
        this.attributeBlock = new AttributeBlock();
        this.effects = new TreeSet<>();
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
        this.creatureRace = statblock.getCreatureRace();
        this.vocation = null;
        this.effects = new TreeSet<>();
        this.faction = statblock.faction;
        this.attributeBlock = statblock.attributes;
        this.stats = statblock.stats;
        this.proficiencies = statblock.proficiencies;
        // add abilities if we get to it
        this.inventory = statblock.inventory; // uncomment when Inventory class is ready
        this.equipmentSlots = statblock.equipmentSlots;
        for (Item item : this.equipmentSlots.values()) {
            Equipable equipped = (Equipable) item;
            equipped.onEquippedBy(this);
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

    private int getHealth() {
        return stats.get(Stats.CURRENTHP);
    }

    public boolean isAlive() {
        return getHealth() > 0;
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

    public MultiRollResult check(Attributes attribute) {
        Dice d20 = new DiceD20(1);
        MultiRollResult result = new MultiRollResult(d20.rollDice(), this.getAttributes().getMod(attribute));
        return result;
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

    public abstract void restoreFaction();

    public CreatureFaction getFaction() {
        if (this.faction == null) {
            this.faction = CreatureFaction.RENEGADE;
        }
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
        Attack a = weapon.generateAttack(this);
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

    protected MultiRollResult adjustDamageByFlavor(MultiRollResult mrr, boolean reverse) {
        if (mrr == null) {
            return null;
        }
        ArrayList<RollResult> adjusted = new ArrayList<>();
        for (RollResult rr : mrr) {
            if (rr instanceof FlavoredRollResult) {
                FlavoredRollResult frr = (FlavoredRollResult) rr;
                switch (frr.getFlavor()) {
                    case HEALING:
                        if (reverse) {
                            adjusted.add(rr.negative());
                        } else {
                            adjusted.add(rr);
                        }
                        break;
                    default:
                        if (reverse) {
                            adjusted.add(rr);
                        } else {
                            adjusted.add(rr.negative());
                        }
                        break;
                }
            } else {
                if (reverse) {
                    adjusted.add(rr.negative());
                } else {
                    adjusted.add(rr);
                }
            }
        }
        return new MultiRollResult(adjusted, mrr.getBonuses());
    }

    public CreatureAffectedMessage applyEffect(CreatureEffect effect, boolean reverse) {
        MultiRollResult mrr = this.adjustDamageByFlavor(effect.getDamageResult(), reverse);
        if (mrr != null) {
            effect.updateDamageResult(mrr);
            this.updateHitpoints(mrr.getRoll());
        }
        for (Stats delta : effect.getStatChanges().keySet()) {
            int amount = effect.getStatChanges().get(delta);
            if (reverse) {
                amount = amount * -1;
            }
            this.updateStat(delta, amount);
        }
        if (this.isAlive()) {
            for (Attributes delta : effect.getAttributeScoreChanges().keySet()) {
                int amount = effect.getAttributeScoreChanges().get(delta);
                if (reverse) {
                    amount = amount * -1;
                }
                this.updateAttribute(delta, amount);
            }
            for (Attributes delta : effect.getAttributeBonusChanges().keySet()) {
                int amount = effect.getAttributeBonusChanges().get(delta);
                if (reverse) {
                    amount = amount * -1;
                }
                this.updateModifier(delta, amount);
            }
            if (!reverse && effect.getPersistence().getTickSize() != TickType.INSTANT) {
                this.effects.add(effect);
            } else if (reverse && this.effects.contains(effect)) {
                this.effects.remove(effect);
            }
            // for now...cannot curse someone with being a renegade
            if (effect.isRestoreFaction()) {
                this.restoreFaction();
            }
        }

        CreatureAffectedMessage camOut = new CreatureAffectedMessage(this, effect, reverse);
        return camOut;
    }

    public CreatureAffectedMessage applyEffect(CreatureEffect effect) {
        return this.applyEffect(effect, false);
    }

    public void tick(TickType type) {
        this.effects.removeIf(effect -> {
            if (effect.tick(type) == 0) {
                this.applyEffect(effect, true);
                return true;
            }
            return false;
        });
    }

    public void removeEffectByName(String name) {
        this.effects.removeIf(effect -> effect.getName().equals(name));
    }

    public boolean hasEffect(String name) {
        return this.effects.stream().anyMatch(effect -> effect.getName().equals(name));
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

    public Vocation getVocation() {
        return this.vocation;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setVocation(Vocation job) {
        if (this.vocation != null) {
            this.proficiencies.removeAll(this.vocation.getProficiencies());
        }
        if (job == null) {
            this.vocation = null;
        } else {
            this.vocation = job;
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
    public boolean equipItem(String itemName, EquipmentSlots slot) {
        Optional<Item> maybeItem = this.inventory.getItem(itemName);
        if (maybeItem.isPresent()) {
            Item fromInventory = maybeItem.get();
            if (fromInventory instanceof Equipable) {
                Equipable equipThing = (Equipable) fromInventory;
                if (slot == null) {
                    slot = equipThing.getWhichSlots().get(0);
                }
                if (equipThing.getWhichSlots().contains(slot)) {
                    this.unequipItem(slot, "");
                    this.inventory.removeItem(equipThing);
                    this.equipmentSlots.putIfAbsent(slot, equipThing);
                    this.sendMsg(new EquipOutMessage(equipThing));
                    equipThing.onEquippedBy(this);

                    return true;
                }
                this.sendMsg(new EquipOutMessage(EquipResultType.BADSLOT, equipThing, itemName, slot));
                return true;
            }
            this.sendMsg(new EquipOutMessage(EquipResultType.NOTEQUIPBLE, fromInventory, itemName, slot));
            return true;
        }
        this.sendMsg(new NotPossessedMessage(Item.class.getSimpleName(), itemName));
        return true;
    }

    @Override
    public boolean unequipItem(EquipmentSlots slot, String weapon) {
        if (slot == null) {
            // if they specified weapon and not slot
            Optional<Item> optItem = getItem(weapon);
            if (optItem.isPresent()) {
                if (equipmentSlots.containsValue(optItem.get())) {
                    Equipable equippedThing = (Equipable) optItem.get();
                    for (EquipmentSlots thingSlot : equippedThing.getWhichSlots()) {
                        if (equippedThing.equals(equipmentSlots.get(thingSlot))) {
                            this.equipmentSlots.remove(thingSlot);
                            this.inventory.addItem(equippedThing);
                            this.sendMsg(new UnequipOutMessage(thingSlot, equippedThing));
                            equippedThing.onUnequippedBy(this);
                            return true;
                        }
                    }
                }
                this.sendMsg(new UnequipOutMessage(null, optItem.get()));
                return false;
            }

            this.sendMsg(new NotPossessedMessage(Item.class.getSimpleName(), weapon));
            return false;
        }
        Equipable thing = getEquipmentSlots().remove(slot);
        if (thing != null) {
            this.inventory.addItem(thing);
            this.sendMsg(new UnequipOutMessage(slot, thing));
            thing.onUnequippedBy(this);
            return true;
        }
        this.sendMsg(new UnequipOutMessage(slot, thing)); // thing is null
        return false;
    }

    @Override
    public Equipable getEquipped(EquipmentSlots slot) {
        return (Equipable) this.equipmentSlots.get(slot);
    }

    @Override
    public Optional<Item> removeItem(String name) {
        Optional<Item> toRemove = InventoryOwner.super.removeItem(name);
        if (toRemove.isEmpty()) {
            for (Item item : this.equipmentSlots.values()) {
                if (item.CheckNameRegex(name, 3)) {
                    toRemove = Optional.of(item);
                    break;
                }
            }
        }
        return toRemove;
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
            this.equipItem(eqmsg.getItemName(), eqmsg.getEquipSlot());
            handled = true;
        } else if (msg.getType() == CommandMessage.UNEQUIP) {
            UnequipMessage uneqmsg = (UnequipMessage) msg;
            this.unequipItem(EquipmentSlots.getEquipmentSlot(uneqmsg.getUnequipWhat()),
                    uneqmsg.getUnequipWhat());
            handled = true;
        } else if (msg.getType() == CommandMessage.STATUS) {
            ctx.sendMsg(new StatusOutMessage(this, true));
            handled = true;
        } else if (msg.getType() == CommandMessage.INVENTORY) {
            ctx.sendMsg(this.inventory.getInventoryOutMessage().AddEquipment(this.equipmentSlots));
            handled = true;
        }

        if (handled) {
            this.tick(TickType.ACTION);
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