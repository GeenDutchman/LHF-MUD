package com.lhf.game;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class Atlas<AtlasMemberType, AtlasMemberID extends Comparable<AtlasMemberID>, AtlasLinkType extends Comparable<AtlasLinkType>, AtlasTraversalTestType> {
    public static final class TargetedTester<TargetLinkType extends Comparable<TargetLinkType>, TargetIDType extends Comparable<TargetIDType>, TargetTraversalTestType>
            implements Serializable {
        private final TargetLinkType link;
        private final TargetIDType targetId;
        private final TargetTraversalTestType predicate;

        protected TargetedTester(TargetLinkType link, TargetIDType targetId, TargetTraversalTestType predicate) {
            this.link = link;
            this.targetId = targetId;
            this.predicate = predicate;
        }

        public TargetLinkType getLink() {
            return link;
        }

        public TargetIDType getTargetId() {
            return targetId;
        }

        public TargetTraversalTestType getPredicate() {
            return predicate;
        }

        @Override
        public int hashCode() {
            return Objects.hash(link, targetId, predicate);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof TargetedTester))
                return false;
            TargetedTester<?, ?, ?> other = (TargetedTester<?, ?, ?>) obj;
            return link == other.link && Objects.equals(targetId, other.targetId)
                    && Objects.equals(predicate, other.predicate);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("TargetedTester [link=").append(link).append(", targetId=").append(targetId)
                    .append(", predicate=").append(predicate).append("]");
            return builder.toString();
        }

    }

    public final static class AtlasMappingItem<MappingMember, MappingLinkType extends Comparable<MappingLinkType>, MappingTargetID extends Comparable<MappingTargetID>, MappingTraversalTestType>
            implements Serializable {
        private final MappingMember atlasMember;
        private final TreeMap<MappingLinkType, TargetedTester<MappingLinkType, MappingTargetID, MappingTraversalTestType>> links;

        protected AtlasMappingItem(MappingMember atlasMember) {
            this.atlasMember = atlasMember;
            this.links = new TreeMap<>();
        }

        public MappingMember getAtlasMember() {
            return atlasMember;
        }

        public Collection<TargetedTester<MappingLinkType, MappingTargetID, MappingTraversalTestType>> getTargetedTesters() {
            return this.getLinks().values();
        }

        public Map<MappingLinkType, TargetedTester<MappingLinkType, MappingTargetID, MappingTraversalTestType>> getLinks() {
            return this.links.entrySet().stream().filter(entry -> entry.getKey() != null && entry.getValue() != null)
                    .collect(Collectors.toUnmodifiableMap(entry -> entry.getKey(), entry -> entry.getValue()));
        }

        public Set<MappingLinkType> getAvailableLinks() {
            return this.getLinks().keySet();
        }

        @Override
        public int hashCode() {
            return Objects.hash(atlasMember);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof AtlasMappingItem))
                return false;
            AtlasMappingItem<?, ?, ?, ?> other = (AtlasMappingItem<?, ?, ?, ?>) obj;
            return Objects.equals(atlasMember, other.atlasMember);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("AtlasMappingItem [atlasMember=").append(atlasMember).append(", links=").append(links)
                    .append("]");
            return builder.toString();
        }

    }

    protected final UUID uuid = UUID.randomUUID();
    // TODO: wait for `SequencedCollection` from Java21
    private final Map<AtlasMemberID, AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> mapping;

    public abstract AtlasMemberID getIDForMemberType(AtlasMemberType member);

    public abstract String getNameForMemberType(AtlasMemberType member);

    protected Atlas() {
        this.mapping = new LinkedHashMap<>(); // keep insertion order
    }

    public synchronized int size() {
        return this.mapping.size();
    }

    public final synchronized void addMember(final AtlasMemberType nextMember) {
        this.emplaceMember(nextMember);
    }

    private final synchronized AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> emplaceMember(
            final AtlasMemberType nextMember) {
        synchronized (this.mapping) {
            if (nextMember == null) {
                throw new IllegalArgumentException("Cannot add null member!");
            }
            final AtlasMemberID nextMemberID = this.getIDForMemberType(nextMember);
            return this.mapping.computeIfAbsent(nextMemberID, key -> new AtlasMappingItem<>(nextMember));
        }
    }

    public final synchronized void connectOneWay(final AtlasMemberType first, final AtlasLinkType toSecond,
            final AtlasMemberType second, AtlasTraversalTestType predicate) {
        synchronized (this.mapping) {
            if (first == null) {
                throw new IllegalArgumentException("The 'first' argument must not be null!");
            } else if (second == null) {
                throw new IllegalArgumentException("the 'second' argument must not be null!");
            }

            if (toSecond == null) {
                throw new IllegalArgumentException("The provided link to the second must not be null!");
            }

            final AtlasMemberID firstID = this.getIDForMemberType(first);
            final AtlasMemberID secondID = this.getIDForMemberType(second);
            if (firstID == null) {
                throw new IllegalStateException(String.format("Cannot retrieve member ID for %s", first));
            } else if (secondID == null) {
                throw new IllegalStateException(String.format("Cannot retrieve member ID for %s", second));
            }

            AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> firstMappingItem = this
                    .getAtlasMappingItem(firstID);
            AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> secondMappingItem = this
                    .getAtlasMappingItem(secondID);
            if (firstMappingItem == null && secondMappingItem == null) {
                throw new IllegalStateException(String
                        .format("One or both of the following MUST already exist in the Atlas! %s %s", first, second));
            } else if (firstMappingItem == null) {
                firstMappingItem = this.emplaceMember(first);
            } else if (secondMappingItem == null) {
                secondMappingItem = this.emplaceMember(second);
            }

            firstMappingItem.links.put(toSecond, new TargetedTester<>(toSecond, secondID, predicate));
        }
    }

    public abstract AtlasLinkType translateLinkToOpposite(AtlasLinkType link);

    /**
     * Meant to be overridden. This is what is called when no predicate reverser is
     * given to connectTwoWay.
     * 
     * By default returns the input (identity function).
     * 
     * @param predicate
     * @return
     */
    public AtlasTraversalTestType reverseTraversalTest(AtlasTraversalTestType predicate) {
        return predicate;
    }

    public final synchronized void connectTwoWay(final AtlasMemberType first, final AtlasLinkType firstToSecond,
            final AtlasMemberType second, final AtlasTraversalTestType predicate,
            Function<AtlasLinkType, AtlasLinkType> linkReverser,
            Function<AtlasTraversalTestType, AtlasTraversalTestType> predicateReverser) {
        if (firstToSecond == null) {
            throw new IllegalArgumentException("The provided link to the second must not be null!");
        }
        final AtlasLinkType secondToFirst = linkReverser != null ? linkReverser.apply(firstToSecond)
                : this.translateLinkToOpposite(firstToSecond);
        if (secondToFirst == null) {
            throw new IllegalArgumentException(String.format("The reversal of '%s' must not be null!", firstToSecond));
        }
        synchronized (this.mapping) {
            this.connectOneWay(first, firstToSecond, second, predicate);
            this.connectOneWay(second, secondToFirst, first,
                    predicateReverser != null ? predicateReverser.apply(predicate)
                            : this.reverseTraversalTest(predicate));
        }
    }

    public final synchronized void connect(AtlasMemberType first, AtlasLinkType toSecond, AtlasMemberType second,
            AtlasTraversalTestType predicate) {
        synchronized (this.mapping) {
            this.connectTwoWay(first, toSecond, second, predicate, this::translateLinkToOpposite,
                    this::reverseTraversalTest);
        }
    }

    public final Set<AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> getAtlasMappingItems() {
        synchronized (this.mapping) {
            LinkedHashSet<AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> mappingSet = new LinkedHashSet<>();
            for (final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mappingItem : this.mapping
                    .values()) {
                if (mappingItem == null) {
                    continue;
                }
                mappingSet.add(mappingItem);
            }
            return Collections.unmodifiableSet(mappingSet);
        }
    }

    public final Optional<AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> getFirstMappingItem() {
        synchronized (this.mapping) {
            return this.mapping.values().stream().filter(item -> item != null).findFirst();
        }
    }

    public final Set<AtlasMemberType> getAtlasMembers() {
        synchronized (this.mapping) {
            LinkedHashSet<AtlasMemberType> mappingSet = new LinkedHashSet<>();
            for (final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mappingItem : this.mapping
                    .values()) {
                if (mappingItem == null) {
                    continue;
                }
                final AtlasMemberType member = mappingItem.getAtlasMember();
                if (member == null) {
                    continue;
                }
                mappingSet.add(member);
            }
            return Collections.unmodifiableSet(mappingSet);
        }
    }

    public final AtlasMemberType getFirstMember() {
        synchronized (this.mapping) {
            Optional<AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> firstItem = this
                    .getFirstMappingItem();
            if (firstItem == null || firstItem.isEmpty()) {
                return null;
            }
            return firstItem.get().getAtlasMember();
        }
    }

    public final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> getAtlasMappingItem(
            AtlasMemberID memberID) {
        synchronized (this.mapping) {
            if (memberID == null) {
                return null;
            }
            return this.mapping.get(memberID);
        }
    }

    public final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> getAtlasMappingItem(
            AtlasMemberType possMember) {
        synchronized (this.mapping) {
            return this.getAtlasMappingItem(this.getIDForMemberType(possMember));
        }

    }

    public final AtlasMemberType getAtlasMember(AtlasMemberID memberId) {
        synchronized (this.mapping) {
            AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> item = this
                    .getAtlasMappingItem(memberId);
            if (item == null) {
                return null;
            }
            return item.getAtlasMember();
        }
    }

    public final UUID getUuid() {
        return uuid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Atlas))
            return false;
        Atlas<?, ?, ?, ?> other = (Atlas<?, ?, ?, ?>) obj;
        return Objects.equals(uuid, other.uuid);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Atlas [uuid=").append(uuid).append(", mapping=").append(mapping).append("]");
        return builder.toString();
    }

    public class DepthFirstIterator implements
            Iterator<AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> {

        private Deque<AtlasMemberID> stack;
        private LinkedHashSet<AtlasMemberID> visited;
        private Iterator<AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> innerIterator;

        public DepthFirstIterator() {
            this.stack = new ArrayDeque<>();
            this.visited = new LinkedHashSet<>();
            this.innerIterator = Atlas.this.getAtlasMappingItems().iterator();
        }

        @Override
        public boolean hasNext() {
            return (!this.stack.isEmpty() || this.innerIterator.hasNext()) && this.visited.size() < Atlas.this.size();
        }

        @Override
        public AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }

            if (this.stack.isEmpty()) {
                while (this.innerIterator.hasNext()) { // for each subtree
                    final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this.innerIterator
                            .next();
                    final AtlasMemberID checkId = Atlas.this.getIDForMemberType(member.getAtlasMember());
                    if (this.visited.contains(checkId)) {
                        continue;
                    }
                    this.stack.push(checkId);
                    break;
                }
            }

            while (!this.stack.isEmpty()) { // get all the connected nodes
                final AtlasMemberID current = this.stack.pop();
                final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mappingItem = getAtlasMappingItem(
                        current);
                if (!this.visited.contains(current)) {
                    this.visited.add(current);
                    this.stack.push(current);
                    return mappingItem;
                }
                final Collection<TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> targetedTesters = mappingItem
                        .getTargetedTesters();
                for (final TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> tester : targetedTesters) {
                    if (!this.visited.contains(tester.getTargetId())) {
                        this.stack.push(tester.getTargetId());
                    }
                }
            }

            throw new NoSuchElementException("End of Line");
        }

    }

    public DepthFirstIterator depthFirstIterator() {
        return new DepthFirstIterator();
    }

    public final <TranslateMemberType, TranslateID extends Comparable<TranslateID>, TranslateLinkType extends Comparable<TranslateLinkType>, TranslateTraversalTestType> Map<AtlasMemberID, TranslateID> translate(
            Atlas<TranslateMemberType, TranslateID, TranslateLinkType, TranslateTraversalTestType> translation,
            Function<AtlasMemberType, TranslateMemberType> memberTransformer,
            Function<AtlasLinkType, TranslateLinkType> linkTransformer,
            Function<AtlasTraversalTestType, TranslateTraversalTestType> traversalTestTransformer) {

        if (translation == null) {
            throw new NullPointerException("Cannot translate to a null atlas!");
        }
        final Map<AtlasMemberID, TranslateID> visited = new LinkedHashMap<>();
        if (this.size() == 0) {
            return visited;
        }
        if (memberTransformer == null) {
            throw new IllegalArgumentException("Must provide a member transformer function");
        } else if (linkTransformer == null) {
            throw new IllegalArgumentException("Must provide a link transformer function");
        } else if (traversalTestTransformer == null) {
            throw new IllegalArgumentException("Must provide a traversal transformer function");
        }

        for (final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mappingItem : this
                .getAtlasMappingItems()) {
            final AtlasMemberType member = mappingItem.getAtlasMember();
            if (member == null) {
                throw new NullPointerException(String.format("Mapping item '%s' must have a member!", mappingItem));
            }
            final AtlasMemberID mappingItemId = this.getIDForMemberType(mappingItem.getAtlasMember());
            final TranslateMemberType translatedMember = memberTransformer.apply(member);
            final TranslateID translatedID = translation.getIDForMemberType(translatedMember);
            translation.addMember(translatedMember);
            visited.put(mappingItemId, translatedID);
        }

        for (final Entry<AtlasMemberID, TranslateID> vistedEntry : visited.entrySet()) {
            final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this
                    .getAtlasMappingItem(vistedEntry.getKey());
            final AtlasMappingItem<TranslateMemberType, TranslateLinkType, TranslateID, TranslateTraversalTestType> translatedMember = translation
                    .getAtlasMappingItem(vistedEntry.getValue());
            for (final TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> tester : member
                    .getTargetedTesters()) {
                final AtlasMemberID targetMemberID = tester.getTargetId();
                final AtlasMappingItem<TranslateMemberType, TranslateLinkType, TranslateID, TranslateTraversalTestType> translatedTarget = translation
                        .getAtlasMappingItem(visited.get(targetMemberID));
                translation.connectOneWay(translatedMember.getAtlasMember(), linkTransformer.apply(tester.getLink()),
                        translatedTarget.getAtlasMember(), traversalTestTransformer.apply(tester.getPredicate()));
            }
        }

        return visited;
    }

    public final <TranslateMemberType, TranslateID extends Comparable<TranslateID>, TranslateLinkType extends Comparable<TranslateLinkType>, TranslateTraversalTestType> Map<AtlasMemberID, TranslateID> translateToSuppliedAtlas(
            Supplier<Atlas<TranslateMemberType, TranslateID, TranslateLinkType, TranslateTraversalTestType>> starter,
            Function<AtlasMemberType, TranslateMemberType> memberTransformer,
            Function<AtlasLinkType, TranslateLinkType> linkTransformer,
            Function<AtlasTraversalTestType, TranslateTraversalTestType> traversalTestTransformer) {
        if (starter == null) {
            throw new IllegalArgumentException("Must provide an Atlas supplier for translation!");
        }
        final Atlas<TranslateMemberType, TranslateID, TranslateLinkType, TranslateTraversalTestType> translation = starter
                .get();
        return this.translate(translation, memberTransformer, linkTransformer, traversalTestTransformer);
    }

    public final String toMermaidFlowchart(boolean fence) {
        StringBuilder sb = new StringBuilder();
        StringBuilder edges = new StringBuilder();
        if (fence) {
            sb.append("```mermaid").append("\r\n");
        }
        sb.append("flowchart LR").append("\r\n");
        for (final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mapItem : this
                .getAtlasMappingItems()) {
            final AtlasMemberType member = mapItem.getAtlasMember();
            final String uuid = this.getIDForMemberType(member).toString();
            sb.append("    ").append(uuid).append("[").append(this.getNameForMemberType(member)).append("]\r\n");
            for (final TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> dir : mapItem
                    .getTargetedTesters()) {
                String otherUUID = dir.getTargetId().toString();
                edges.append("    ").append(uuid).append("-->|").append(dir.getLink()).append("|").append(otherUUID)
                        .append("\r\n");
            }
        }
        sb.append("\r\n");
        sb.append(edges.toString());
        if (fence) {
            sb.append("```").append("\r\n");
        }
        return sb.toString();
    }

}
