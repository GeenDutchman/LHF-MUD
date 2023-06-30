package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.EntityEffect;
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
import com.lhf.game.item.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.game.magic.concrete.DMBlessing;
import com.lhf.messages.out.CreatureAffectedMessage;

public class NonPlayerCharacter extends Creature {
    public static class BlessedFist extends Weapon {
        private final static CreatureEffectSource source = new CreatureEffectSource("Blessed Punch",
                new EffectPersistence(TickType.INSTANT),
                new EffectResistance(EnumSet.allOf(Attributes.class), Stats.AC), "A blessed fist punches harder.",
                false);

        BlessedFist(NonPlayerCharacter owner) {
            super("Blessed Fist", false, Set.of(BlessedFist.source), DamageFlavor.MAGICAL_BLUDGEONING,
                    WeaponSubtype.CREATUREPART);
            if (BlessedFist.source.getDamages().size() == 0) {
                for (DamageFlavor df : DamageFlavor.values()) {
                    BlessedFist.source.addDamage(new DamageDice(1, DieType.FOUR, df));
                }
            }

            this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.MONSTERPART);
            this.slots = List.of(EquipmentSlots.WEAPON);
            this.descriptionString = "This is a " + getName() + " attached to a " + owner.getName()
                    + "\n";
        }
    }

    private final Weapon defaultWeapon = new BlessedFist(this);
    private ConversationTree convoTree = null;
    public static final String defaultConvoTreeName = "verbal_default";

    public class HarmMemories {
        private Optional<String> lastAttackerName = Optional.empty();
        private int lastDamageAmount = 0;
        private Optional<String> lastMassAttackerName = Optional.empty();
        private int lastMassDamageAmount = 0;

        public String getOwnerName() {
            return NonPlayerCharacter.this.getName();
        }

        public Optional<String> getLastAttackerName() {
            return lastAttackerName;
        }

        public int getLastDamageAmount() {
            return lastDamageAmount;
        }

        public Optional<String> getLastMassAttackerName() {
            return lastMassAttackerName;
        }

        public int getLastMassDamageAmount() {
            return lastMassDamageAmount;
        }

        public HarmMemories reset() {
            this.lastAttackerName = Optional.empty();
            this.lastMassAttackerName = Optional.empty();
            this.lastDamageAmount = 0;
            this.lastMassDamageAmount = 0;
            return this;
        }

        public HarmMemories update(CreatureAffectedMessage cam) {
            if (cam == null || !NonPlayerCharacter.this.equals(cam.getAffected())) {
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

    private transient HarmMemories harmMemories = new HarmMemories();

    protected static abstract class AbstractNPCBuilder<T extends AbstractNPCBuilder<T>>
            extends Creature.CreatureBuilder<T> {
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
                this.conversationTree = convoManager.convoTreeFromFile(NonPlayerCharacter.defaultConvoTreeName);
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

        protected NonPlayerCharacter register(NonPlayerCharacter npc) {
            if (this.aiRunner != null) {
                this.aiRunner.register(npc, this.getAiHandlersAsArray());
            }
            return npc;
        }

        @Override
        public NonPlayerCharacter build() {
            return this.register(new NonPlayerCharacter(this));
        }

    }

    public static class NPCBuilder extends NonPlayerCharacter.AbstractNPCBuilder<NPCBuilder> {
        private NPCBuilder(AIRunner aiRunner) {
            super(aiRunner);
        }

        public static NPCBuilder getInstance(AIRunner aiRunner) {
            return new NPCBuilder(aiRunner);
        }
    }

    public NonPlayerCharacter(AbstractNPCBuilder<?> builder) {
        super(builder);
        this.convoTree = builder.getConversationTree();
    }

    public static NPCBuilder getNPCBuilder(AIRunner aiRunner) {
        return new NPCBuilder(aiRunner);
    }

    @Override
    public boolean equipItem(String itemName, EquipmentSlots slot) {
        boolean did = super.equipItem(itemName, slot);
        if (this.getEquipped(EquipmentSlots.ARMOR) != null) {
            this.removeEffectByName(DMBlessing.name);
        }
        return did;
    }

    @Override
    public void setFaction(CreatureFaction faction) {
        if (!CreatureFaction.NPC.equals(faction)) {
            this.removeEffectByName(DMBlessing.name);
        }
        super.setFaction(faction);
    }

    @Override
    public Weapon getWeapon() {
        if (this.getEquipped(EquipmentSlots.ARMOR) != null) {
            this.removeEffectByName(DMBlessing.name);
        }
        if (this.hasEffect(DMBlessing.name)) {
            return this.defaultWeapon;
        }
        return super.getWeapon();
    }

    public void setConvoTree(ConversationTree tree) {
        this.convoTree = tree;
    }

    public void setConvoTree(ConversationManager manager, String name) {
        if (name != null && manager != null) {
            try {
                this.convoTree = manager.convoTreeFromFile(name);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot load that convo file");
                e.printStackTrace();
            }
        } else {
            this.convoTree = null;
        }
    }

    public ConversationTree getConvoTree() {
        return this.convoTree;
    }

    @Override
    public void restoreFaction() {
        this.setFaction(CreatureFaction.NPC);
    }

    public HarmMemories getHarmMemories() {
        return harmMemories;
    }

    @Override
    public CreatureAffectedMessage processEffect(EntityEffect effect, boolean reverse) {
        CreatureAffectedMessage cam = super.processEffect(effect, reverse);
        this.getHarmMemories().update(cam);
        return cam;
    }

}
