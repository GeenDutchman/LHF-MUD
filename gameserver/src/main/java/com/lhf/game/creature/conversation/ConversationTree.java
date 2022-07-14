package com.lhf.game.creature.conversation;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import com.lhf.game.creature.Creature;

public class ConversationTree {
    private ConversationTreeNode start;
    private Map<UUID, ConversationTreeNode> tree;
    private Map<Creature, UUID> bookmarks;
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
                if (lowerMessage.matches(".*\\b" + greet + "\\b.*") && this.start != null) {
                    this.bookmarks.put(c, this.start.getNodeID());
                    return this.start.getBody();
                }
            }
            return null;
        }
        UUID id = this.bookmarks.get(c);
        ConversationTreeNode node = this.tree.get(id);
        if (node != null) {
            UUID newId = node.getNextNodeID(message);
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
