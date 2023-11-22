package com.lhf.game.creature;

import java.util.*;
import java.util.regex.PatternSyntaxException;

import com.lhf.game.AffectableEntity;
import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.EntityEffect;
import com.lhf.game.TickType;
import com.lhf.game.battle.Attack;
import com.lhf.game.creature.inventory.EquipmentOwner;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.inventory.InventoryOwner;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.Statblock.DamageFlavorReactions;
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
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.enums.Stats;
import com.lhf.game.events.GameEventContext;
import com.lhf.game.events.GameEventHandlerNode;
import com.lhf.game.events.messages.ClientMessenger;
import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;
import com.lhf.game.events.messages.ITickMessage;
import com.lhf.game.events.messages.in.EquipMessage;
import com.lhf.game.events.messages.in.UnequipMessage;
import com.lhf.game.events.messages.out.CreatureAffectedMessage;
import com.lhf.game.events.messages.out.EquipOutMessage;
import com.lhf.game.events.messages.out.NotPossessedMessage;
import com.lhf.game.events.messages.out.OutMessage;
import com.lhf.game.events.messages.out.SeeOutMessage;
import com.lhf.game.events.messages.out.StatusOutMessage;
import com.lhf.game.events.messages.out.UnequipOutMessage;
import com.lhf.game.events.messages.out.EquipOutMessage.EquipResultType;
import com.lhf.game.events.messages.out.SeeOutMessage.SeeCategory;
import com.lhf.game.events.messages.out.UnequipOutMessage.UnequipResultType;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Item;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.server.client.ClientID;

public abstract class Creature
        implements InventoryOwner, EquipmentOwner, ClientMessenger, GameEventHandlerNode, Comparable<Creature>,
        AffectableEntity<CreatureEffect> {

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
    private Vocation vocation;
    // private MonsterType monsterType; // I dont know if we'll need this

    private TreeSet<CreatureEffect> effects;
    private Statblock statblock;

    private boolean inBattle; // Boolean to determine if this creature is in combat
    private transient ClientMessenger controller;
    private transient GameEventHandlerNode successor;
    private Map<CommandMessage, String> cmds;

    public abstract static class CreatureBuilder<T extends CreatureBuilder<T>> {
        protected T thisObject;
        private String name;
        private CreatureFaction faction;
        private Vocation vocation;
        private Statblock statblock;
        private ClientMessenger controller;
        private GameEventHandlerNode successor;

        protected CreatureBuilder() {
            this.name = NameGenerator.Generate(null);
            this.faction = CreatureFaction.NPC;
            this.vocation = null;
            this.statblock = new Statblock();
            this.controller = null;
            this.successor = null;
            this.thisObject = getThis();
        }

        // used for the generics and safe casts
        // https://stackoverflow.com/questions/17164375/subclassing-a-java-builder-class
        protected abstract T getThis();

        public T setName(String name) {
            this.name = name != null && !name.isBlank() ? name : NameGenerator.Generate(null);
            return this.getThis();
        }

        public String getName() {
            return this.name;
        }

        public T setFaction(CreatureFaction faction) {
            this.faction = faction != null ? faction : CreatureFaction.RENEGADE;
            return this.getThis();
        }

        public CreatureFaction getFaction() {
            return this.faction;
        }

        public T setVocation(Vocation vocation) {
            this.vocation = vocation;
            return this.getThis();
        }

        public Vocation getVocation() {
            return this.vocation;
        }

        public T setStatblock(Statblock statblock) {
            this.statblock = statblock;
            return this.getThis();
        }

        public Statblock getStatblock() {
            if (this.vocation != null) {
                this.statblock = this.vocation.createNewDefaultStatblock("creature");
            } else if (this.statblock == null) {
                this.statblock = new Statblock();
            }
            return this.statblock;
        }

        public T setController(ClientMessenger controller) {
            this.controller = controller;
            return this.getThis();
        }

        public ClientMessenger getController() {
            return this.controller;
        }

        public T setSuccessor(GameEventHandlerNode successor) {
            this.successor = successor;
            return this.getThis();
        }

        public GameEventHandlerNode getSuccessor() {
            return this.successor;
        }

        public abstract Creature build();

    }

    // Default constructor
    protected Creature(Creature.CreatureBuilder<?> builder) {
        this.cmds = this.buildCommands();
        // Instantiate creature with no name and type Monster
        this.name = builder.getName();
        this.faction = builder.getFaction();
        this.vocation = builder.getVocation();

        this.effects = new TreeSet<>();
        this.statblock = builder.getStatblock();
        this.controller = builder.getController();
        this.successor = builder.getSuccessor();

        // We don't start them in battle
        this.inBattle = false;
    }

    private Map<CommandMessage, String> buildCommands() {
        StringJoiner sj = new StringJoiner(" ");
        Map<CommandMessage, String> cmds = new EnumMap<>(CommandMessage.class);
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
        return this.statblock.getStats().get(Stats.CURRENTHP);
    }

    public HealthBuckets getHealthBucket() {
        return HealthBuckets.calculate(getHealth(), this.statblock.getStats().get(Stats.MAXHP));
    }

    public boolean isAlive() {
        return getHealth() > 0;
    }

    public void updateHitpoints(int value) {
        int current = this.statblock.getStats().get(Stats.CURRENTHP);
        int max = this.statblock.getStats().get(Stats.MAXHP);
        current += value;
        if (current <= 0) {
            current = 0;
            this.die();
        }
        if (current > max) {
            current = max;
        }
        this.statblock.getStats().replace(Stats.CURRENTHP, current);
    }

    public void updateAc(int value) {
        int current = this.statblock.getStats().get(Stats.AC);
        current += value;
        this.statblock.getStats().replace(Stats.AC, current);
    }

    public void updateXp(int value) {
        int current = this.statblock.getStats().get(Stats.XPEARNED);
        current += value;
        this.statblock.getStats().replace(Stats.XPEARNED, current);
        if (this.canLevelUp(current, current - value)) {
            // this.levelUp();
        }
    }

    public MultiRollResult check(Attributes attribute) {
        Dice d20 = new DiceD20(1);
        MultiRollResult result = new MultiRollResult.Builder().addRollResults(d20.rollDice())
                .addBonuses(this.getAttributes().getMod(attribute)).Build();
        return result;
    }

    public void updateModifier(Attributes modifier, int value) {
        AttributeBlock retrieved = this.statblock.getAttributes();
        retrieved.setModBonus(modifier, retrieved.getModBonus(modifier) + value);
    }

    private void updateAttribute(Attributes attribute, int value) {
        AttributeBlock retrieved = this.statblock.getAttributes();
        retrieved.setScoreBonus(attribute, retrieved.getScoreBonus(attribute) + value);
    }

    private void updateStat(Stats stat, int value) {
        Map<Stats, Integer> stats = this.statblock.getStats();
        stats.put(stat, stats.get(stat) + value);
    }

    /* start getters */

    public Map<Stats, Integer> getStats() {
        return this.statblock.getStats();
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
        return this.statblock.getAttributes();
    }

    public Attributes getHighestAttributeBonus(EnumSet<Attributes> attrs) {
        int highestMod = Integer.MIN_VALUE;
        Attributes found = null;
        if (attrs == null || attrs.size() == 0) {
            return found;
        }
        for (Attributes attr : attrs) {
            int retrieved = this.getAttributes().getMod(attr);
            if (retrieved > highestMod) {
                highestMod = retrieved;
                found = attr;
            }
        }
        return found;
    }

    public Set<EquipmentTypes> getProficiencies() {
        return this.statblock.getProficiencies();
    }

    public Map<EquipmentSlots, Equipable> getEquipmentSlots() {
        return this.statblock.getEquipmentSlots();
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
        MultiRollResult.Builder mrrBuilder = new MultiRollResult.Builder();
        DamageFlavorReactions dfr = this.statblock.getDamageFlavorReactions();
        for (RollResult rr : mrr) {
            if (rr instanceof FlavoredRollResult) {
                FlavoredRollResult frr = (FlavoredRollResult) rr;
                if (dfr.getHealing().contains(frr.getDamageFlavor())) {
                    if (reverse) {
                        mrrBuilder.addRollResults(frr.negative());
                    } else {
                        mrrBuilder.addRollResults(frr);
                    }
                } else if (dfr.getImmunities().contains(frr.getDamageFlavor())) {
                    mrrBuilder.addRollResults(frr.none());
                } else if (dfr.getResistances().contains(frr.getDamageFlavor())) {
                    if (reverse) {
                        mrrBuilder.addRollResults(frr.half());
                    } else {
                        mrrBuilder.addRollResults(frr.negative().half());
                    }
                } else if (dfr.getWeaknesses().contains(frr.getDamageFlavor())) {
                    if (reverse) {
                        mrrBuilder.addRollResults(frr.twice());
                    } else {
                        mrrBuilder.addRollResults(frr.negative().twice());
                    }
                } else {
                    if (reverse) {
                        mrrBuilder.addRollResults(frr);
                    } else {
                        mrrBuilder.addRollResults(frr.negative());
                    }
                }
            } else {
                if (dfr.getImmunities().size() > 0) {
                    mrrBuilder.addRollResults(rr.none()); // if they have any immunities, unflavored damge does nothing
                } else if (reverse) {
                    mrrBuilder.addRollResults(rr.negative());
                } else {
                    mrrBuilder.addRollResults(rr);
                }
            }
        }

        if (dfr.getImmunities().size() == 0) { // if they have any immunities, unflavored damge does nothing
            mrrBuilder.addBonuses(mrr.getBonuses());
        }

        return mrrBuilder.Build();
    }

    @Override
    public boolean isCorrectEffectType(EntityEffect effect) {
        return effect != null && effect instanceof CreatureEffect;
    }

    @Override
    public boolean shouldAdd(EntityEffect effect, boolean reverse) {
        return this.isAlive() && AffectableEntity.super.shouldAdd(effect, reverse);
    }

    @Override
    public CreatureAffectedMessage processEffect(EntityEffect effect, boolean reverse) {
        if (!this.isCorrectEffectType(effect)) {
            return null;
        }
        CreatureEffect creatureEffect = (CreatureEffect) effect;
        MultiRollResult mrr = this.adjustDamageByFlavor(creatureEffect.getDamageResult(), reverse);
        if (mrr != null) {
            creatureEffect.updateDamageResult(mrr);
            this.updateHitpoints(mrr.getRoll());
        }
        for (Stats delta : creatureEffect.getStatChanges().keySet()) {
            int amount = creatureEffect.getStatChanges().get(delta);
            if (reverse) {
                amount = amount * -1;
            }
            this.updateStat(delta, amount);
        }
        if (this.isAlive()) {
            for (Attributes delta : creatureEffect.getAttributeScoreChanges().keySet()) {
                int amount = creatureEffect.getAttributeScoreChanges().get(delta);
                if (reverse) {
                    amount = amount * -1;
                }
                this.updateAttribute(delta, amount);
            }
            for (Attributes delta : creatureEffect.getAttributeBonusChanges().keySet()) {
                int amount = creatureEffect.getAttributeBonusChanges().get(delta);
                if (reverse) {
                    amount = amount * -1;
                }
                this.updateModifier(delta, amount);
            }
            // for now...cannot curse someone with being a renegade
            if (creatureEffect.isRestoreFaction()) {
                this.restoreFaction();
            }
        }

        CreatureAffectedMessage camOut = CreatureAffectedMessage.getBuilder().setAffected(this)
                .setEffect(creatureEffect).setReversed(reverse).setBroacast().Build();
        return camOut;
    }

    @Override
    public NavigableSet<CreatureEffect> getMutableEffects() {
        return this.effects;
    }

    private Item getWhatInSlot(EquipmentSlots slot) {
        return this.statblock.getEquipmentSlots().get(slot);
    }

    public String getCreatureRace() {
        return this.statblock.getCreatureRace();
    }

    public void setCreatureRace(String creatureRace) {
        this.statblock.setCreatureRace(creatureRace);
    }

    public Vocation getVocation() {
        return this.vocation;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method sets the creature's vocation. Note that proficiencies, stats,
     * etc. will not be updated.
     * This is the penalty for switching vocations.
     * 
     * @param job the new vocation
     */
    public void setVocation(Vocation job) {
        this.vocation = job;
    }

    public void setAttributes(AttributeBlock attributes) {
        this.statblock.setAttributes(attributes);
    }

    public void setStats(EnumMap<Stats, Integer> stats) {
        this.statblock.setStats(stats);
    }

    public void setProficiencies(EnumSet<EquipmentTypes> proficiencies) {
        this.statblock.setProficiencies(proficiencies);
    }

    public void setInventory(Inventory inventory) {
        this.statblock.setInventory(inventory);
    }

    public void setEquipmentSlots(EnumMap<EquipmentSlots, Equipable> equipmentSlots) {
        this.statblock.setEquipmentSlots(equipmentSlots);
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
            if (this.getEquipmentSlots().containsKey(slot)) {
                unequipItem(slot, this.getEquipmentSlots().get(slot).getName());
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
        return this.statblock.getInventory();
    }

    @Override
    public String printInventory() {
        return this.statblock.getInventory().getInventoryOutMessage(this.getEquipmentSlots()).toString();
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder();
        String statusString = StatusOutMessage.getBuilder().setFromCreature(this, false).Build().toString();
        sb.append(statusString).append("\r\n");
        Map<EquipmentSlots, Equipable> equipped = this.statblock.getEquipmentSlots();
        if (equipped.get(EquipmentSlots.HAT) != null) {
            sb.append("On their head is:").append(equipped.get(EquipmentSlots.HAT).getColorTaggedName());
        }
        if (equipped.get(EquipmentSlots.ARMOR) != null) {
            sb.append("They are wearing:").append(equipped.get(EquipmentSlots.ARMOR).getColorTaggedName());
        } else {
            if (equipped.get(EquipmentSlots.NECKLACE) != null) {
                sb.append("Around their neck is:")
                        .append(equipped.get(EquipmentSlots.NECKLACE).getColorTaggedName());
            }
        }
        return sb.toString();
    }

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage.Builder seeOutMessage = SeeOutMessage.getBuilder().setExaminable(this);
        for (CreatureEffect effect : this.effects) {
            seeOutMessage.addSeen(SeeCategory.EFFECTS, effect);
        }
        return seeOutMessage.Build();
    }

    @Override
    public boolean equipItem(String itemName, EquipmentSlots slot) {
        Optional<Item> maybeItem = this.getInventory().getItem(itemName);
        EquipOutMessage.Builder equipMessage = EquipOutMessage.getBuilder().setAttemptedItemName(itemName)
                .setNotBroadcast().setAttemptedSlot(slot);
        if (maybeItem.isPresent()) {
            Item fromInventory = maybeItem.get();
            equipMessage.setItem(fromInventory);
            if (fromInventory instanceof Equipable) {
                Equipable equipThing = (Equipable) fromInventory;
                if (slot == null) {
                    slot = equipThing.getWhichSlots().get(0);
                    equipMessage.setAttemptedSlot(slot);
                }
                if (equipThing.getWhichSlots().contains(slot)) {
                    this.unequipItem(slot, "");
                    this.getInventory().removeItem(equipThing);
                    this.getEquipmentSlots().putIfAbsent(slot, equipThing);
                    this.sendMsg(equipMessage.setSubType(EquipResultType.SUCCESS).Build());
                    equipThing.onEquippedBy(this);

                    return true;
                }
                this.sendMsg(equipMessage.setSubType(EquipResultType.BADSLOT).Build());
                return true;
            }
            this.sendMsg(equipMessage.setSubType(EquipResultType.NOTEQUIPBLE));
            return true;
        }
        this.sendMsg(NotPossessedMessage.getBuilder().setNotBroadcast().setItemType(Item.class.getSimpleName())
                .setItemName(itemName).Build());
        return true;
    }

    @Override
    public boolean unequipItem(EquipmentSlots slot, String weapon) {
        UnequipOutMessage.Builder unequipMessage = UnequipOutMessage.getBuilder().setNotBroadcast().setSlot(slot)
                .setAttemptedName(weapon);
        if (slot == null) {
            // if they specified weapon and not slot
            Optional<Item> optItem = getItem(weapon);
            if (optItem.isPresent()) {
                unequipMessage.setItem(optItem.get());
                Map<EquipmentSlots, Equipable> equipped = this.getEquipmentSlots();
                if (equipped.containsValue(optItem.get())) {
                    Equipable equippedThing = (Equipable) optItem.get();
                    for (EquipmentSlots thingSlot : equippedThing.getWhichSlots()) {
                        if (equippedThing.equals(equipped.get(thingSlot))) {
                            equipped.remove(thingSlot);
                            this.getInventory().addItem(equippedThing);
                            this.sendMsg(unequipMessage.setSubType(UnequipResultType.SUCCESS).Build());
                            equippedThing.onUnequippedBy(this);
                            return true;
                        }
                    }
                }
                this.sendMsg(unequipMessage.setSubType(UnequipResultType.ITEM_NOT_EQUIPPED).Build());
                return false;
            }

            this.sendMsg(NotPossessedMessage.getBuilder().setNotBroadcast().setItemType(Item.class.getSimpleName())
                    .setItemName(weapon));
            return false;
        }
        Equipable thing = getEquipmentSlots().remove(slot);
        if (thing != null) {
            unequipMessage.setItem(thing).setSubType(UnequipResultType.SUCCESS);
            this.getInventory().addItem(thing);
            this.sendMsg(unequipMessage.Build());
            thing.onUnequippedBy(this);
            return true;
        }
        this.sendMsg(unequipMessage.setItem(null).setSubType(UnequipResultType.ITEM_NOT_FOUND).Build()); // thing is
                                                                                                         // null
        return false;
    }

    @Override
    public Equipable getEquipped(EquipmentSlots slot) {
        return (Equipable) this.getEquipmentSlots().get(slot);
    }

    @Override
    public Optional<Item> removeItem(String name) {
        Optional<Item> toRemove = InventoryOwner.super.removeItem(name);
        if (toRemove.isEmpty()) {
            for (Item item : this.getEquipmentSlots().values()) {
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
        if (msg != null && msg instanceof ITickMessage) {
            this.tick(((ITickMessage) msg).getTickType());
        }
        if (this.getController() != null) {
            this.getController().sendMsg(msg);
            return;
        }
        // Does nothing silently
    }

    @Override
    public ClientID getClientID() {
        if (this.getController() != null) {
            return this.getController().getClientID();
        }
        return null;
    }

    @Override
    public void setSuccessor(GameEventHandlerNode successor) {
        this.successor = successor;
    }

    @Override
    public GameEventHandlerNode getSuccessor() {
        return this.successor;
    }

    @Override
    public Map<CommandMessage, String> getCommands(GameEventContext ctx) {
        return ctx.addHelps(Collections.unmodifiableMap(this.cmds));
    }

    @Override
    public GameEventContext addSelfToContext(GameEventContext ctx) {
        if (ctx.getCreature() == null) {
            ctx.setCreature(this);
        }
        return ctx;
    }

    @Override
    public GameEventContext.Reply handleMessage(GameEventContext ctx, GameEvent msg) {
        boolean handled = false;
        CommandMessage msgType = msg.getGameEventType();
        ctx = this.addSelfToContext(ctx);
        if (msgType != null && this.getCommands(ctx).containsKey(msgType)) {
            if (msgType == CommandMessage.EQUIP) {
                EquipMessage eqmsg = (EquipMessage) msg;
                this.equipItem(eqmsg.getItemName(), eqmsg.getEquipSlot());
                handled = true;
            } else if (msgType == CommandMessage.UNEQUIP) {
                UnequipMessage uneqmsg = (UnequipMessage) msg;
                this.unequipItem(EquipmentSlots.getEquipmentSlot(uneqmsg.getUnequipWhat()),
                        uneqmsg.getUnequipWhat());
                handled = true;
            } else if (msgType == CommandMessage.STATUS) {
                ctx.sendMsg(StatusOutMessage.getBuilder().setNotBroadcast().setFromCreature(this, true).Build());
                handled = true;
            } else if (msgType == CommandMessage.INVENTORY) {
                ctx.sendMsg(this.getInventory().getInventoryOutMessage(this.getEquipmentSlots()));
                handled = true;
            }
        }

        if (handled) {
            this.tick(TickType.ACTION);
            return ctx.handled();
        }
        return GameEventHandlerNode.super.handleMessage(ctx, msg);
    }

    @Override
    public int compareTo(Creature other) {
        return this.getName().compareTo(other.getName());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Creature [name=").append(name).append(", health=").append(this.getHealthBucket())
                .append(", faction=").append(faction).append(", vocation=")
                .append(vocation).append("]");
        return builder.toString();
    }

}