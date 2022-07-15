package com.lhf.game.creature.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lhf.game.creature.Creature;
import com.lhf.server.interfaces.NotNull;

public class ConversationTree {
    private ConversationTreeNode start;
    private Map<UUID, ConversationTreeNode> nodes;
    private Map<UUID, List<ConversationTreeBranch>> branches;
    private Map<Creature, UUID> bookmarks;
    private SortedSet<ConversationTreeBranch> greetings;
    private SortedSet<String> repeatWords;
    private String endOfConvo;

    public ConversationTree(@NotNull ConversationTreeNode startNode) {
        this.nodes = new TreeMap<>();
        this.branches = new TreeMap<>();
        this.start = startNode;
        this.nodes.put(startNode.getNodeID(), startNode);
        this.bookmarks = new TreeMap<>();
        this.repeatWords = new TreeSet<>();
        this.greetings = new TreeSet<>();
        this.repeatWords.add("again");
        this.repeatWords.add("repeat");
        this.addGreeting(Pattern.compile("^hello\\b", Pattern.CASE_INSENSITIVE));
        this.addGreeting(Pattern.compile("^hi\\b", Pattern.CASE_INSENSITIVE));
        this.endOfConvo = "Goodbye";
    }

    public void addGreeting(Pattern regex) {
        this.greetings.add(new ConversationTreeBranch(regex, this.start.getNodeID()));
    }

    public ConversationTreeNode addNode(UUID nodeID, Pattern regex, ConversationTreeNode nextNode) {
        if (nodeID == null) {
            nodeID = this.start.getNodeID();
        }
        if (!this.branches.containsKey(nodeID)) {
            this.branches.put(nodeID, new ArrayList<>());
        }
        this.branches.get(nodeID).add(new ConversationTreeBranch(regex, nextNode.getNodeID()));

        return this.nodes.put(nextNode.getNodeID(), nextNode);
    }

    public String listen(Creature c, String message) {
        if (!this.bookmarks.containsKey(c)) {
            for (ConversationTreeBranch greet : this.greetings) {
                Matcher matcher = greet.getKeyword().matcher(message);
                if (matcher.find()) {
                    this.bookmarks.put(c, this.start.getNodeID());
                    return this.start.getBody();
                }
            }
            return null;
        }
        UUID id = this.bookmarks.get(c);
        if (this.branches.containsKey(id)) {
            for (ConversationTreeBranch branch : this.branches.get(id)) {
                Matcher matcher = branch.getKeyword().matcher(message);
                if (matcher.find()) {
                    UUID nextID = branch.getNodeID();
                    ConversationTreeNode node = this.nodes.get(nextID);
                    if (node != null) {
                        this.bookmarks.put(c, nextID);
                        return node.getBody();
                    }
                }
            }
        }
        this.forget(c);
        return this.endOfConvo;
    }

    public void forget(Creature c) {
        this.bookmarks.remove(c);
    }

    public String getEndOfConvo() {
        return endOfConvo;
    }

    public void setEndOfConvo(String endOfConvo) {
        this.endOfConvo = endOfConvo;
    }

}
