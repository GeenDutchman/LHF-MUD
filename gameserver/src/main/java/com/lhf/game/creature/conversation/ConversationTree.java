package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.lhf.RichOutput;
import com.lhf.RichOutput.PrintingInstructions;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.Atlas;
import com.lhf.game.AtlasTrawlerBuilder;
import com.lhf.game.Atlas.AtlasException;
import com.lhf.game.Atlas.AtlasMemberException;
import com.lhf.game.Atlas.AtlasTraversalException;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContext;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContextKey;
import com.lhf.server.client.Client.ClientID;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

public class ConversationTree implements Serializable {
    private static class ConversationAtlas
            extends Atlas<ConversationTreeNode, UUID, ConversationPattern, ConversationPredicate> {

        @Override
        public UUID getIDForMemberType(ConversationTreeNode member) {
            return member != null ? member.getNodeID() : null;
        }

        @Override
        public String getNameForMemberType(ConversationTreeNode member) {
            return member != null ? member.getBodyAsString() : null;
        }

        @Override
        public final ConversationPattern translateLinkToOpposite(ConversationPattern link) {
            throw new UnsupportedOperationException(
                    "Default two-way link is unsupported for conversations, specify a concrete reversal in `connectTwoWay`");
        }

    }

    private final String treeName;
    private final ConversationTreeNode start;
    private final ConversationAtlas conversationAtlas;
    private transient Map<ClientID, ConversationContext> bookmarks = new TreeMap<>();
    private final SortedMap<ConversationPattern, ConversationPredicate> greetings;
    private final SortedSet<ConversationPattern> repeatWords;
    private final String endOfConvo;
    private final String notRecognized;
    private final boolean tagkeywords;

    private ConversationTree(@NotNull Builder builder) throws AtlasException {
        this.treeName = builder.getTreeName();
        this.start = builder.getStart().build();
        this.bookmarks = new TreeMap<>();
        this.conversationAtlas = builder.buildNodes();
        this.greetings = Collections.unmodifiableSortedMap(new TreeMap<>(builder.getBuiltGreetings()));
        this.repeatWords = Collections.unmodifiableSortedSet(new TreeSet<>(builder.getRepeatWords()));
        this.endOfConvo = builder.getEndOfConvo();
        this.notRecognized = builder.getNotRecognized();
        this.tagkeywords = builder.isTagkeywords();
    }

    public static class Builder implements Serializable {
        private class BuilderAtlas
                extends Atlas<ConversationTreeNode.Builder, UUID, ConversationPattern, ConversationPredicate> {

            @Override
            public UUID getIDForMemberType(com.lhf.game.creature.conversation.ConversationTreeNode.Builder member) {
                return member != null ? member.getNodeID() : null;
            }

            @Override
            public String getNameForMemberType(com.lhf.game.creature.conversation.ConversationTreeNode.Builder member) {
                return member != null ? member.getBodyAsString() : null;
            }

            @Override
            public final ConversationPattern translateLinkToOpposite(ConversationPattern link) {
                throw new UnsupportedOperationException(
                        "Default two-way link is unsupported for conversations, specify a concrete reversal in `connectTwoWay`");
            }

        }

        private final static String CONVO_END = "Goodbye";
        private final static String UNRECOGNIZED = "What did you say? ...";
        private final ConversationTreeNode.Builder start;
        private String treeName;
        private BuilderAtlas conversationAtlas;
        private SortedMap<ConversationPattern, ConversationPredicate.Builder> greetings;
        private SortedSet<ConversationPattern> repeatWords;
        private String endOfConvo;
        private String notRecognized;
        private boolean tagkeywords;

        public Builder() {
            this(new ConversationTreeNode.Builder());
        }

        public Builder(ConversationTreeNode.Builder nodeBuilder) {
            this.treeName = UUID.randomUUID().toString();
            this.conversationAtlas = new BuilderAtlas();
            this.start = nodeBuilder != null ? nodeBuilder : new ConversationTreeNode.Builder();
            this.conversationAtlas.addMember(this.start);
            this.addDefaultGreetings();
            this.addDefaultRepeatWords();
            this.endOfConvo = CONVO_END;
            this.notRecognized = UNRECOGNIZED;
            this.tagkeywords = true;
        }

        public Builder(String starting) {
            this(ConversationTreeNode.Builder.ofString(starting));
        }

        public ConversationTree build() throws AtlasException {
            return new ConversationTree(this);
        }

        public static Builder fromTree(ConversationTree tree) {
            Builder builder = new Builder(new ConversationTreeNode.Builder(tree != null ? tree.start : null));
            if (tree == null) {
                return builder;
            }
            builder.setTreeName(tree.getTreeName()).setEndOfConvo(tree.getEndOfConvo())
                    .setNotRecognized(tree.getNotRecognized()).setGreetings(tree.greetings)
                    .setRepeatWords(tree.repeatWords).setTagkeywords(tree.tagkeywords);
            try {
                tree.conversationAtlas.translate(builder.conversationAtlas,
                        node -> new ConversationTreeNode.Builder(node), Function.identity(),
                        ConversationPredicate::copyFrom);
            } catch (AtlasException e) {
                // wrap it and send it on
                throw new IllegalStateException(String.format("Error reverting tree '%s' to builder", tree), e);
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
            return this.conversationAtlas.getAtlasMemberOrNull(builderID);
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
                ConversationTreeNode.Builder nextNode, Consumer<ConversationPredicate.Builder> branchEditor)
                throws AtlasMemberException {
            if (nextNode == null) {
                return this;
            }
            if (fromNodeBuilder == null) {
                fromNodeBuilder = this.start.getNodeID();
            }
            ConversationTreeNode.Builder from = this.conversationAtlas.getAtlasMemberOrNull(fromNodeBuilder);

            ConversationPredicate.Builder predicate = new ConversationPredicate.Builder();
            if (branchEditor != null) {
                branchEditor.accept(predicate);
            }
            this.conversationAtlas.connectOneWay(from, pathToNode, nextNode, predicate.build());

            return this;
        }

        public Builder addNode(UUID fromNodeBuilder, ConversationPattern pathToNode,
                ConversationTreeNode.Builder nextNode) throws AtlasMemberException {
            return this.addNode(fromNodeBuilder, pathToNode, nextNode, null);
        }

        public Builder addNode(UUID fromNodeBuilder, ConversationPattern pathToNode, String nextNode,
                Consumer<ConversationPredicate.Builder> branchEditor) throws AtlasMemberException {
            if (nextNode == null) {
                return this;
            }
            return this.addNode(fromNodeBuilder, pathToNode, ConversationTreeNode.Builder.ofString(nextNode),
                    branchEditor);
        }

        public Builder addNode(UUID fromNodeBuilder, ConversationPattern pathToNode, String nextNode)
                throws AtlasMemberException {
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
            this.conversationAtlas.removeMemberAndChildren(idToRemove);
            if (this.start.getNodeID().equals(idToRemove)) {
                this.start.setBodySequence(null).setPrompts(null);
                this.conversationAtlas.addMember(this.start);
            }

            return this;
        }

        public synchronized ConversationPattern getBranchPattern(UUID fromHere, UUID toThere) {
            if (toThere == null) {
                return null;
            }
            if (fromHere == null) {
                fromHere = this.start.getNodeID();
            }
            return this.conversationAtlas.getLinkTypeBetweenOrNull(fromHere, toThere);
        }

        public synchronized ConversationPredicate getBranchPredicate(UUID fromHere, UUID toThere) {
            if (toThere == null) {
                return null;
            }
            if (fromHere == null) {
                fromHere = this.start.getNodeID();
            }
            return this.conversationAtlas.getTraversalBetweenOrNull(fromHere, toThere);
        }

        public Builder editBranch(UUID fromHere, UUID toThere, Consumer<ConversationPredicate> branchEditor) {
            ConversationPredicate branch = this.getBranchPredicate(fromHere, toThere);
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
                this.greetings = new TreeMap<>();
            }
            this.addGreeting(new ConversationPattern("hello", "^\\s*hello\\b", Pattern.CASE_INSENSITIVE));
            this.addGreeting(new ConversationPattern("hi", "^\\s*hi\\b", Pattern.CASE_INSENSITIVE));
            return this;
        }

        public Builder addGreeting(ConversationPattern regex) {
            if (this.greetings == null) {
                this.greetings = new TreeMap<>();
            }
            this.greetings.put(regex, new ConversationPredicate.Builder());
            return this;
        }

        public Builder clearGreetings() {
            if (this.greetings != null) {
                this.greetings.clear();
            }
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

        public Set<ConversationTreeNode.Builder> getNodes() {
            return this.conversationAtlas.getAtlasMembers();
        }

        public Builder trawlBuildTree(
                Consumer<AtlasTrawlerBuilder<ConversationTreeNode.Builder, UUID, ConversationPattern, ConversationPredicate>> trawlerConsumer) {
            if (this.conversationAtlas != null && trawlerConsumer != null) {
                AtlasTrawlerBuilder<ConversationTreeNode.Builder, UUID, ConversationPattern, ConversationPredicate> trawler = this.conversationAtlas
                        .getTrawlerBuilder();
                if (trawler != null) {
                    trawlerConsumer.accept(trawler);
                }
            }
            return this;
        }

        public ConversationAtlas buildNodes() throws AtlasException {
            ConversationAtlas translated = new ConversationAtlas();
            this.conversationAtlas.translate(translated, builder -> builder.build(), pattern -> pattern,
                    predicate -> ConversationPredicate.copyFrom(predicate));
            return translated;
        }

        public SortedMap<ConversationPattern, ConversationPredicate.Builder> getGreetings() {
            return greetings;
        }

        public SortedMap<ConversationPattern, ConversationPredicate> getBuiltGreetings() {
            return greetings.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(),
                    entry -> entry.getValue().build(), (a, b) -> b, () -> new TreeMap<>()));
        }

        public Builder setGreetings(SortedMap<ConversationPattern, ConversationPredicate> freshGreetings) {
            this.greetings = freshGreetings != null
                    ? freshGreetings.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(),
                            entry -> ConversationPredicate.getBuilder().addRules(entry.getValue().getBlacklist()),
                            (a, b) -> b, () -> new TreeMap<ConversationPattern, ConversationPredicate.Builder>()))
                    : new TreeMap<>();
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

        public String toMermaidStateDiagram(boolean fence) {
            final String spacing = "    ";
            String mermaid = this.conversationAtlas.toStateDiagramMermaid(spacing, fence, true, uuid -> uuid.toString(),
                    pattern -> pattern != null ? pattern.getRegex().toString() : "linked", predicate -> {
                        if (predicate == null) {
                            return "";
                        }
                        StringBuilder branchBuilder = new StringBuilder();
                        for (Entry<String, ConversationPattern> restriction : predicate.getBlacklist().entrySet()) {
                            branchBuilder.append(" ").append(restriction.getKey()).append(" ")
                                    .append(restriction.getValue().getRegex().toString());
                        }
                        return branchBuilder.toString();
                    }, builder -> {
                        if (builder == null) {
                            return "";
                        }
                        StringBuilder sb = new StringBuilder();
                        for (RichOutputBuilder prompt : builder.getPrompts()) {
                            sb.append(prompt.printString(EnumSet.allOf(PrintingInstructions.class))).append("\r\n");
                        }
                        return sb.toString();
                    });

            StringBuilder sb = new StringBuilder();

            final String startID = this.start.getNodeID().toString().replace("-", "");
            for (Entry<ConversationPattern, ConversationPredicate.Builder> greetBranch : this.greetings.entrySet()) {
                sb.append(spacing).append("[*] --> ").append(startID);
                sb.append(" : ").append(greetBranch.getKey().getExample()).append(" ")
                        .append(greetBranch.getKey().getRegex().toString());
                final ConversationPredicate.Builder predicate = greetBranch.getValue();
                if (predicate != null) {
                    for (Entry<String, ConversationPattern> restriction : predicate.getBlacklist().entrySet()) {
                        sb.append(" ").append(restriction.getKey()).append(" ")
                                .append(restriction.getValue().getRegex().toString());
                    }
                }
                sb.append("\r\n");
            }

            mermaid = mermaid.replace(spacing + "[*] --> " + startID, sb.toString());
            return mermaid;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Builder [start=").append(start).append(", treeName=").append(treeName)
                    .append(", conversationAtlas=").append(conversationAtlas).append(", greetings=").append(greetings)
                    .append(", repeatWords=").append(repeatWords).append(", endOfConvo=").append(endOfConvo)
                    .append(", notRecognized=").append(notRecognized).append(", tagkeywords=").append(tagkeywords)
                    .append("]");
            return builder.toString();
        }

    }

    protected ConversationTree initBookmarks() {
        if (this.bookmarks == null) {
            this.bookmarks = new TreeMap<>();
        }
        this.bookmarks.clear();
        return this;
    }

    public String getTreeName() {
        return treeName;
    }

    private ConversationTreeNodeResult tagIt(ConversationContext ctx, ConversationTreeNode node) {
        if (node == null) {
            return null;
        }
        TreeSet<ConversationPattern> branches = new TreeSet<>();
        Set<ConversationPattern> patterns = this.conversationAtlas.getLinksForMember(node.getNodeID());
        if (patterns != null && this.tagkeywords) {
            for (ConversationPattern branch : patterns) {
                if (branch == null) {
                    continue;
                }
                final SortedSet<ConversationPredicate> predicates = this.conversationAtlas
                        .getTraversalTestsFromMember(node.getNodeID(), branch);
                if (predicates != null) {
                    predicates.stream().filter(predicate -> predicate.canAccess(ctx))
                            .forEach(predicate -> branches.add(branch));
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
        return this.tagIt(ctx, this.conversationAtlas.getAtlasMemberOrNull(backNode));
    }

    protected ConversationTreeNode getNode(UUID nodeID) {
        return this.conversationAtlas.getAtlasMemberOrNull(nodeID);
    }

    protected Map<UUID, ConversationTreeNode> getNodes() {
        return this.conversationAtlas.getAtlasMap();
    }

    protected ConversationTreeNode getCurrentNode(CommandInvoker talker) {
        ConversationContext ctx = this.bookmarks.get(talker.getClientID());
        UUID nodeID = ctx.getTrailEnd();
        return this.getNode(nodeID);
    }

    public ConversationTreeNodeResult listen(CommandInvoker talker, String message) {
        if (!this.bookmarks.containsKey(talker.getClientID())) {
            for (Entry<ConversationPattern, ConversationPredicate> greet : this.greetings.entrySet()) {
                ConversationPattern pattern = greet.getKey();
                if (pattern == null) {
                    continue;
                }
                Matcher matcher = pattern.getRegex().matcher(message);
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
        ConversationTreeNode nextNode = null;
        try {
            nextNode = this.conversationAtlas.attemptAllTraversals(id, pattern -> {
                if (pattern == null) {
                    return false;
                }
                Matcher matcher = pattern.getRegex().matcher(message);
                return matcher.find();
            }, (traversalTester, source, link, destination) -> {
                return traversalTester != null ? traversalTester.canAccess(ctx) : true;
            }, true);
        } catch (AtlasTraversalException e) {
            // do nothing, we'll try other things
        }
        if (nextNode != null) {
            this.bookmarks.get(talker.getClientID()).addTrail(nextNode.getNodeID());
            return this.tagIt(ctx, nextNode);
        }

        for (ConversationPattern repeater : this.repeatWords) {
            Matcher matcher = repeater.matcher(message);
            if (matcher.find() && this.conversationAtlas.getAtlasMemberOrNull(id) != null) {
                return this.tagIt(ctx, this.conversationAtlas.getAtlasMemberOrNull(id));
            }
        }

        Set<ConversationPattern> links = this.conversationAtlas.getLinksForMember(id);
        if (links == null || links.size() <= 0) {
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
        ConversationPattern pattern = this.greetings.keySet().stream().findFirst().orElse(null);
        if (pattern == null) {
            return null;
        }
        TreeSet<ConversationPattern> patterns = new TreeSet<>();
        patterns.add(pattern);
        return ConversationTreeNodeResult.fromString(transformer, pattern.getExample(), null, patterns);
    }

    public String toMermaidStateDiagram(boolean fence) {
        final String spacing = "    ";
        String mermaid = this.conversationAtlas.toStateDiagramMermaid(spacing, fence, true, uuid -> uuid.toString(),
                pattern -> pattern != null ? pattern.getRegex().toString() : "linked", predicate -> {
                    if (predicate == null) {
                        return "";
                    }
                    StringBuilder branchBuilder = new StringBuilder();
                    for (Entry<String, ConversationPattern> restriction : predicate.getBlacklist().entrySet()) {
                        branchBuilder.append(" ").append(restriction.getKey()).append(" ")
                                .append(restriction.getValue().getRegex().toString());
                    }
                    return branchBuilder.toString();
                }, node -> {
                    if (node == null) {
                        return "";
                    }
                    StringBuilder sb = new StringBuilder();
                    for (RichOutput prompt : node.getPrompts()) {
                        sb.append(prompt.printString(EnumSet.allOf(PrintingInstructions.class))).append("\r\n");
                    }
                    return sb.toString();
                });

        StringBuilder sb = new StringBuilder();

        final String startID = this.start.getNodeID().toString().replace("-", "");
        for (Entry<ConversationPattern, ConversationPredicate> greetBranch : this.greetings.entrySet()) {
            sb.append(spacing).append("[*] --> ").append(startID);
            sb.append(" : ").append(greetBranch.getKey().getExample()).append(" ")
                    .append(greetBranch.getKey().getRegex().toString());
            final ConversationPredicate predicate = greetBranch.getValue();
            if (predicate != null) {
                for (Entry<String, ConversationPattern> restriction : predicate.getBlacklist().entrySet()) {
                    sb.append(" ").append(restriction.getKey()).append(" ")
                            .append(restriction.getValue().getRegex().toString());
                }
            }
            sb.append("\r\n");
        }

        mermaid = mermaid.replace(spacing + "[*] --> " + startID, sb.toString());
        return mermaid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(treeName, start, conversationAtlas, greetings, repeatWords, endOfConvo, notRecognized,
                tagkeywords);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ConversationTree))
            return false;
        ConversationTree other = (ConversationTree) obj;
        return Objects.equals(treeName, other.treeName) && Objects.equals(start, other.start)
                && Objects.equals(conversationAtlas, other.conversationAtlas)
                && Objects.equals(greetings, other.greetings) && Objects.equals(repeatWords, other.repeatWords)
                && Objects.equals(endOfConvo, other.endOfConvo) && Objects.equals(notRecognized, other.notRecognized)
                && tagkeywords == other.tagkeywords;
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("ConversationTree [treeName=").append(treeName).append(", start=").append(start)
                .append(", conversationAtlas=").append(conversationAtlas).append(", greetings=").append(greetings)
                .append(", repeatWords=").append(repeatWords).append(", endOfConvo=").append(endOfConvo)
                .append(", notRecognized=").append(notRecognized).append(", tagkeywords=").append(tagkeywords)
                .append(", bookmarks=").append(bookmarks).append("]");
        return builder2.toString();
    }

}
