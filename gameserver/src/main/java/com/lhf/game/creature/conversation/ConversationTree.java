package com.lhf.game.creature.conversation;

import java.util.TreeMap;

public class ConversationTree { // TODO: make the GameMessages more easily parseable
    public class ConversationTreeNode {
        StringBuilder block;
        TreeMap<String, ConversationTreeNode> forwardMap;

        public ConversationTreeNode() {
            this.block = new StringBuilder();
            this.forwardMap = new TreeMap<>();
        }
    }
}
