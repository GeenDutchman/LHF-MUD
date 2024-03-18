package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.Map;

import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.magic.concrete.PlotArmor;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.events.CreatureAffectedEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

public class NonPlayerCharacter extends Creature implements INonPlayerCharacter {

    public static INPCBuildInfo getNPCBuilder() {
        return new INPCBuildInfo();
    }

    private ConversationTree convoTree = null;
    private transient final HarmMemories harmMemories;
    private String leaderName;

    protected NonPlayerCharacter(INonPlayerCharacterBuildInfo builder,
            @NotNull CommandInvoker controller, CommandChainHandler successor,
            ConversationTree conversationTree) {
        super(builder, controller, successor);
        this.convoTree = conversationTree;
        this.leaderName = builder.getLeaderName();
        this.harmMemories = HarmMemories.makeMemories(this);
    }

    @Override
    public void acceptCreatureVisitor(CreatureVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected Map<AMessageType, CommandHandler> buildCommands() {
        Map<AMessageType, CommandHandler> generated = super.buildCommands();
        generated.put(AMessageType.FOLLOW, INonPlayerCharacter.followHandler);
        return generated;
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
            this.removeEffectByName(PlotArmor.name);
        }
        return did;
    }

    @Override
    public void setFaction(CreatureFaction faction) {
        if (!CreatureFaction.NPC.equals(faction)) {
            this.removeEffectByName(PlotArmor.name);
        }
        super.setFaction(faction);
    }

    @Override
    public CreatureAffectedEvent processEffectApplication(CreatureEffect effect) {
        CreatureAffectedEvent cam = super.processEffectApplication(effect);
        final HarmMemories memories = this.getHarmMemories();
        if (memories != null) {
            memories.update(cam);
        }
        return cam;
    }

    @Override
    public void setController(CommandInvoker cont) {
        super.setController(cont);
    }

    @Override
    public String getLeaderName() {
        return leaderName;
    }

    @Override
    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

}
