package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.function.UnaryOperator;

import com.lhf.game.EntityEffect;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.magic.concrete.DMBlessing;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.events.CreatureAffectedEvent;
import com.lhf.server.client.CommandInvoker;

public class NonPlayerCharacter extends Creature implements INonPlayerCharacter {

    public static class NPCBuilder extends INonPlayerCharacter.AbstractNPCBuilder<NPCBuilder, NonPlayerCharacter> {
        private NPCBuilder() {
            super();
        }

        public static NPCBuilder getInstance() {
            return new NPCBuilder();
        }

        @Override
        protected NonPlayerCharacter preEnforcedRegistrationBuild(CommandChainHandler successor,
                StatblockManager statblockManager, UnaryOperator<NPCBuilder> composedlazyLoaders)
                throws FileNotFoundException {
            if (statblockManager != null) {
                this.loadStatblock(statblockManager);
            }
            if (composedlazyLoaders != null) {
                composedlazyLoaders.apply(this.getThis());
            }
            NonPlayerCharacter created = new NonPlayerCharacter(this);
            created.setSuccessor(successor);
            return created;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && obj instanceof NPCBuilder;
        }

    }

    public static NPCBuilder getNPCBuilder() {
        return new NPCBuilder();
    }

    private ConversationTree convoTree = null;
    private transient final HarmMemories harmMemories = HarmMemories.makeMemories(this);

    public NonPlayerCharacter(AbstractNPCBuilder<?, ? extends INonPlayerCharacter> builder) {
        super(builder);
        this.convoTree = builder.getConversationTree();
    }

    @Override
    public void setConvoTree(ConversationTree tree) {
        this.convoTree = tree;
    }

    @Override
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

    @Override
    public ConversationTree getConvoTree() {
        return this.convoTree;
    }

    @Override
    public HarmMemories getHarmMemories() {
        return this.harmMemories;
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
    public CreatureAffectedEvent processEffect(EntityEffect effect, boolean reverse) {
        CreatureAffectedEvent cam = super.processEffect(effect, reverse);
        this.getHarmMemories().update(cam);
        return cam;
    }

    @Override
    public void setController(CommandInvoker cont) {
        super.setController(cont);
    }
}
