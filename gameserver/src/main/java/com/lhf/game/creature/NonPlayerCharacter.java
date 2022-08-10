package com.lhf.game.creature;

import java.io.FileNotFoundException;

import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.out.OutMessage;

public class NonPlayerCharacter extends Creature {
    private ConversationTree convoTree = null;
    public static final String defaultConvoTreeName = "verbal_default";

    public NonPlayerCharacter() {
        super();
    }

    public NonPlayerCharacter(String name, Statblock statblock) {
        super(name, statblock);
        this.setFaction(CreatureFaction.NPC);
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
    public void sendMsg(OutMessage msg) {
        if (this.getController() == null) {
            BasicAI lizardbrain = new BasicAI(this);
            this.setController(lizardbrain);
        }
        super.sendMsg(msg);
    }

    @Override
    public void restoreFaction() {
        this.setFaction(CreatureFaction.NPC);
    }
}
