package com.lhf.game;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import com.lhf.game.Atlas.AtlasException;
import com.lhf.game.Atlas.AtlasFunction;
import com.lhf.game.Atlas.AtlasMemberException;
import com.lhf.game.Atlas.AtlasTraversalException;
import com.lhf.game.Atlas.ContextualTraversalPredicate;

public class AtlasTrawlerBuilder<Member, ID extends Comparable<ID>, Link extends Comparable<Link>, Traversal extends Comparable<Traversal>> {
    private Member currentNode;
    private final Deque<Member> trace = new LinkedList<>();
    private final Atlas<Member, ID, Link, Traversal> atlas;

    public static <M, I extends Comparable<I>, L extends Comparable<L>, T extends Comparable<T>> AtlasTrawlerBuilder<M, I, L, T> trawl(
            Atlas<M, I, L, T> myatlas) {
        if (myatlas == null) {
            throw new NullPointerException("Provided atlas must not be null!");
        }
        return new AtlasTrawlerBuilder<>(myatlas);
    }

    private AtlasTrawlerBuilder(Atlas<Member, ID, Link, Traversal> myatlas) {
        this.currentNode = myatlas.getFirstMember();
        this.atlas = myatlas;
    }

    public void checkInitialized() throws IllegalStateException {
        if (this.atlas == null) {
            throw new NullPointerException("Atlas is null");
        }
        Member root = this.atlas.getFirstMember();
        if (root == null) {
            throw new IllegalStateException("Atlas does not have a starting node, try 'plainAddMember'");
        }
        if (this.currentNode == null) {
            this.currentNode = this.trace.peekLast();
        }
        if (this.currentNode == null) {
            this.currentNode = root;
        }
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> back() {
        this.checkInitialized();
        this.currentNode = this.trace.pollLast();
        if (this.currentNode == null) {
            this.currentNode = this.atlas.getFirstMember();
        }
        return this;
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> jumpToRoot() {
        this.checkInitialized();
        this.trace.clear();
        this.currentNode = this.atlas.getFirstMember();
        return this;
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> jumpToID(ID id) throws AtlasMemberException {
        this.checkInitialized();
        Member temp = this.currentNode;
        this.currentNode = this.atlas.getAtlasMemberOrThrow(id);
        this.trace.addLast(temp);
        return this;
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> jumpToMember(Member member) throws AtlasMemberException {
        this.checkInitialized();
        Member temp = this.currentNode;
        this.currentNode = this.atlas.getAtlasMemberOrThrow(this.atlas.getIDForMemberType(member));
        this.trace.addLast(temp);
        return this;
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> addMemberOneWay(Link link, Traversal traversal,
            Member nextMember) throws IllegalArgumentException, AtlasMemberException {
        this.checkInitialized();
        this.atlas.connectOneWay(this.currentNode, link, nextMember, traversal);
        this.trace.addLast(this.currentNode);
        this.currentNode = nextMember;
        return this;
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> addOnewayToExistingMember(Link link, Traversal traversal,
            ID existant) throws AtlasMemberException {
        this.checkInitialized();
        Member destination = this.atlas.getAtlasMemberOrThrow(existant);
        return this.addMemberOneWay(link, traversal, destination);
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> addMemberTwoWay(Link link, Traversal traversal,
            Member nextMember) throws IllegalArgumentException, AtlasException {
        this.checkInitialized();
        this.atlas.connect(this.currentNode, link, nextMember, traversal);
        this.trace.addLast(this.currentNode);
        this.currentNode = nextMember;
        return this;
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> addTwoWayToExistingMember(Link link, Traversal traversal,
            ID existant) throws AtlasException {
        this.checkInitialized();
        Member destination = this.atlas.getAtlasMemberOrThrow(existant);
        return this.addMemberTwoWay(link, traversal, destination);
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> addMemberTwoWay(Link link, Traversal traversal,
            Member nextMember, AtlasFunction<Link, Link> linkReverser,
            AtlasFunction<Traversal, Traversal> traversalReverser) throws IllegalArgumentException, AtlasException {
        this.checkInitialized();
        this.atlas.connectTwoWay(this.currentNode, link, nextMember, traversal, linkReverser, traversalReverser);
        this.trace.addLast(this.currentNode);
        this.currentNode = nextMember;
        return this;
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> addTwoWayToExistingMember(Link link, Traversal traversal,
            ID existant, AtlasFunction<Link, Link> linkReverser, AtlasFunction<Traversal, Traversal> traversalReverser)
            throws IllegalArgumentException, IllegalStateException, AtlasException {
        this.checkInitialized();
        Member destination = this.atlas.getAtlasMemberOrThrow(existant);
        return this.addMemberTwoWay(link, traversal, destination, linkReverser, traversalReverser);
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> plainAddMember(Member member) {
        if (this.atlas == null) {
            throw new NullPointerException("Atlas is null");
        }
        Member root = this.atlas.getFirstMember();
        if (root != null) {
            throw new IllegalStateException(
                    "Atlas already has a starting node, try 'addMemberOneWay' or 'addMemberTwoWay'");
        }
        this.atlas.addMember(member);
        this.currentNode = member;
        return this;
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> traverse(Link throughLink,
            ContextualTraversalPredicate<Member, Link, Traversal> traversalPredicate) throws AtlasTraversalException {
        this.checkInitialized();
        Member temp = this.currentNode;
        this.currentNode = this.atlas.attemptTraversal(this.atlas.getIDForMemberType(this.currentNode), throughLink,
                traversalPredicate);
        this.trace.addLast(temp);
        return this;
    }

    public AtlasTrawlerBuilder<Member, ID, Link, Traversal> traverseAny(Predicate<Link> throughLink,
            ContextualTraversalPredicate<Member, Link, Traversal> traversalPredicate) throws AtlasTraversalException {
        this.checkInitialized();
        Member temp = this.currentNode;
        this.currentNode = this.atlas.attemptAllTraversals(this.atlas.getIDForMemberType(this.currentNode), throughLink,
                traversalPredicate, true);
        this.trace.addLast(temp);
        return this;
    }

    public final String getStateName() {
        return this.getClass().getName();
    }

    public ID getMemberID() {
        if (currentNode == null || this.atlas == null) {
            return null;
        }
        return this.atlas.getIDForMemberType(currentNode);
    }

    public Set<Link> getLinksForMember() {
        if (currentNode == null || this.atlas == null) {
            return Set.of();
        }
        return this.atlas.getLinksForMember(atlas.getIDForMemberType(currentNode));
    }

    public SortedSet<Traversal> getTraversalsFromMember(Link through) {
        if (currentNode == null || this.atlas == null) {
            return Collections.unmodifiableSortedSet(new TreeSet<>());
        }
        return this.atlas.getTraversalTestsFromMember(atlas.getIDForMemberType(currentNode), through);
    }

    public final Member getMember() {
        return this.currentNode;
    }

    public Atlas<Member, ID, Link, Traversal> getAtlas() {
        return this.atlas;
    }

    public int size() {
        return this.atlas.size();
    }

    public final String toStateDiagramMermaid(String indent, boolean fence, boolean includeStart) {
        if (this.atlas == null) {
            return "null";
        }
        Atlas<Member, ID, Link, Traversal>.AtlasToMermaidWriter writer = this.atlas.generateMermaidWriter(indent);
        if (writer == null) {
            writer = this.atlas.new AtlasToMermaidWriter(indent) {

                @Override
                protected String displayID(ID id) {
                    return id != null ? id.toString() : "null";
                }

                @Override
                protected String displayLink(Link link) {
                    return link != null ? link.toString() : "null";
                }

                @Override
                protected String displayTraversalTest(Traversal traversal) {
                    return traversal != null ? traversal.toString() : "null";
                }

                @Override
                protected String displayMemberNote(Member member) {
                    return AtlasTrawlerBuilder.this.atlas.getNameForMemberType(member);
                }

            };
        }
        return writer.printStateDiagram(fence, includeStart, true);
    }

    public final String printMermaid() {
        return this.toStateDiagramMermaid("    ", false, true);
    }
}
