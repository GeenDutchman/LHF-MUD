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
import com.lhf.game.creature.conversation.ConversationContext.ConversationContextKey;
import com.lhf.server.interfaces.NotNull;

public class ConversationTree implements Serializable {
    private ConversationTreeNode start;
    private Map<UUID, ConversationTreeNode> nodes;
    private Map<UUID, List<ConversationTreeBranch>> branches;
    private transient Map<Creature, ConversationContext> bookmarks;
    private SortedSet<ConversationTreeBranch> greetings;
    private SortedSet<Pattern> repeatWords;
    private String endOfConvo;
    private String notRecognized;
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
        this.notRecognized = "What did you say? ...";
        this.tagkeywords = true;
    }

    public void addGreeting(Pattern regex) {
        this.greetings.add(new ConversationTreeBranch(regex, this.start.getNodeID()));
    }

    public ConversationTreeBranch addNode(UUID nodeID, Pattern regex, ConversationTreeNode nextNode) {
        if (nodeID == null) {
            nodeID = this.start.getNodeID();
        }
        if (!this.branches.containsKey(nodeID)) {
            this.branches.put(nodeID, new ArrayList<>());
        }
        ConversationTreeBranch branch = new ConversationTreeBranch(regex, nextNode.getNodeID());
        this.branches.get(nodeID).add(branch);
        this.nodes.put(nextNode.getNodeID(), nextNode);
        return branch;
    }

    private ConversationTreeNodeResult tagIt(ConversationContext ctx, ConversationTreeNode node) {
        if (node == null) {
            return null;
        }
        ConversationTreeNodeResult result = node.getResult();
        String output = node.getBody();
        if (this.branches.containsKey(node.getNodeID()) && this.tagkeywords) {
            for (ConversationTreeBranch branch : this.branches.get(node.getNodeID())) {
                if (branch.canAccess(ctx)) {
                    Matcher matcher = branch.getRegex().matcher(output);
                    output = matcher.replaceFirst("<convo>$0</convo>");
                }
            }
        }

        for (String contextual : ctx.keySet()) {
            Pattern pattern = Pattern.compile("\\b" + contextual + "\\b");
            Matcher matcher = pattern.matcher(output);
            output = matcher.replaceAll(ctx.getOrDefault(contextual, ""));
        }

        result.setBody(output);
        return result;
    }

    public ConversationTreeNodeResult listen(Creature c, String message) {
        if (!this.bookmarks.containsKey(c)) {
            for (ConversationTreeBranch greet : this.greetings) {
                Matcher matcher = greet.getRegex().matcher(message);
                if (matcher.find()) {
                    ConversationContext ctx = new ConversationContext();
                    ctx.put(ConversationContextKey.TALKER_NAME, c.getName());
                    ctx.put(ConversationContextKey.TALKER_TAGGED_NAME, c.getColorTaggedName());
                    ctx.addTrail(this.start.getNodeID());
                    this.bookmarks.put(c, ctx);
                    return this.tagIt(ctx, this.start);
                }
            }
            return null;
        }
        ConversationContext ctx = this.bookmarks.get(c);
        UUID id = ctx.getTrailEnd();
        int hasBranches = this.branches.containsKey(id) ? this.branches.get(id).size() : 0;
        if (hasBranches > 0) {
            for (ConversationTreeBranch branch : this.branches.get(id)) {
                if (branch.canAccess(ctx)) {
                    Matcher matcher = branch.getRegex().matcher(message);
                    if (matcher.find()) {
                        UUID nextID = branch.getNodeID();
                        ConversationTreeNode node = this.nodes.get(nextID);
                        if (node != null) {
                            this.bookmarks.get(c).addTrail(nextID);
                            return this.tagIt(ctx, node);
                        }
                    }
                } else {
                    hasBranches--;
                }
            }
        }
        for (Pattern repeater : this.repeatWords) {
            Matcher matcher = repeater.matcher(message);
            if (matcher.find() && this.nodes.get(id) != null) {
                return this.tagIt(ctx, this.nodes.get(id));
            }
        }

        if (hasBranches <= 0) {
            this.bookmarks.get(c).addTrail(this.start.getNodeID());
            return new ConversationTreeNodeResult(this.endOfConvo);
        }
        return new ConversationTreeNodeResult(this.notRecognized);
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

    public String getNotRecognized() {
        return notRecognized;
    }

    public void setNotRecognized(String notRecognized) {
        this.notRecognized = notRecognized;
    }

    public void setGreetings(Set<ConversationTreeBranch> greetings) {
        this.greetings = new TreeSet<>(greetings);
    }

    public void setRepeats(Set<Pattern> repeats) {
        this.repeatWords = new TreeSet<>(new SortPatternByLength());
        this.repeatWords.addAll(repeats);
    }

}
