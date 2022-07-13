package com.lhf.game.creature.conversation;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.lhf.game.creature.Creature;

public class ConversationTree {
    private ConversationTreeNode start;
    private Map<ConversationTreeNodeID, ConversationTreeNode> tree;
    private Map<Creature, ConversationTreeNodeID> bookmarks;
    private SortedSet<String> repeatWords;
    private SortedSet<String> greetings;

    public ConversationTree() {
        this.start = null;
        this.tree = new TreeMap<>();
        this.bookmarks = new TreeMap<>();
        this.repeatWords = new TreeSet<>();
        this.greetings = new TreeSet<>();
        this.repeatWords.add("again");
        this.repeatWords.add("repeat");
        this.greetings.add("hello");
        this.greetings.add("hi");
    }

    public ConversationTreeNode setStartNode(ConversationTreeNode starter) {
        if (!this.tree.containsKey(starter.getNodeID())) {
            this.addNode(starter);
        }
        this.start = starter;
        return this.start;
    }

    public ConversationTreeNode addNode(ConversationTreeNode node) {
        return this.tree.put(node.getNodeID(), node);
    }

    public String listen(Creature c, String message) {
        String lowerMessage = message.toLowerCase();
        if (!this.bookmarks.containsKey(c)) {
            for (String greet : this.greetings) {
                if (lowerMessage.matches(".*\\b" + greet + "\\b.*")) {
                    this.bookmarks.put(c, this.start.getNodeID()); // TODO: what to do if start is null
                    return this.start.getBody();
                }
            }
            return null;
        }
        ConversationTreeNodeID id = this.bookmarks.get(c);
        ConversationTreeNode node = this.tree.get(id);
        if (node != null) {
            ConversationTreeNodeID newId = node.getNextNodeID(message);
            node = this.tree.get(newId);
            if (node != null) {
                this.bookmarks.put(c, newId);
                return node.getBody();
            }
            return "Sorry, what was that?";
        }
        return "I'm sorry, what did you say?";
    }

    public void forget(Creature c) {
        this.bookmarks.remove(c);
    }

}
