package com.lhf.game.creature.conversation;

import java.util.TreeMap;

public class ConversationTree {
    public class ConversationTreeNode {
        StringBuilder block;
        TreeMap<String, ConversationTreeNode> forwardMap;

        public ConversationTreeNode() {
            this.block = new StringBuilder();
            this.forwardMap = new TreeMap<>();
        }
    }
}
