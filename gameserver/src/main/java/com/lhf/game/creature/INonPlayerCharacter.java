package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.TickType;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.game.magic.concrete.DMBlessing;
import com.lhf.messages.events.CreatureAffectedEvent;
import com.lhf.server.client.Controller;
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
                false);

        BlessedFist() {
            super("Blessed Fist", false, Set.of(BlessedFist.source), DamageFlavor.MAGICAL_BLUDGEONING,
                    WeaponSubtype.CREATUREPART);
            if (BlessedFist.source.getDamages().size() == 0) {
                for (DamageFlavor df : DamageFlavor.values()) {
                    BlessedFist.source.addDamage(new DamageDice(1, DieType.FOUR, df));
                }
            }

            this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.MONSTERPART);
            this.slots = List.of(EquipmentSlots.WEAPON);
            this.descriptionString = "This is a Fist attached to a Creature who is blessed\n";

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
            CreatureEffect ce = cam.getEffect();
            if (ce == null) {
                return this;
            }
            MultiRollResult damage = ce.getDamageResult();
            if (ce.isOffensive()) {
                this.lastAttackerName = Optional.of(ce.creatureResponsible().getName());
                this.lastDamageAmount = damage.getTotal();
            }
            if (damage.getTotal() >= this.lastMassDamageAmount) {
                this.lastMassDamageAmount = damage.getTotal();
                this.lastMassAttackerName = Optional.of(ce.creatureResponsible().getName());
            }
            return this;
        }

    }

    /**
     * Builder pattern root for all NPCs.
     */
    public static abstract class AbstractNPCBuilder<T extends AbstractNPCBuilder<T>>
            extends ICreature.CreatureBuilder<T> {
        private ConversationTree conversationTree = null;
        private AIRunner aiRunner;
        private List<AIHandler> aiHandlers;

        protected AbstractNPCBuilder(AIRunner aiRunner) {
            super();
            this.setFaction(CreatureFaction.NPC);
            this.aiRunner = aiRunner;
            this.aiHandlers = new ArrayList<>();
        }

        @Override
        protected T getThis() {
            return this.thisObject;
        }

        public T setConversationTree(ConversationTree tree) {
            this.conversationTree = tree;
            return this.getThis();
        }

        public T setConversationTree(ConversationManager manager, String name) {
            if (name != null && manager != null) {
                try {
                    this.conversationTree = manager.convoTreeFromFile(name);
                } catch (FileNotFoundException e) {
                    System.err.println("Cannot load that convo file");
                    e.printStackTrace();
                }
            } else {
                this.conversationTree = null;
            }
            return this.getThis();
        }

        public ConversationTree getConversationTree() {
            return this.conversationTree;
        }

        public T useDefaultConversation(ConversationManager convoManager) throws FileNotFoundException {
            if (convoManager != null) {
                this.conversationTree = convoManager.convoTreeFromFile(INonPlayerCharacter.defaultConvoTreeName);
            }
            return this.getThis();
        }

        public AIRunner getAiRunner() {
            return aiRunner;
        }

        public T setAiRunner(AIRunner aiRunner) {
            this.aiRunner = aiRunner;
            return this.getThis();
        }

        public T addAIHandler(AIHandler handler) {
            if (handler != null) {
                this.aiHandlers.add(handler);
            }
            return this.getThis();
        }

        public List<AIHandler> getAIHandlers() {
            return this.aiHandlers;
        }

        public AIHandler[] getAiHandlersAsArray() {
            return this.aiHandlers.toArray(new AIHandler[this.aiHandlers.size()]);
        }

        public T clearAIHandlers() {
            this.aiHandlers.clear();
            return this.getThis();
        }

        protected INonPlayerCharacter register(INonPlayerCharacter npc) {
            if (this.aiRunner != null) {
                this.aiRunner.register(npc, this.getAiHandlersAsArray());
            }
            return npc;
        }

        /**
         * Builds an NPC that is potentially not registered with an
         * {@link com.lhf.game.creature.intelligence.AIRunner AIRunner}.
         * 
         * @return the built NPC
         */
        protected abstract INonPlayerCharacter preEnforcedRegistrationBuild();

        @Override
        public INonPlayerCharacter build() {
            return this.register(this.preEnforcedRegistrationBuild());
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
     * Sets the {@link com.lhf.server.client.Controller Controller} for the NPC.
     * 
     * @param cont {@link com.lhf.server.client.Controller Controller}
     */
    public abstract void setController(Controller cont);

}
