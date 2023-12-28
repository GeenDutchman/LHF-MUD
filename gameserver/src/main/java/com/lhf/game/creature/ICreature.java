package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.regex.PatternSyntaxException;

import com.lhf.game.AffectableEntity;
import com.lhf.game.CreatureContainer;
import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.ItemContainer;
import com.lhf.game.TickType;
import com.lhf.game.battle.Attack;
import com.lhf.game.creature.inventory.EquipmentOwner;
import com.lhf.game.creature.inventory.InventoryOwner;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.Dice;
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
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Item;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.ITickEvent;
import com.lhf.messages.events.CreatureStatusRequestedEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.SeeCategory;
import com.lhf.server.client.Client.ClientID;
import com.lhf.server.client.CommandInvoker;

/**
 * An interface for all things Creature. This way we can create wrappers, mocks,
 * and other fun stuff.
 * 
 * Mostly acts like a {@link com.lhf.game.creature.statblock.Statblock
 * Statblock} "Owner" interface, along with providing some default
 * implementations for a few other interfaces.
 * 
 * @see com.lhf.game.creature.inventory.InventoryOwner
 * @see com.lhf.game.creature.inventory.EquipmentOwner
 * @see com.lhf.messages.GameEventProcessor
 * @see com.lhf.messages.CommandChainHandler
 * @see com.lhf.game.AffectableEntity
 * @see com.lhf.game.creature.CreatureEffect
 * 
 * @see java.lang.Comparable
 */
public interface ICreature
        extends InventoryOwner, EquipmentOwner, Comparable<ICreature>,
        AffectableEntity<CreatureEffect>, CommandInvoker {

    /**
     * A Fist is a weapon that most Creatures can be assumed to have.
     * Does some small {@link com.lhf.game.enums.DamageFlavor#BLUDGEONING
     * Bludgeoning} damage.
     */
    public static class Fist extends Weapon {

        Fist() {
            super("Fist", false, Set.of(
                    new CreatureEffectSource("Punch", new EffectPersistence(TickType.INSTANT),
                            new EffectResistance(EnumSet.of(Attributes.STR, Attributes.DEX), Stats.AC),
                            "Fists punch things", false)
                            .addDamage(new DamageDice(1, DieType.TWO, DamageFlavor.BLUDGEONING))),
                    DamageFlavor.BLUDGEONING, WeaponSubtype.CREATUREPART);

            this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.MONSTERPART);
            this.slots = List.of(EquipmentSlots.WEAPON);
            this.descriptionString = "This is a Fist attached to a Creature \n";
        }

    }

    /**
     * The default {@link com.lhf.game.item.Weapon Weapon} for a Creature, a
     * {@link com.lhf.game.creature.ICreature.Fist Fist}!
     */
    public static final Fist defaultFist = new Fist();

    /**
     * Returns some default {@link com.lhf.game.item.Weapon Weapon} for the
     * Creature.
     * <p>
     * It will first check to see what is in the
     * {@link com.lhf.game.enums.EquipmentSlots#WEAPON Weapon} slot according to
     * {@link com.lhf.game.creature.inventory.EquipmentOwner#getEquipped(EquipmentSlots)
     * EquipmentOwner.getEquipped(EquipmentSlots)}.
     * <p>
     * Defaults to {@link com.lhf.game.creature.ICreature.Fist Fist}.
     * 
     * @return {@link com.lhf.game.item.Weapon Weapon} that should not be null
     */
    public default Weapon defaultWeapon() {
        Equipable found = this.getEquipped(EquipmentSlots.WEAPON);
        if (found != null && found instanceof Weapon weapon) {
            return weapon;
        }
        return defaultFist;
    }

    public interface ControllerAssigner {
        public abstract void assign();
    }

    /**
     * Builder pattern root for Creature
     */
    public abstract static class CreatureBuilder<BuilderType extends CreatureBuilder<BuilderType, CreatureType>, CreatureType extends ICreature>
            implements Serializable {
        protected final transient BuilderType thisObject;
        private String name;
        private CreatureFaction faction;
        private Vocation vocation;
        private String statblockName;
        private Statblock statblock;
        private Corpse corpse;

        protected CreatureBuilder() {
            this.thisObject = getThis();
            this.name = null;
            this.faction = null;
            this.vocation = null;
            this.statblockName = null;
            this.statblock = null;
            this.corpse = null;
        }

        // used for the generics and safe casts
        // https://stackoverflow.com/questions/17164375/subclassing-a-java-builder-class
        protected abstract BuilderType getThis();

        public BuilderType setName(String name) {
            this.name = name;
            return this.getThis();
        }

        /**
         * Will lazily generate a name if none is already set
         * 
         * @return
         */
        public synchronized String getName() {
            if (this.name == null || name.isBlank()) {
                this.name = NameGenerator.Generate(null);
            }
            return this.name;
        }

        public BuilderType setFaction(CreatureFaction faction) {
            this.faction = faction;
            return this.getThis();
        }

        /**
         * Will lazily generate a faction (default to
         * {@link com.lhf.game.enums.CreatureFaction#RENEGADE RENEGADE}) if none is
         * already set
         * 
         * @return
         */
        public CreatureFaction getFaction() {
            if (this.faction == null) {
                this.faction = CreatureFaction.RENEGADE;
            }
            return this.faction;
        }

        public BuilderType setVocation(Vocation vocation) {
            this.vocation = vocation;
            return this.getThis();
        }

        public Vocation getVocation() {
            return this.vocation;
        }

        public BuilderType setStatblock(Statblock statblock) {
            this.statblock = statblock;
            if (this.statblock != null) {
                this.statblockName = this.statblock.getCreatureRace();
            }
            return this.getThis();
        }

        public BuilderType setStatblockName(String statblockName) {
            this.statblockName = statblockName;
            if (this.statblock != null && !this.statblock.getCreatureRace().equals(statblockName)) {
                this.statblock = null;
            }
            return this.getThis();
        }

        public String getStatblockName() {
            return statblockName;
        }

        /**
         * Will lazily generate a {@link com.lhf.game.creature.statblock.Statblock
         * Statblock} if none is provided.
         * <p>
         * If this has a vocationName set, it'll try to use the provided
         * {@link com.lhf.game.creature.statblock.StatblockManager StatblockManager}.
         * Elsewise if this has a {@link com.lhf.game.creature.vocation.Vocation
         * Vocation} set,
         * it will use the default for the Vocation.
         * Otherwise it'll be a plain statblock.
         * 
         * @return
         * @throws FileNotFoundException
         */
        public Statblock loadStatblock(StatblockManager statblockManager) throws FileNotFoundException {
            if (this.statblock == null) {
                String nextname = this.getStatblockName();
                if (nextname != null) {
                    this.setStatblock(statblockManager.statblockFromfile(nextname));
                } else if (this.vocation != null) {
                    this.setStatblock(this.vocation.createNewDefaultStatblock("creature"));
                } else {
                    this.setStatblock(new Statblock());
                }
            }

            return this.statblock;
        }

        protected Statblock getStatblock() {
            return this.statblock;
        }

        public BuilderType setCorpse(Corpse corpse) {
            this.corpse = corpse;
            return this.getThis();
        }

        public Corpse getCorpse() {
            return this.corpse;
        }

        public abstract CreatureType build(Supplier<CommandInvoker> controllerSupplier,
                CommandChainHandler successor, StatblockManager statblockManager,
                UnaryOperator<BuilderType> composedLazyLoaders) throws FileNotFoundException;

        @Override
        public int hashCode() {
            return Objects.hash(name, faction, vocation, statblockName, statblock, corpse,
                    getThis().getClass().getName());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof CreatureBuilder))
                return false;
            CreatureBuilder<?, ?> other = (CreatureBuilder<?, ?>) obj;
            return Objects.equals(name, other.name) && faction == other.faction
                    && Objects.equals(getThis().getClass().getName(), other.getThis().getClass().getName())
                    && Objects.equals(statblockName, other.statblockName)
                    && Objects.equals(vocation, other.vocation) && Objects.equals(statblock, other.statblock)
                    && Objects.equals(corpse, other.corpse);
        }

    }

    /**
     * Gets the {@link com.lhf.server.client.CommandInvoker Controller}
     * for this Creature
     * 
     * @return {@link com.lhf.server.client.CommandInvoker Controller}
     */
    public abstract CommandInvoker getController();

    /**
     * Delegates to {@link #getController()}
     * 
     * @return
     */
    @Override
    default CommandInvoker getInnerCommandInvoker() {
        return this.getController();
    }

    /**
     * Gets the {@link com.lhf.server.client.Client.ClientID ClientID} from the
     * {@link com.lhf.server.client.CommandInvoker Controller} for the creature, or
     * NULL
     * if there is no Controller.
     * 
     * @return {@link com.lhf.server.client.Client.ClientID ClientID} or NULL
     */
    public default ClientID getClientID() {
        CommandInvoker controller = this.getController();
        return controller != null ? controller.getClientID() : null;
    }

    @Override
    default Consumer<GameEvent> getAcceptHook() {
        return (event) -> {
            if (event == null) {
                return;
            }
            if (event instanceof ITickEvent tickEvent) {
                this.tick(tickEvent);
            }
            this.announce(event);
        };
    }

    /**
     * Returns a {@link com.lhf.game.enums.HealthBuckets HealthBucket} as an
     * approximation of Health
     * 
     * @return
     */
    public abstract HealthBuckets getHealthBucket();

    /**
     * Checks to see if the Creature is alive
     * 
     * @return
     */
    public abstract boolean isAlive();

    /**
     * Updates the Creature's hitpoints by value.
     * If the Creature is no longer {@link #isAlive()} then it may search for the
     * nearest {@link com.lhf.game.CreatureContainer CreatureContainer} and notify
     * them of the death.
     */
    public abstract void updateHitpoints(int value);

    /**
     * Updates the Creature's XP
     * 
     * @param value
     */
    public abstract void updateXp(int value);

    /**
     * Retrieves the {@link com.lhf.game.creature.statblock.AttributeBlock
     * AttributeBlock} for the Creature
     * 
     * @return
     */
    public abstract AttributeBlock getAttributes();

    /**
     * Sets the Attribute block for the creature
     * 
     * @deprecated
     *             Changing the attributes wholesale during run is not good.
     *             <p>
     *             Use {@link com.lhf.game.creature.ICreature.CreatureBuilder
     *             CreatureBuilder} to change the attributes of a Creature being
     *             built
     * @param attributes
     */
    @Deprecated
    public abstract void setAttributes(AttributeBlock attributes);

    /**
     * Makes a check for the Creature, using the slected
     * {@link com.lhf.game.enums.Attributes Attribute} modifier.
     * Will just do a straight roll if attribute is null
     * 
     * @param attribute
     * @return {@link com.lhf.game.dice.MultiRollResult MultiRollResult}
     */
    public default MultiRollResult check(Attributes attribute) {
        Dice d20 = new DiceD20(1);
        MultiRollResult result = new MultiRollResult.Builder().addRollResults(d20.rollDice())
                .addBonuses(this.getAttributes().getMod(attribute)).Build();
        return result;
    }

    /**
     * Updates an {@link com.lhf.game.enums.Attributes Attribute} modifier
     * 
     * @param modifier
     * @param value
     */
    public default void updateModifier(Attributes modifier, int value) {
        AttributeBlock retrieved = this.getAttributes();
        if (retrieved == null) {
            return;
        }
        retrieved.setModBonus(modifier, retrieved.getModBonus(modifier) + value);
    }

    /**
     * This should return an unmodifiable map of stats
     * 
     * @return stat map
     */
    public abstract Map<Stats, Integer> getStats();

    /**
     * Sets the stats
     * 
     * @param stats
     */
    public abstract void setStats(EnumMap<Stats, Integer> stats);

    /**
     * Returns the name of this Creature. Should *NOT* be null!
     */
    public abstract String getName();

    /**
     * Used to check if this Creature's name matches some string (case insensitive)
     * 
     * @param otherName some string to check if it matches
     * @return true if matches, false if otherName is null or doesn't match
     */
    public default boolean checkName(String otherName) {
        String thisName = this.getName();
        if (otherName == null) {
            return false;
        }
        return thisName.equalsIgnoreCase(otherName.trim());
    }

    /**
     * Used to check if this Creature's name matches possName (case insensitive)
     * 
     * @param possName      some string to check if it matches
     * @param minimumLength the minimum length possName needs to be
     * @return true if matches, false otherwise
     */
    public default boolean CheckNameRegex(String possName, Integer minimumLength) {
        Integer min = minimumLength;
        if (min == null || min < 0) {
            min = 0;
        }
        String thisName = this.getName();
        if (thisName.length() < min) {
            min = this.getName().length();
        }
        if (min > thisName.length()) {
            min = thisName.length();
        }
        if (possName == null || possName.length() < min || possName.length() > thisName.length()) {
            return false;
        }
        if (this.checkName(possName)) {
            return true;
        }
        if (possName.matches("[^ a-zA-Z_-]") || possName.contains("*")) {
            return false;
        }
        try {
            return thisName.matches("(?i).*" + possName + ".*");
        } catch (PatternSyntaxException pse) {
            pse.printStackTrace();
            return false;
        }
    }

    /**
     * Restores the Creature's faction to some default
     */
    public abstract void restoreFaction();

    /**
     * Gets the Creature's faction. If it is null, assume it is
     * {@link com.lhf.game.enums.CreatureFaction#RENEGADE RENEGADE}
     * 
     * @see com.lhf.game.enums.CreatureFaction
     * @return creature faction
     */
    public abstract CreatureFaction getFaction();

    /**
     * Set the Creature's faction to be some new faction.
     * 
     * @see com.lhf.game.enums.CreatureFaction
     * @param faction new faction
     */
    public abstract void setFaction(CreatureFaction faction);

    /**
     * Retrieves the {@link com.lhf.game.enums.Attributes Attribute} which has the
     * highest modifier
     * 
     * @param attrs the set of Attributes to check for
     * @return the first encountered Attribute with the highest modifier or null if
     *         none found
     */
    public default Attributes getHighestAttributeBonus(EnumSet<Attributes> attrs) {
        int highestMod = Integer.MIN_VALUE;
        Attributes found = null;
        AttributeBlock retrievedBlock = this.getAttributes();

        if (attrs == null || attrs.size() == 0 || retrievedBlock == null) {
            return found;
        }

        for (Attributes attr : attrs) {
            int retrieved = retrievedBlock.getMod(attr);
            if (retrieved > highestMod) {
                highestMod = retrieved;
                found = attr;
            }
        }
        return found;
    }

    /**
     * Retrieves the {@link com.lhf.game.enums.EquipmentTypes EquipmentTypes} that
     * the Creature is proficient in
     * 
     * @return
     */
    public abstract Set<EquipmentTypes> getProficiencies();

    /**
     * Sets the proficiencies of the creature
     * 
     * @param proficiences
     */
    public abstract void setProficiencies(EnumSet<EquipmentTypes> proficiences);

    /**
     * Reveals if the Creature is set to be in a battle
     * 
     * @return true or false
     */
    public abstract boolean isInBattle();

    /**
     * Used to set the Creature in a battle.
     * 
     * @param inBattle
     */
    public abstract void setInBattle(boolean inBattle);

    /**
     * Performs the calculations for an Attack based on a
     * {@link com.lhf.game.item.Weapon Weapon}
     * 
     * @param weapon the weapon to use
     * @return the {@link com.lhf.game.battle.Attack Attack}
     */
    public default Attack attack(Weapon weapon) {
        Attack a = weapon.generateAttack(this);
        return a;
    }

    /**
     * Performs the calculations for an {@link com.lhf.game.battle.Attack Attack}
     * based on the name of the {@link com.lhf.game.item.Weapon Weapon} to use and a
     * target's name.
     * 
     * @param itemName the name of an item to try and find in the
     *                 Inventory/Equipment
     * @param target   the name of the target to attack
     * @return the {@link com.lhf.game.battle.Attack Attack}
     */
    public default Attack attack(String itemName, String target) {
        this.log(Level.FINER, () -> "Attempting to attack: " + target);
        Weapon toUse;
        Optional<Item> item = this.getItem(itemName);
        if (item.isPresent() && item.get() instanceof Weapon) {
            toUse = (Weapon) item.get();
        } else {
            toUse = this.defaultWeapon();
        }
        return this.attack(toUse);
    }

    /**
     * This isn't yet a thing much
     * 
     * @return
     */
    public abstract String getCreatureRace();

    /**
     * Will set the Creature's race
     * 
     * @param creatureRace
     */
    public abstract void setCreatureRace(String creatureRace);

    /**
     * Gets the Creature's {@link com.lhf.game.creature.vocation.Vocation Vocation}.
     * Not all Creatures have a vocation, so null is a proper response
     * 
     * @return the vocation or null
     */
    public abstract Vocation getVocation();

    /**
     * This method sets the creature's vocation. Note that proficiencies, stats,
     * etc. will not be updated.
     * This is the penalty for switching vocations.
     * 
     * @param job the new vocation
     */
    public abstract void setVocation(Vocation job);

    /**
     * A static method to create a Corpse from a Creature.
     * Moves all the equipment and stuff to the Corpse.
     * 
     * @param deadCreature
     * @return {@link com.lhf.game.item.concrete.Corpse Corpse} with all the stuff
     */
    public static Corpse die(ICreature deadCreature) {
        deadCreature.log(Level.INFO, () -> "Died.  ^_^   ->   x_x ");
        for (EquipmentSlots slot : EquipmentSlots.values()) {
            if (deadCreature.getEquipmentSlots().containsKey(slot)) {
                deadCreature.unequipItem(slot, deadCreature.getEquipmentSlots().get(slot).getName());
            }
        }
        Corpse deadCorpse = new Corpse(deadCreature.getName() + "'s corpse", true);
        ItemContainer.transfer(deadCreature, deadCorpse, null);
        return deadCorpse;
    }

    /**
     * Finds the nearest CreatureContainer announces the death.
     * 
     * @param dead
     */
    static public void announceDeath(ICreature dead) {
        if (dead == null || dead.isAlive()) {
            return;
        }
        CommandChainHandler next = dead.getSuccessor();
        while (next != null) {
            if (next instanceof CreatureContainer container) {
                container.onCreatureDeath(dead); // the rest of the chain should be handled here as well
                return; // break out of here, because it is handled
            }
            next = next.getSuccessor();
        }
        // if it gets to here, welcome to undeath (not literally)
        dead.log(Level.WARNING, "died while not in a `CreatureContainer`!");
    }

    /**
     * Prints a description of the Creature
     */
    @Override
    public default String printDescription() {
        StringBuilder sb = new StringBuilder();
        String statusString = CreatureStatusRequestedEvent.getBuilder().setFromCreature(this, false).Build().toString();
        sb.append(statusString).append("\r\n");
        Map<EquipmentSlots, Equipable> equipped = this.getEquipmentSlots();
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
    public default CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx.getCreature() == null) {
            ctx.setCreature(this);
        }
        return ctx;
    }

    /**
     * Produces a {@link com.lhf.messages.events.SeeEvent SeeOutMessage}
     * describing this Creature and any {@link com.lhf.game.creature.CreatureEffect
     * Effects} upon it.
     */
    @Override
    public default SeeEvent produceMessage(SeeEvent.Builder seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeEvent.getBuilder();
        }
        seeOutMessage.setExaminable(this);
        for (CreatureEffect effect : this.getEffects()) {
            seeOutMessage.addSeen(SeeCategory.EFFECTS, effect);
        }
        return seeOutMessage.Build();
    }

    @Override
    public default String getStartTag() {
        String tag = "<" + this.getClass().getSimpleName().toLowerCase() + ">";
        CreatureFaction foundFaction = this.getFaction();
        if (foundFaction != null) {
            tag = "<" + foundFaction.name().toLowerCase() + ">";
        }
        return tag;
    }

    @Override
    public default String getEndTag() {
        String tag = "</" + this.getClass().getSimpleName().toLowerCase() + ">";
        CreatureFaction foundFaction = this.getFaction();
        if (foundFaction != null) {
            tag = "</" + foundFaction.name().toLowerCase() + ">";
        }
        return tag;
    }

    @Override
    public default String getColorTaggedName() {
        return getStartTag() + getName() + getEndTag();
    }

    @Override
    public default int compareTo(ICreature other) {
        return this.getName().compareTo(other.getName());
    }

    public interface CreatureCommandHandler extends CommandHandler {
        static final Predicate<CommandContext> defaultCreaturePredicate = CommandHandler.defaultPredicate
                .and((ctx) -> ctx.getCreature() != null && ctx.getCreature().isAlive());
    }

    @Override
    public default Collection<GameEventProcessor> getGameEventProcessors() {
        TreeSet<GameEventProcessor> messengers = new TreeSet<>(GameEventProcessor.getComparator());
        GameEventProcessor controller = this.getController();
        if (controller != null) {
            messengers.add(controller);
        }
        return Collections.unmodifiableCollection(messengers);
    }

}