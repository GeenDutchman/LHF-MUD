package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.commandHandlers.FollowHandler;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.DamgeFlavorReaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Takeable;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.game.magic.concrete.DMBlessing;
import com.lhf.messages.events.CreatureAffectedEvent;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

/**
 * An interface for all things NPC. This way we can create fun things like
 * wrappers, mocks, and the like.
 * Adds functionality to the {@link com.lhf.game.creature.ICreature Creature}
 * interface.
 * 
 * @see {@link com.lhf.game.creature.ICreature ICreature}
 */
public interface INonPlayerCharacter extends ICreature {
    public static final AIRunner defaultAIRunner = new GroupAIRunner(true);
    public static final FollowHandler followHandler = new FollowHandler();

    /**
     * A BlessedFist is a {@link com.lhf.game.item.Weapon Weapon} used by those NPCs
     * who maintain their {@link com.lhf.game.magic.concrete.DMBlessing Blessing}
     * from the {@link com.lhf.game.creature.DungeonMaster DungeonMaster}.
     * <p>
     * It does the full rainbow of damages, including
     * {@link com.lhf.game.enums.DamageFlavor#HEALING HEALING} just in case, as well
     * as bypassing any resistance like {@link com.lhf.game.enums.Stats#AC Armor
     * Class}.
     */
    public static class BlessedFist extends Weapon {
        private final static CreatureEffectSource source = new CreatureEffectSource("Blessed Punch",
                new EffectPersistence(TickType.INSTANT),
                new EffectResistance(EnumSet.allOf(Attributes.class), Stats.AC), "A blessed fist punches harder.",
                new Deltas());
        private final static String description = "This is a Fist attached to a Creature who is blessed\n";

        BlessedFist() {
            super("Blessed Fist", BlessedFist.description, Set.of(BlessedFist.source), DamageFlavor.MAGICAL_BLUDGEONING,
                    WeaponSubtype.CREATUREPART);
            if (BlessedFist.source.onApplication.getDamages().size() == 0) {
                for (DamageFlavor df : DamageFlavor.values()) {
                    BlessedFist.source.onApplication.addDamage(new DamageDice(1, DieType.FOUR, df));
                }
            }

            this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.MONSTERPART);
            this.slots = List.of(EquipmentSlots.WEAPON);

        }

        @Override
        public BlessedFist makeCopy() {
            return this;
        }
    }

    /**
     * A static instance of the
     * {@link com.lhf.game.creature.INonPlayerCharacter.BlessedFist BlessedFist}
     * available to NPCs who maintain their
     * {@link com.lhf.game.magic.concrete.DMBlessing Blessing}
     * from the {@link com.lhf.game.creature.DungeonMaster DungeonMaster}.
     */
    public static final BlessedFist blessedFist = new BlessedFist();

    /**
     * HarmMemories are meant to be used in a battle to remember things like:
     * "Who hit me last?" and "How much did that hurt?". Then the NPC can use that
     * information to decide what to do next in a fight.
     */
    public static class HarmMemories {
        /**
         * Must have an owner!
         * 
         * @notnull
         */
        private final INonPlayerCharacter owner;
        private Optional<String> lastAttackerName = Optional.empty();
        private int lastDamageAmount = 0;
        private Optional<String> lastMassAttackerName = Optional.empty();
        private int lastMassDamageAmount = 0;

        /**
         * Creates HarmMemories for an NPC. Raises a NullPointerException if the
         * provided `owner` is null.
         * Is protected so that only NPCs can make them.
         * 
         * @param owner {@link com.lhf.game.creature.INonPlayerCharacter NPC} that must
         *              not be null
         * @throws NullPointerException if owner is null
         */
        protected static HarmMemories makeMemories(INonPlayerCharacter owner) {
            if (owner == null) {
                throw new NullPointerException("Cannot make HarmMemories for a null owner!");
            }
            return new HarmMemories(owner);
        }

        /**
         * Private constructor used by {@link #makeMemories(INonPlayerCharacter)}
         * 
         * @param owner {@link com.lhf.game.creature.INonPlayerCharacter NPC} that must
         *              not be null
         * @see {@link #makeMemories(INonPlayerCharacter)}
         */
        private HarmMemories(@NotNull INonPlayerCharacter owner) {
            this.owner = owner;
        }

        /**
         * Gets the name of the owner of these memories
         * 
         * @return owner name
         */
        public String getOwnerName() {
            return this.owner.getName();
        }

        /**
         * Returns the name of the last attacker, if set
         */
        public Optional<String> getLastAttackerName() {
            return lastAttackerName;
        }

        /**
         * Returns the amount of the last damage dealt to the owner
         */
        public int getLastDamageAmount() {
            return lastDamageAmount;
        }

        /**
         * Returns the name of the last attacker who did the most damage, if set
         */
        public Optional<String> getLastMassAttackerName() {
            return lastMassAttackerName;
        }

        /**
         * Returns the greatest amount of damage that has been dealt to the owner
         */
        public int getLastMassDamageAmount() {
            return lastMassDamageAmount;
        }

        /**
         * Resets all the memories to an empty or default state.
         * Returns `this` so it is chainable.
         */
        public HarmMemories reset() {
            this.lastAttackerName = Optional.empty();
            this.lastMassAttackerName = Optional.empty();
            this.lastDamageAmount = 0;
            this.lastMassDamageAmount = 0;
            return this;
        }

        /**
         * Uses the data stored in the
         * {@link com.lhf.messages.events.CreatureAffectedEvent CreatureAffectedMessage}
         * to update the statistics in the memories
         * 
         * @param cam {@link com.lhf.messages.events.CreatureAffectedEvent
         *            CreatureAffectedMessage} contains updates
         * @return `this` for chainability
         * @see {@link com.lhf.messages.events.CreatureAffectedEvent
         *      CreatureAffectedMessage}
         */
        public HarmMemories update(CreatureAffectedEvent cam) {
            if (cam == null || !this.owner.equals(cam.getAffected())) {
                return this;
            }
            Deltas ce = cam.getHighlightedDelta();
            if (ce == null) {
                return this;
            }
            MultiRollResult damage = cam.getDamages();
            if (damage == null) {
                return this;
            }
            if (ce.isOffensive()) {
                this.lastAttackerName = Optional.of(cam.getCreatureResponsible().getName());
                this.lastDamageAmount = damage.getTotal();
            }
            if (damage.getTotal() >= this.lastMassDamageAmount) {
                this.lastMassDamageAmount = damage.getTotal();
                this.lastMassAttackerName = Optional.of(cam.getCreatureResponsible().getName());
            }
            return this;
        }

    }

    public interface INonPlayerCharacterBuildInfo extends ICreatureBuildInfo {
        public enum SummonData {
            /**
             * When the summoner dies, the summon dies
             */
            LIFELINE_SUMMON,
            /**
             * While the summoner is alive, summon maintains the same faction as the
             * summoner
             */
            SYMPATHETIC_SUMMON,
            /**
             * If the summon is survives the summoner, maintains the same faction as the
             * summoner
             */
            LOYAL_SUMMON
        };

        public String getConversationFileName();

        public ConversationTree loadConversationTree(ConversationManager conversationManager)
                throws FileNotFoundException;

        public ConversationTree getConversationTree();

        public List<AIHandler> getAIHandlers();

        public default AIHandler[] getAiHandlersAsArray() {
            List<AIHandler> handlers = this.getAIHandlers();
            if (handlers == null) {
                return new AIHandler[0];
            }
            return handlers.toArray(new AIHandler[handlers.size()]);
        }

        public EnumSet<SummonData> getSummonState();

        public String getLeaderName();
    }

    /**
     * Builder pattern root for all NPCs.
     */
    public static final class INPCBuildInfo implements INonPlayerCharacterBuildInfo {

        private final String className;
        private final CreatureBuildInfo creatureBuilder;
        protected final CreatureBuilderID id;
        private String conversationFileName = null;
        private ConversationTree conversationTree = null;
        private List<AIHandler> aiHandlers;
        private EnumSet<SummonData> summonState;
        private String leaderName;

        protected INPCBuildInfo() {
            this.className = this.getClass().getName();
            this.creatureBuilder = new CreatureBuildInfo().setFaction(CreatureFaction.NPC);
            this.id = new CreatureBuilderID();
            this.aiHandlers = new ArrayList<>();
            this.summonState = EnumSet.noneOf(SummonData.class);
            this.leaderName = null;
        }

        public INPCBuildInfo(INPCBuildInfo other) {
            this.className = other.getClassName();
            this.creatureBuilder = new CreatureBuildInfo(other.creatureBuilder);
            this.id = new CreatureBuilderID();
            this.conversationFileName = other.getConversationFileName() != null
                    ? new String(other.getConversationFileName())
                    : null;
            this.conversationTree = null;
            ConversationTree otherTree = other.getConversationTree();
            if (otherTree != null) {
                this.conversationTree = otherTree.makeCopy();
            }
            this.aiHandlers = new ArrayList<>(other.getAIHandlers());
            this.summonState = EnumSet.copyOf(other.getSummonState());
        }

        @Override
        public String getClassName() {
            return this.className;
        }

        @Override
        public CreatureBuilderID getCreatureBuilderID() {
            return this.id;
        }

        public String getConversationFileName() {
            if (this.conversationTree != null) {
                this.conversationFileName = this.conversationTree.getTreeName();
            }
            return conversationFileName;
        }

        public INPCBuildInfo setConversationFileName(String conversationFileName) {
            this.conversationFileName = conversationFileName;
            if (this.conversationTree != null && !this.conversationTree.getTreeName().equals(conversationFileName)) {
                this.conversationTree = null;
            }
            return this;
        }

        public INPCBuildInfo setConversationTree(ConversationTree tree) {
            this.conversationTree = tree;
            if (tree != null) {
                this.conversationFileName = tree.getTreeName();
            }
            return this;
        }

        public ConversationTree loadConversationTree(ConversationManager conversationManager)
                throws FileNotFoundException {
            String filename = this.getConversationFileName();
            if (this.conversationTree == null && filename != null) {
                if (conversationManager == null) {
                    throw new IllegalArgumentException("Cannot create conversation Tree without converation manager");
                }
                this.setConversationTree(conversationManager.convoTreeFromFile(filename));
            }
            return this.conversationTree;
        }

        public ConversationTree getConversationTree() {
            return conversationTree;
        }

        public INPCBuildInfo useDefaultConversation() {
            this.setConversationFileName(INonPlayerCharacter.defaultConvoTreeName);
            return this;
        }

        public INPCBuildInfo addAIHandler(AIHandler handler) {
            if (handler != null) {
                this.aiHandlers.add(handler);
            }
            return this;
        }

        public List<AIHandler> getAIHandlers() {
            return this.aiHandlers;
        }

        public final AIHandler[] getAiHandlersAsArray() {
            return this.aiHandlers.toArray(new AIHandler[this.aiHandlers.size()]);
        }

        public INPCBuildInfo clearAIHandlers() {
            this.aiHandlers.clear();
            return this;
        }

        public EnumSet<SummonData> getSummonState() {
            return summonState;
        }

        public INPCBuildInfo resetSummonState() {
            this.summonState.clear();
            return this;
        }

        public INPCBuildInfo addSummonState(SummonData data) {
            if (data != null) {
                this.summonState.add(data);
            }
            return this;
        }

        public INPCBuildInfo setSummonStates(Set<SummonData> summonData) {
            if (summonData != null) {
                this.summonState = EnumSet.copyOf(summonData);
            }
            return this;
        }

        public String getLeaderName() {
            return leaderName;
        }

        public INPCBuildInfo setLeaderName(String leaderName) {
            this.leaderName = leaderName;
            return this;
        }

        public INPCBuildInfo setCreatureRace(String race) {
            creatureBuilder.setCreatureRace(race);
            return this;
        }

        public String getCreatureRace() {
            return creatureBuilder.getCreatureRace();
        }

        public INPCBuildInfo defaultStats() {
            creatureBuilder.defaultStats();
            return this;
        }

        public INPCBuildInfo setAttributeBlock(AttributeBlock block) {
            creatureBuilder.setAttributeBlock(block);
            return this;
        }

        public INPCBuildInfo setAttributeBlock(Integer strength, Integer dexterity, Integer constitution,
                Integer intelligence,
                Integer wisdom, Integer charisma) {
            creatureBuilder.setAttributeBlock(strength, dexterity, constitution, intelligence, wisdom, charisma);
            return this;
        }

        public INPCBuildInfo resetFlavorReactions() {
            creatureBuilder.resetFlavorReactions();
            return this;
        }

        public INPCBuildInfo addFlavorReaction(DamgeFlavorReaction sort, DamageFlavor flavor) {
            creatureBuilder.addFlavorReaction(sort, flavor);
            return this;
        }

        public AttributeBlock getAttributeBlock() {
            return creatureBuilder.getAttributeBlock();
        }

        public INPCBuildInfo setStats(Map<Stats, Integer> newStats) {
            creatureBuilder.setStats(newStats);
            return this;
        }

        public INPCBuildInfo setStat(Stats stat, int value) {
            creatureBuilder.setStat(stat, value);
            return this;
        }

        public EnumMap<Stats, Integer> getStats() {
            return creatureBuilder.getStats();
        }

        public INPCBuildInfo setProficiencies(EnumSet<EquipmentTypes> types) {
            creatureBuilder.setProficiencies(types);
            return this;
        }

        public INPCBuildInfo addProficiency(EquipmentTypes type) {
            creatureBuilder.addProficiency(type);
            return this;
        }

        public EnumSet<EquipmentTypes> getProficiencies() {
            return creatureBuilder.getProficiencies();
        }

        public INPCBuildInfo setInventory(Inventory other) {
            creatureBuilder.setInventory(other);
            return this;
        }

        public INPCBuildInfo addItem(Takeable item) {
            creatureBuilder.addItem(item);
            return this;
        }

        public Inventory getInventory() {
            return creatureBuilder.getInventory();
        }

        public INPCBuildInfo addEquipment(EquipmentSlots slot, Equipable equipable) {
            creatureBuilder.addEquipment(slot, equipable);
            return this;
        }

        public INPCBuildInfo setEquipmentSlots(Map<EquipmentSlots, Equipable> slots) {
            creatureBuilder.setEquipmentSlots(slots);
            return this;
        }

        public EnumMap<EquipmentSlots, Equipable> getEquipmentSlots() {
            return creatureBuilder.getEquipmentSlots();
        }

        public INPCBuildInfo setCreatureEffects(Set<CreatureEffect> others) {
            creatureBuilder.setCreatureEffects(others);
            return this;
        }

        public Set<CreatureEffect> getCreatureEffects() {
            return creatureBuilder.getCreatureEffects();
        }

        public INPCBuildInfo defaultFlavorReactions() {
            creatureBuilder.defaultFlavorReactions();
            return this;
        }

        public INPCBuildInfo setDamageFlavorReactions(EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> other) {
            creatureBuilder.setDamageFlavorReactions(other);
            return this;
        }

        public EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> getDamageFlavorReactions() {
            return creatureBuilder.getDamageFlavorReactions();
        }

        public INPCBuildInfo setName(String name) {
            creatureBuilder.setName(name);
            return this;
        }

        public String getName() {
            return creatureBuilder.getName();
        }

        public INPCBuildInfo setFaction(CreatureFaction faction) {
            creatureBuilder.setFaction(faction);
            return this;
        }

        public CreatureFaction getFaction() {
            return creatureBuilder.getFaction();
        }

        public INPCBuildInfo setVocation(Vocation vocation) {
            creatureBuilder.setVocation(vocation);
            return this;
        }

        public INPCBuildInfo setVocation(VocationName vocationName) {
            creatureBuilder.setVocation(vocationName);
            return this;
        }

        public INPCBuildInfo setVocationLevel(int level) {
            creatureBuilder.setVocationLevel(level);
            return this;
        }

        public VocationName getVocation() {
            return creatureBuilder.getVocation();
        }

        public Integer getVocationLevel() {
            return creatureBuilder.getVocationLevel();
        }

        public INPCBuildInfo setCorpse(Corpse corpse) {
            creatureBuilder.setCorpse(corpse);
            return this;
        }

        @Override
        public void acceptBuildInfoVisitor(ICreatureBuildInfoVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof INPCBuildInfo))
                return false;
            INPCBuildInfo other = (INPCBuildInfo) obj;
            return Objects.equals(id, other.id);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(this.id.toString()).append(this.getClass().getSimpleName())
                    .append(" ");
            if (this.conversationFileName != null) {
                sb.append("With conversation like: ").append(this.conversationFileName);
                if (this.conversationTree != null) {
                    sb.append(" (concrete conversation tree present)");
                }
                sb.append(".\r\n");
            }
            if (this.aiHandlers != null && !this.aiHandlers.isEmpty()) {
                StringJoiner sj = new StringJoiner(", ", "With handlers for ", ".\r\n");
                for (final AIHandler handler : this.aiHandlers) {
                    sj.add(handler.getOutMessageType().toString());
                }
                sb.append(sj.toString());
            }
            if (this.summonState != null && !this.summonState.isEmpty()) {
                sb.append("With the following summon characteristics: ").append(this.summonState.toString())
                        .append("\r\n");
            }
            sb.append("And the following internal characteristics: ").append(this.creatureBuilder);
            return sb.toString();
        }

    }

    /**
     * Returns some default {@link com.lhf.game.item.Weapon Weapon} for the
     * Creature.
     * <p>
     * It will first check to see if the
     * {@link com.lhf.game.magic.concrete.DMBlessing Blessing}
     * is maintained. If that is the case, it will return a {@link #blessedFist}.
     * <p>
     * It will then check to see what is in the
     * {@link com.lhf.game.enums.EquipmentSlots#WEAPON Weapon} slot according to
     * {@link com.lhf.game.creature.inventory.EquipmentOwner#getEquipped(EquipmentSlots)
     * EquipmentOwner.getEquipped(EquipmentSlots)} and returns that if present.
     * <p>
     * Finally, it defaults to {@link com.lhf.game.creature.ICreature.Fist Fist}.
     * 
     * @return {@link com.lhf.game.item.Weapon Weapon} that should not be null
     */
    @Override
    public default Weapon defaultWeapon() {
        if (this.getEquipped(EquipmentSlots.ARMOR) != null) {
            this.removeEffectByName(DMBlessing.name);
        }
        if (this.hasEffect(DMBlessing.name)) {
            return INonPlayerCharacter.blessedFist;
        }
        Equipable found = this.getEquipped(EquipmentSlots.WEAPON);
        if (found != null && found instanceof Weapon weapon) {
            return weapon;
        }
        return ICreature.defaultFist;
    }

    /**
     * Provides a default name for a default conversation tree file.
     * 
     * @see {@link com.lhf.game.creature.conversation.ConversationTree
     *      ConversationTree}
     */
    public static final String defaultConvoTreeName = "verbal_default";

    /**
     * Sets the {@link com.lhf.game.creature.conversation.ConversationTree
     * ConversationTree} for the NPC.
     * 
     * @param tree
     */
    public abstract void setConvoTree(ConversationTree tree);

    /**
     * Uses a {@link com.lhf.game.creature.conversation.ConversationManager
     * ConversationManager} to load a
     * {@link com.lhf.game.creature.conversation.ConversationTree ConversationTree}
     * file and sets that as the NPCs ConversationTree.
     * 
     * @param manager {@link com.lhf.game.creature.conversation.ConversationManager
     *                ConversationManager} to use
     * @param name    of the
     *                {@link com.lhf.game.creature.conversation.ConversationTree
     *                ConversationTree} file
     */
    public abstract void setConvoTree(ConversationManager manager, String name);

    /**
     * Retrieves the {@link com.lhf.game.creature.conversation.ConversationTree
     * ConversationTree} that the NPC uses.
     * 
     * @return {@link com.lhf.game.creature.conversation.ConversationTree
     *         ConversationTree} or null if none available.
     */
    public abstract ConversationTree getConvoTree();

    /**
     * Restores the NPC to the default faction of
     * {@link com.lhf.game.enums.CreatureFaction#NPC NPC}.
     */
    @Override
    public default void restoreFaction() {
        this.setFaction(CreatureFaction.NPC);
    }

    /**
     * Returns the {@link com.lhf.game.creature.INonPlayerCharacter.HarmMemories
     * HarmMemories} of this NPC.
     * Should never be null.
     * 
     * @return {@link com.lhf.game.creature.INonPlayerCharacter.HarmMemories
     *         HarmMemories}
     */
    public abstract HarmMemories getHarmMemories();

    /**
     * Sets the {@link com.lhf.server.client.CommandInvoker Controller} for the NPC.
     * 
     * @param cont {@link com.lhf.server.client.CommandInvoker Controller}
     */
    public abstract void setController(CommandInvoker cont);

    /**
     * Gets the name of the Creature that this NPC will follow, or null
     * 
     * @return
     */
    public abstract String getLeaderName();

    /**
     * Sets the name of the Creature that this NPC will follow, can be null
     * 
     * @param leaderName
     */
    public abstract void setLeaderName(String leaderName);

}
