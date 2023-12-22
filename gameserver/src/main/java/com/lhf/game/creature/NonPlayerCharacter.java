package com.lhf.game.creature;

import java.io.FileNotFoundException;

import com.lhf.game.EntityEffect;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.magic.concrete.DMBlessing;
import com.lhf.messages.events.CreatureAffectedEvent;

public class NonPlayerCharacter extends Creature implements INonPlayerCharacter {

    public static class NPCBuilder extends INonPlayerCharacter.AbstractNPCBuilder<NPCBuilder> {
        private NPCBuilder(AIRunner aiRunner) {
            super(aiRunner);
        }

        public static NPCBuilder getInstance(AIRunner aiRunner) {
            return new NPCBuilder(aiRunner);
        }

        @Override
        protected INonPlayerCharacter preEnforcedRegistrationBuild() {
            return new NonPlayerCharacter(this);
        }
    }

    public static NPCBuilder getNPCBuilder(AIRunner aiRunner) {
        return new NPCBuilder(aiRunner);
    }

    private ConversationTree convoTree = null;
    private transient final HarmMemories harmMemories = HarmMemories.makeMemories(this);

    public NonPlayerCharacter(AbstractNPCBuilder<?> builder) {
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
}
