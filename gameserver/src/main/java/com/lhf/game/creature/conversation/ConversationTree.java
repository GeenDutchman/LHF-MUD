package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lhf.RichOutput.OutputSequence;
import com.lhf.RichOutput.PrintingInstructions;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContext;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContextKey;
import com.lhf.server.client.Client.ClientID;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

public class ConversationTree implements Serializable {
    private String treeName;
    private ConversationTreeNode start;
    // TODO: wait for `SequencedCollection` from Java21
    private Map<UUID, ConversationTreeNode> nodes;
    private Map<UUID, List<ConversationTreeBranch>> branches;
    private transient Map<ClientID, ConversationContext> bookmarks;
    private SortedSet<ConversationTreeBranch> greetings;
    private SortedSet<ConversationPattern> repeatWords;
    private String endOfConvo;
    private String notRecognized;
    private boolean tagkeywords;

    public ConversationTree(@NotNull ConversationTreeNode startNode) {
        this.treeName = UUID.randomUUID().toString();
        this.nodes = new LinkedHashMap<>();
        this.branches = new LinkedHashMap<>();
        this.start = startNode;
        this.nodes.put(startNode.getNodeID(), startNode);
        this.init();
        this.endOfConvo = "Goodbye";
        this.notRecognized = "What did you say? ...";
        this.tagkeywords = true;
    }

    public ConversationTree makeCopy() {
        return new ConversationTree(this.start);
    }

    protected ConversationTree init() {
        this.initBookmarks();
        this.addDefaultGreetings();
        this.addDefaultRepeatWords();
        return this;
    }

    protected ConversationTree initBookmarks() {
        if (this.bookmarks == null) {
            this.bookmarks = new TreeMap<>();
        }
        return this;
    }

    protected ConversationTree addDefaultGreetings() {
        if (this.greetings == null) {
            this.greetings = new TreeSet<>();
        }
        this.addGreeting(new ConversationPattern("hello", "^\\s*hello\\b", Pattern.CASE_INSENSITIVE));
        this.addGreeting(new ConversationPattern("hi", "^\\s*hi\\b", Pattern.CASE_INSENSITIVE));
        return this;
    }

    protected void addDefaultRepeatWords() {
        if (this.repeatWords == null) {
            this.repeatWords = new TreeSet<>();
        }
        this.repeatWords.add(new ConversationPattern("again", "\\bagain\\b", Pattern.CASE_INSENSITIVE));
        this.repeatWords.add(new ConversationPattern("repeat", "\\brepeat\\b", Pattern.CASE_INSENSITIVE));
    }

    public String getTreeName() {
        return treeName;
    }

    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }

    public void addGreeting(ConversationPattern regex) {
        if (this.greetings == null) {
            this.greetings = new TreeSet<>();
        }
        this.greetings.add(new ConversationTreeBranch(regex, this.start.getNodeID()));
    }

    public ConversationTreeBranch addNode(UUID nodeID, ConversationPattern regex, ConversationTreeNode nextNode) {
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

    @Deprecated
    private ConversationTreeNodeResult tagIt(ConversationContext ctx, ConversationTreeNode node) {
        if (node == null) {
            return null;
        }
        TreeSet<ConversationPattern> branches = new TreeSet<>();
        if (this.branches.containsKey(node.getNodeID()) && this.tagkeywords) {
            for (ConversationTreeBranch branch : this.branches.get(node.getNodeID())) {
                if (branch.canAccess(ctx)) {
                    branches.add(branch.getRegex());
                }
            }
        }
        ConversationTreeNodeResult result = ConversationTreeNodeResult.create(ctx, node.getBodySequence(),
                node.getPrompts(), branches);

        return result;
    }

    protected ConversationTreeNodeResult backtrack(CommandInvoker talker) {
        ConversationContext ctx = this.bookmarks.get(talker.getClientID());
        ctx.backtrack();
        UUID backNode = ctx.getTrailEnd();
        return this.tagIt(ctx, this.nodes.get(backNode));
    }

    protected ConversationTreeNode getNode(UUID nodeID) {
        return this.nodes.get(nodeID);
    }

    protected Map<UUID, ConversationTreeNode> getNodes() {
        return Collections.unmodifiableMap(this.nodes);
    }

    protected ConversationTreeNode getCurrentNode(CommandInvoker talker) {
        ConversationContext ctx = this.bookmarks.get(talker.getClientID());
        UUID nodeID = ctx.getTrailEnd();
        return this.getNode(nodeID);
    }

    protected List<ConversationTreeBranch> getBranches(UUID nodeID) {
        return this.branches.get(nodeID);
    }

    public ConversationTreeNodeResult listen(CommandInvoker talker, String message) {
        if (!this.bookmarks.containsKey(talker.getClientID())) {
            for (ConversationTreeBranch greet : this.greetings) {
                Matcher matcher = greet.getRegex().matcher(message);
                if (matcher.find()) {
                    ConversationContext ctx = new ConversationContext();
                    ctx.put(ConversationContextKey.TALKER_NAME, ConversationTransformer.ofString(talker.getName()));
                    ctx.put(ConversationContextKey.TALKER_TAGGED_NAME, ConversationTransformer.ofTaggable(talker));
                    ctx.addTrail(this.start.getNodeID());
                    this.bookmarks.put(talker.getClientID(), ctx);
                    return this.tagIt(ctx, this.start);
                }
            }
            return null;
        }
        ConversationContext ctx = this.bookmarks.get(talker.getClientID());
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
                            this.bookmarks.get(talker.getClientID()).addTrail(nextID);
                            return this.tagIt(ctx, node);
                        }
                    }
                } else {
                    hasBranches--;
                }
            }
        }
        for (ConversationPattern repeater : this.repeatWords) {
            Matcher matcher = repeater.matcher(message);
            if (matcher.find() && this.nodes.get(id) != null) {
                return this.tagIt(ctx, this.nodes.get(id));
            }
        }

        if (hasBranches <= 0) {
            this.bookmarks.get(talker.getClientID()).addTrail(this.start.getNodeID());
            return ConversationTreeNodeResult.fromString(ctx, this.endOfConvo, null, null);
        }
        return ConversationTreeNodeResult.fromString(ctx, this.notRecognized, null, repeatWords);
    }

    public void forgetBookmark(CommandInvoker talker) {
        this.bookmarks.remove(talker.getClientID());
    }

    public boolean store(CommandInvoker talker, String key, ConversationTransformer transformer) {
        if (this.bookmarks.containsKey(talker.getClientID())) {
            this.bookmarks.get(talker.getClientID()).put(key, transformer);
            return true;
        }
        return false;
    }

    public ConversationContext getContext(CommandInvoker talker) {
        return this.bookmarks.get(talker.getClientID());
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

    public ConversationTreeNodeResult getAGreeting(ConversationTransformer transformer) {
        if (this.greetings == null || this.greetings.size() == 0) {
            return null;
        }
        ConversationPattern pattern = this.greetings.first().getRegex();
        if (pattern == null) {
            return null;
        }
        TreeSet<ConversationPattern> patterns = new TreeSet<>();
        patterns.add(pattern);
        return ConversationTreeNodeResult.fromString(transformer, pattern.getExample(), null, patterns);
    }

    public void setRepeats(Set<ConversationPattern> repeats) {
        this.repeatWords = new TreeSet<>();
        this.repeatWords.addAll(repeats);
    }

    public String toMermaid(boolean fence) {
        StringBuilder sb = new StringBuilder();
        // GsonBuilder gb = new GsonBuilder();
        // Gson gson = gb.create();
        if (fence) {
            sb.append("```mermaid").append("\r\n");
        }
        sb.append("stateDiagram-v2").append("\r\n");
        for (ConversationTreeNode node : this.nodes.values()) {
            // String json = gson.toJson(node);
            sb.append("    ").append(node.getNodeID().toString().replace("-", "")).append(":")
                    .append(node.getBodySequence().printString(EnumSet.allOf(PrintingInstructions.class)))
                    .append("\r\n");
            if (node.getPrompts().size() > 0) {
                sb.append("    note right of ").append(node.getNodeID().toString().replace("-", "")).append("\r\n");
                for (OutputSequence prompt : node.getPrompts()) {
                    sb.append("        ").append(prompt.printString(EnumSet.allOf(PrintingInstructions.class)))
                            .append("\r\n");
                }
                sb.append("    end note").append("\r\n");
            }

        }

        for (ConversationTreeBranch greetBranch : this.greetings) {
            sb.append("    [*] --> ").append(greetBranch.getNodeID().toString().replace("-", ""));
            sb.append(" : ").append(greetBranch.getRegex().getExample()).append(" ")
                    .append(greetBranch.getRegex().getRegex().toString());

            for (String restriction : greetBranch.getBlacklist().keySet()) {
                sb.append(" ").append(restriction).append(" ")
                        .append(greetBranch.getBlacklist().get(restriction).toString());
            }
            sb.append("\r\n");
        }

        for (UUID source : this.branches.keySet()) {
            for (ConversationTreeBranch branch : this.branches.get(source)) {
                sb.append("    ").append(source.toString().replace("-", "")).append(" --> ")
                        .append(branch.getNodeID().toString().replace("-", ""));
                sb.append(" : ").append(branch.getRegex().toString());

                for (String restriction : branch.getBlacklist().keySet()) {
                    sb.append(" ").append(restriction).append(" ")
                            .append(branch.getBlacklist().get(restriction).toString());
                }
                sb.append("\r\n");
            }
        }

        if (fence) {
            sb.append("```").append("\r\n");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(branches, endOfConvo, greetings, nodes, notRecognized, repeatWords, start, tagkeywords,
                treeName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConversationTree)) {
            return false;
        }
        ConversationTree other = (ConversationTree) obj;
        if (!this.treeName.equals(other.treeName) || !this.endOfConvo.equals(other.endOfConvo)
                || !this.notRecognized.equals(other.notRecognized)) {
            return false;
        }
        if (!this.start.equals(other.start)) {
            return false;
        }
        if (!this.greetings.equals(other.greetings) || !this.repeatWords.equals(other.repeatWords)) {
            return false;
        }
        if (this.nodes.size() != other.nodes.size()) {
            return false;
        }
        if (this.branches.size() != other.branches.size()) {
            return false;
        }
        for (UUID nodeID : this.nodes.keySet()) {
            if (!other.nodes.containsKey(nodeID)) {
                return false;
            }
            if (!this.nodes.get(nodeID).equals(other.nodes.get(nodeID))) {
                return false;
            }
        }
        for (UUID nodeID : this.branches.keySet()) {
            if (!other.branches.containsKey(nodeID)) {
                return false;
            }

            if (!this.branches.get(nodeID).equals(other.branches.get(nodeID))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConversationTree [treename=").append(treeName).append(", branches=").append(branches)
                .append(", endOfConvo=").append(endOfConvo).append(", greetings=").append(greetings).append(", nodes=")
                .append(nodes).append(", notRecognized=").append(notRecognized).append(", repeatWords=")
                .append(repeatWords).append(", start=").append(start).append(", tagkeywords=").append(tagkeywords)
                .append(", bookmarks=").append(bookmarks).append("]");
        return builder.toString();
    }

}
