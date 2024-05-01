package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lhf.RichOutput;
import com.lhf.RichOutput.PrintingInstructions;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContext;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContextKey;
import com.lhf.server.client.Client.ClientID;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

public class ConversationTree implements Serializable {
    private final String treeName;
    private final ConversationTreeNode start;
    // TODO: wait for `SequencedCollection` from Java21
    private final Map<UUID, ConversationTreeNode> nodes;
    private final Map<UUID, List<ConversationTreeBranch>> branches;
    private final transient Map<ClientID, ConversationContext> bookmarks;
    private final SortedSet<ConversationTreeBranch> greetings;
    private final SortedSet<ConversationPattern> repeatWords;
    private final String endOfConvo;
    private final String notRecognized;
    private final boolean tagkeywords;

    private ConversationTree(@NotNull Builder builder) {
        this.treeName = builder.getTreeName();
        this.start = builder.getStart().build();
        this.nodes = Collections.unmodifiableMap(builder.buildNodes());
        this.branches = Collections.unmodifiableMap(new LinkedHashMap<>(builder.getBranches()));
        this.bookmarks = new TreeMap<>();
        this.greetings = Collections.unmodifiableSortedSet(new TreeSet<>(builder.getGreetings()));
        this.repeatWords = Collections.unmodifiableSortedSet(new TreeSet<>(builder.getRepeatWords()));
        this.endOfConvo = builder.getEndOfConvo();
        this.notRecognized = builder.getNotRecognized();
        this.tagkeywords = builder.isTagkeywords();
    }

    public static class Builder implements Serializable {
        private final static String CONVO_END = "Goodbye";
        private final static String UNRECOGNIZED = "What did you say? ...";
        private final ConversationTreeNode.Builder start;
        private String treeName;
        private LinkedHashMap<UUID, ConversationTreeNode.Builder> nodes;
        private LinkedHashMap<UUID, List<ConversationTreeBranch>> branches;
        private SortedSet<ConversationTreeBranch> greetings;
        private SortedSet<ConversationPattern> repeatWords;
        private String endOfConvo;
        private String notRecognized;
        private boolean tagkeywords;

        public Builder() {
            this.treeName = UUID.randomUUID().toString();
            this.nodes = new LinkedHashMap<>();
            this.branches = new LinkedHashMap<>();
            this.start = new ConversationTreeNode.Builder();
            this.nodes.put(start.getNodeID(), start);
            this.addDefaultGreetings();
            this.addDefaultRepeatWords();
            this.endOfConvo = CONVO_END;
            this.notRecognized = UNRECOGNIZED;
            this.tagkeywords = true;
        }

        public Builder(String starting) {
            this.treeName = UUID.randomUUID().toString();
            this.nodes = new LinkedHashMap<>();
            this.branches = new LinkedHashMap<>();
            this.start = new ConversationTreeNode.Builder();
            this.nodes.put(start.getNodeID(), start);
            this.setStartBody(starting);
            this.addDefaultGreetings();
            this.addDefaultRepeatWords();
            this.endOfConvo = CONVO_END;
            this.notRecognized = UNRECOGNIZED;
            this.tagkeywords = true;
        }

        public ConversationTree build() {
            return new ConversationTree(this);
        }

        public static Builder fromTree(ConversationTree tree) {
            Builder builder = new Builder();
            if (tree == null) {
                return builder;
            }
            builder.setTreeName(tree.getTreeName())
                    .editStartNode(node -> node.getBodySequence().appendRichOutput(tree.start.getBodySequence()))
                    .setEndOfConvo(tree.getEndOfConvo()).setNotRecognized(tree.getNotRecognized())
                    .setGreetings(tree.greetings).setRepeatWords(tree.repeatWords).setTagkeywords(tree.tagkeywords);
            for (ConversationTreeNode node : tree.nodes.values()) {
                if (node == null) {
                    continue;
                }
                builder.nodes.put(node.getNodeID(), new ConversationTreeNode.Builder(node));
            }
            for (Entry<UUID, List<ConversationTreeBranch>> branch : tree.branches.entrySet()) {
                if (branch == null) {
                    continue;
                }
                List<ConversationTreeBranch> value = branch.getValue();
                UUID key = branch.getKey();
                if (key == null || value == null) {
                    continue;
                }
                List<ConversationTreeBranch> copies = new ArrayList<>();
                for (ConversationTreeBranch valueBranch : value) {
                    if (valueBranch != null) {
                        ConversationTreeBranch newBranch = new ConversationTreeBranch(valueBranch.getRegex(),
                                valueBranch.getNodeID());
                        copies.add(newBranch);
                        valueBranch.getBlacklist().entrySet().stream()
                                .forEach(entry -> newBranch.addRule(entry.getKey(), entry.getValue()));
                    }
                }
                builder.branches.put(key, copies);
            }
            return builder;
        }

        public ConversationTreeNode.Builder getStart() {
            return start;
        }

        public Builder setStartBody(RichOutputBuilder body) {
            this.start.setBodySequence(body);
            return this;
        }

        public Builder setStartBody(String body) {
            this.start.setBodySequence(
                    new RichOutputBuilder(ConversationTreeNode.NPC_CONVERSATION_TAG).appendString(body, null, null));
            return this;
        }

        public Builder editStartNode(Consumer<ConversationTreeNode.Builder> nodeModifier) {
            if (nodeModifier != null) {
                nodeModifier.accept(this.start);
            }
            return this;
        }

        public synchronized ConversationTreeNode.Builder getNodeBuilder(UUID builderID) {
            if (builderID == null) {
                return null;
            }
            return this.nodes.get(builderID);
        }

        public Builder editNode(Supplier<UUID> idSupplier, Consumer<ConversationTreeNode.Builder> nodeModifier) {
            if (idSupplier == null) {
                return this;
            }
            UUID id = idSupplier.get();
            ConversationTreeNode.Builder nodeBuilder = this.getNodeBuilder(id);
            if (nodeBuilder == null || nodeModifier == null) {
                return this;
            }
            nodeModifier.accept(nodeBuilder);
            return this;
        }

        public Builder addNode(UUID fromNodeBuilder, ConversationPattern pathToNode,
                ConversationTreeNode.Builder nextNode, Consumer<ConversationTreeBranch> branchEditor) {
            if (nextNode == null) {
                return this;
            }
            if (fromNodeBuilder == null) {
                fromNodeBuilder = this.start.getNodeID();
            }
            if (!this.branches.containsKey(fromNodeBuilder)) {
                this.branches.put(fromNodeBuilder, new ArrayList<>());
            }
            ConversationTreeBranch branch = new ConversationTreeBranch(pathToNode, nextNode.getNodeID());
            if (branchEditor != null) {
                branchEditor.accept(branch);
            }
            this.branches.get(fromNodeBuilder).add(branch);
            this.nodes.put(nextNode.getNodeID(), nextNode);
            return this;
        }

        public Builder addNode(UUID fromNodeBuilder, ConversationPattern pathToNode,
                ConversationTreeNode.Builder nextNode) {
            return this.addNode(fromNodeBuilder, pathToNode, nextNode, null);
        }

        public Builder addNode(UUID fromNodeBuilder, ConversationPattern pathToNode, String nextNode,
                Consumer<ConversationTreeBranch> branchEditor) {
            if (nextNode == null) {
                return this;
            }
            return this.addNode(fromNodeBuilder, pathToNode, ConversationTreeNode.Builder.ofString(nextNode),
                    branchEditor);
        }

        public Builder addNode(UUID fromNodeBuilder, ConversationPattern pathToNode, String nextNode) {
            return this.addNode(fromNodeBuilder, pathToNode, nextNode, null);
        }

        /**
         * Removes the node identified (if it exists) and any child nodes
         * 
         * @param idToRemove
         * @return
         */
        public synchronized Builder removeNode(UUID idToRemove) {
            ConversationTreeNode.Builder target = this.getNodeBuilder(idToRemove);
            if (target == null) {
                return this;
            }
            if (this.start.getNodeID().equals(idToRemove)) {
                this.start.setBodySequence(null).setPrompts(null);
            } else {
                this.nodes.remove(idToRemove);
            }
            List<ConversationTreeBranch> targetBranches = this.branches.remove(idToRemove);
            if (targetBranches == null) {
                return this;
            }
            for (ConversationTreeBranch branch : targetBranches) {
                if (branch != null) {
                    this.removeNode(branch.getNodeID()); // recursive call
                }
            }
            return this;
        }

        public synchronized ConversationTreeBranch getBranch(UUID fromHere, UUID toThere) {
            if (toThere == null) {
                return null;
            }
            if (fromHere == null) {
                fromHere = this.start.getNodeID();
            }
            List<ConversationTreeBranch> branchList = this.branches.get(fromHere);
            if (branchList == null) {
                return null;
            }
            for (ConversationTreeBranch branch : branchList) {
                if (branch != null && toThere.equals(branch.getNodeID())) {
                    return branch;
                }
            }
            return null;
        }

        public Builder editBranch(UUID fromHere, UUID toThere, Consumer<ConversationTreeBranch> branchEditor) {
            ConversationTreeBranch branch = this.getBranch(fromHere, toThere);
            if (branch != null && branchEditor != null) {
                branchEditor.accept(branch);
            }
            return this;
        }

        protected Builder addDefaultRepeatWords() {
            if (this.repeatWords == null) {
                this.repeatWords = new TreeSet<>();
            }
            this.repeatWords.add(new ConversationPattern("again", "\\bagain\\b", Pattern.CASE_INSENSITIVE));
            this.repeatWords.add(new ConversationPattern("repeat", "\\brepeat\\b", Pattern.CASE_INSENSITIVE));
            return this;
        }

        protected Builder addDefaultGreetings() {
            if (this.greetings == null) {
                this.greetings = new TreeSet<>();
            }
            this.addGreeting(new ConversationPattern("hello", "^\\s*hello\\b", Pattern.CASE_INSENSITIVE));
            this.addGreeting(new ConversationPattern("hi", "^\\s*hi\\b", Pattern.CASE_INSENSITIVE));
            return this;
        }

        public Builder addGreeting(ConversationPattern regex) {
            if (this.greetings == null) {
                this.greetings = new TreeSet<>();
            }
            this.greetings.add(new ConversationTreeBranch(regex, this.start.getNodeID()));
            return this;
        }

        public synchronized String getTreeName() {
            if (treeName == null) {
                this.treeName = UUID.randomUUID().toString();
            }
            return treeName;
        }

        public Builder setTreeName(String treeName) {
            this.treeName = treeName != null ? treeName : UUID.randomUUID().toString();
            return this;
        }

        public LinkedHashMap<UUID, ConversationTreeNode.Builder> getNodes() {
            return nodes;
        }

        public LinkedHashMap<UUID, ConversationTreeNode> buildNodes() {
            LinkedHashMap<UUID, ConversationTreeNode> built = new LinkedHashMap<>();
            for (ConversationTreeNode.Builder node : this.nodes.values()) {
                built.put(node.getNodeID(), node.build());
            }
            return built;
        }

        public LinkedHashMap<UUID, List<ConversationTreeBranch>> getBranches() {
            return branches;
        }

        public SortedSet<ConversationTreeBranch> getGreetings() {
            return greetings;
        }

        public Builder setGreetings(SortedSet<ConversationTreeBranch> greetings) {
            this.greetings = greetings != null ? new TreeSet<>(greetings) : new TreeSet<>();
            return this;
        }

        public SortedSet<ConversationPattern> getRepeatWords() {
            return repeatWords;
        }

        public Builder setRepeatWords(SortedSet<ConversationPattern> repeatWords) {
            this.repeatWords = repeatWords != null ? new TreeSet<>(repeatWords) : new TreeSet<>();
            return this;
        }

        public String getEndOfConvo() {
            return endOfConvo != null ? endOfConvo : CONVO_END;
        }

        public Builder setEndOfConvo(String endOfConvo) {
            this.endOfConvo = endOfConvo;
            return this;
        }

        public String getNotRecognized() {
            return notRecognized != null ? notRecognized : UNRECOGNIZED;
        }

        public Builder setNotRecognized(String notRecognized) {
            this.notRecognized = notRecognized;
            return this;
        }

        public boolean isTagkeywords() {
            return tagkeywords;
        }

        public Builder setTagkeywords(boolean tagkeywords) {
            this.tagkeywords = tagkeywords;
            return this;
        }

        public String toMermaid(boolean fence) {
            StringBuilder sb = new StringBuilder();
            // GsonBuilder gb = new GsonBuilder();
            // Gson gson = gb.create();
            if (fence) {
                sb.append("```mermaid").append("\r\n");
            }
            sb.append("stateDiagram-v2").append("\r\n");
            for (ConversationTreeNode.Builder node : this.nodes.values()) {
                // String json = gson.toJson(node);
                sb.append("    ").append(node.getNodeID().toString().replace("-", "")).append(":")
                        .append(node.getBodySequence().printString(EnumSet.allOf(PrintingInstructions.class)))
                        .append("\r\n");
                if (node.getPrompts().size() > 0) {
                    sb.append("    note right of ").append(node.getNodeID().toString().replace("-", "")).append("\r\n");
                    for (RichOutputBuilder prompt : node.getPrompts()) {
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
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Builder [start=").append(start).append(", treeName=").append(treeName).append(", nodes=")
                    .append(nodes).append(", branches=").append(branches).append(", greetings=").append(greetings)
                    .append(", repeatWords=").append(repeatWords).append(", endOfConvo=").append(endOfConvo)
                    .append(", notRecognized=").append(notRecognized).append(", tagkeywords=").append(tagkeywords)
                    .append("]");
            return builder.toString();
        }

    }

    protected ConversationTree initBookmarks() {
        this.bookmarks.clear();
        return this;
    }

    public String getTreeName() {
        return treeName;
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

    public String getNotRecognized() {
        return notRecognized;
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
                for (RichOutput prompt : node.getPrompts()) {
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
