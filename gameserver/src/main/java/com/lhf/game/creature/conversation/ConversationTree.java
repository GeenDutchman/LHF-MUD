package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private transient Map<Creature, UUID> bookmarks;
    private SortedSet<ConversationTreeBranch> greetings;
    private SortedSet<Pattern> repeatWords;
    private String endOfConvo;
    private boolean tagkeywords;
    // TODO: tag keywords optionally

    private class SortPatternByLength implements Comparator<Pattern>, Serializable {

        @Override
        public int compare(Pattern arg0, Pattern arg1) {
            if (arg0 == null || arg1 == null) {
                throw new NullPointerException("Cannot compare null Patterns");
            }
            if (arg0.equals(arg1)) {
                return 0;
            }
            return arg0.toString().compareTo(arg1.toString()) * -1;
        }

    }

    public ConversationTree(@NotNull ConversationTreeNode startNode) {
        this.nodes = new TreeMap<>();
        this.branches = new TreeMap<>();
        this.start = startNode;
        this.nodes.put(startNode.getNodeID(), startNode);
        this.bookmarks = new TreeMap<>();
        this.repeatWords = new TreeSet<>(new SortPatternByLength());
        this.greetings = new TreeSet<>();
        this.repeatWords.add(Pattern.compile("\\bagain\\b", Pattern.CASE_INSENSITIVE));
        this.repeatWords.add(Pattern.compile("\\brepeat\\b", Pattern.CASE_INSENSITIVE));
        this.addGreeting(Pattern.compile("^hello\\b", Pattern.CASE_INSENSITIVE));
        this.addGreeting(Pattern.compile("^hi\\b", Pattern.CASE_INSENSITIVE));
        this.endOfConvo = "Goodbye";
        this.tagkeywords = true;
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

    private ConversationTreeNodeResult tagIt(ConversationTreeNode node) {
        if (node == null) {
            return null;
        }
        ConversationTreeNodeResult result = node.getResult();
        String output = node.getBody();
        if (this.branches.containsKey(node.getNodeID()) && this.tagkeywords) {
            for (ConversationTreeBranch branch : this.branches.get(node.getNodeID())) {
                Matcher matcher = branch.getRegex().matcher(output);
                output = matcher.replaceFirst("<convo>$0</convo>");
            }
        }
        result.setBody(output);
        return result;
    }

    public ConversationTreeNodeResult listen(Creature c, String message) {
        if (!this.bookmarks.containsKey(c)) {
            for (ConversationTreeBranch greet : this.greetings) {
                Matcher matcher = greet.getRegex().matcher(message);
                if (matcher.find()) {
                    this.bookmarks.put(c, this.start.getNodeID());
                    return this.tagIt(this.start);
                }
            }
            return null;
        }
        UUID id = this.bookmarks.get(c);
        if (this.branches.containsKey(id)) {
            for (ConversationTreeBranch branch : this.branches.get(id)) {
                Matcher matcher = branch.getRegex().matcher(message);
                if (matcher.find()) {
                    UUID nextID = branch.getNodeID();
                    ConversationTreeNode node = this.nodes.get(nextID);
                    if (node != null) {
                        this.bookmarks.put(c, nextID);
                        return this.tagIt(node);
                    }
                }
            }
        }
        for (Pattern repeater : this.repeatWords) {
            Matcher matcher = repeater.matcher(message);
            if (matcher.find() && this.nodes.get(id) != null) {
                return this.tagIt(this.nodes.get(id));
            }
        }
        this.forget(c);
        return new ConversationTreeNodeResult(this.endOfConvo);
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

    public void setGreetings(Set<ConversationTreeBranch> greetings) {
        this.greetings = new TreeSet<>(greetings);
    }

    public void setRepeats(Set<Pattern> repeats) {
        this.repeatWords = new TreeSet<>(new SortPatternByLength());
        this.repeatWords.addAll(repeats);
    }

}
