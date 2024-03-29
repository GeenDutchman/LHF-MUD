package com.lhf.game.map;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Function;

public abstract class Atlas<AtlasMemberType, AtlasMemberID extends Comparable<AtlasMemberID>> {
    protected static final class TargetedTester<TargetIDType> implements Serializable {
        private final Directions direction;
        private final TargetIDType targetId;
        private final Doorway predicate;

        protected TargetedTester(Directions direction, TargetIDType targetId, Doorway predicate) {
            this.direction = direction;
            this.targetId = targetId;
            this.predicate = predicate;
        }

        public Directions getDirection() {
            return direction;
        }

        public TargetIDType getTargetId() {
            return targetId;
        }

        public Doorway getPredicate() {
            return predicate;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, targetId, predicate);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof TargetedTester))
                return false;
            TargetedTester<?> other = (TargetedTester<?>) obj;
            return direction == other.direction && Objects.equals(targetId, other.targetId)
                    && Objects.equals(predicate, other.predicate);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("TargetedTester [direction=").append(direction).append(", targetId=").append(targetId)
                    .append(", predicate=").append(predicate).append("]");
            return builder.toString();
        }

    }

    protected final static class AtlasMappingItem<MappingMember, MappingTargetID> implements Serializable {
        private final MappingMember atlasMember;
        private final EnumMap<Directions, TargetedTester<MappingTargetID>> directions;

        protected AtlasMappingItem(MappingMember atlasMember) {
            this.atlasMember = atlasMember;
            this.directions = new EnumMap<>(Directions.class);
        }

        public MappingMember getAtlasMember() {
            return atlasMember;
        }

        public Collection<TargetedTester<MappingTargetID>> getTargetedTesters() {
            return this.getDirections().values();
        }

        public Map<Directions, TargetedTester<MappingTargetID>> getDirections() {
            return this.directions.entrySet().stream()
                    .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                    .collect(Collectors.toUnmodifiableMap(entry -> entry.getKey(), entry -> entry.getValue()));
        }

        public Set<Directions> getAvailableDirections() {
            return this.getDirections().keySet();
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
            AtlasMappingItem<?, ?> other = (AtlasMappingItem<?, ?>) obj;
            return Objects.equals(atlasMember, other.atlasMember);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("AtlasMappingItem [atlasMember=").append(atlasMember).append(", directions=")
                    .append(directions).append("]");
            return builder.toString();
        }

    }

    private final Map<AtlasMemberID, AtlasMappingItem<AtlasMemberType, AtlasMemberID>> mapping;

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

    private final synchronized AtlasMappingItem<AtlasMemberType, AtlasMemberID> emplaceMember(
            final AtlasMemberType nextMember) {
        synchronized (this.mapping) {
            if (nextMember == null) {
                throw new IllegalArgumentException("Cannot add null member!");
            }
            final AtlasMemberID nextMemberID = this.getIDForMemberType(nextMember);
            return this.mapping.computeIfAbsent(nextMemberID, key -> new AtlasMappingItem<>(nextMember));
        }
    }

    public final synchronized void connectOneWay(final AtlasMemberType first, final Directions toSecond,
            final AtlasMemberType second, Doorway predicate) {
        synchronized (this.mapping) {
            if (first == null) {
                throw new IllegalArgumentException("The 'first' argument must not be null!");
            } else if (second == null) {
                throw new IllegalArgumentException("the 'second' argument must not be null!");
            }

            if (toSecond == null) {
                throw new IllegalArgumentException("The provided direction must not be null!");
            }

            final AtlasMemberID firstID = this.getIDForMemberType(first);
            final AtlasMemberID secondID = this.getIDForMemberType(second);
            if (firstID == null) {
                throw new IllegalStateException(String.format("Cannot retrieve member ID for %s", first));
            } else if (secondID == null) {
                throw new IllegalStateException(String.format("Cannot retrieve member ID for %s", second));
            }

            AtlasMappingItem<AtlasMemberType, AtlasMemberID> firstMappingItem = this.getAtlasMappingItem(firstID);
            AtlasMappingItem<AtlasMemberType, AtlasMemberID> secondMappingItem = this.getAtlasMappingItem(secondID);
            if (firstMappingItem == null && secondMappingItem == null) {
                throw new IllegalStateException(String
                        .format("One or both of the following MUST already exist in the Atlas! %s %s", first, second));
            } else if (firstMappingItem == null) {
                firstMappingItem = this.emplaceMember(first);
            } else if (secondMappingItem == null) {
                secondMappingItem = this.emplaceMember(second);
            }

            firstMappingItem.directions.put(toSecond, new TargetedTester<>(toSecond, secondID, predicate));
        }
    }

    public final synchronized void connect(AtlasMemberType first, Directions toSecond, AtlasMemberType second,
            Doorway predicate) {
        synchronized (this.mapping) {
            this.connectOneWay(first, toSecond, second, predicate);
            this.connectOneWay(second, toSecond.opposite(), first, predicate);
        }
    }

    public final Set<AtlasMappingItem<AtlasMemberType, AtlasMemberID>> getAtlasMappingItems() {
        synchronized (this.mapping) {
            LinkedHashSet<AtlasMappingItem<AtlasMemberType, AtlasMemberID>> mappingSet = new LinkedHashSet<>();
            for (final AtlasMappingItem<AtlasMemberType, AtlasMemberID> mappingItem : this.mapping.values()) {
                if (mappingItem == null) {
                    continue;
                }
                mappingSet.add(mappingItem);
            }
            return Collections.unmodifiableSet(mappingSet);
        }
    }

    public final Optional<AtlasMappingItem<AtlasMemberType, AtlasMemberID>> getFirstMappingItem() {
        synchronized (this.mapping) {
            return this.mapping.values().stream().filter(item -> item != null).findFirst();
        }
    }

    public final Set<AtlasMemberType> getAtlasMembers() {
        synchronized (this.mapping) {
            LinkedHashSet<AtlasMemberType> mappingSet = new LinkedHashSet<>();
            for (final AtlasMappingItem<AtlasMemberType, AtlasMemberID> mappingItem : this.mapping.values()) {
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
            Optional<AtlasMappingItem<AtlasMemberType, AtlasMemberID>> firstItem = this.getFirstMappingItem();
            if (firstItem == null || firstItem.isEmpty()) {
                return null;
            }
            return firstItem.get().getAtlasMember();
        }
    }

    public final AtlasMappingItem<AtlasMemberType, AtlasMemberID> getAtlasMappingItem(AtlasMemberID memberID) {
        synchronized (this.mapping) {
            if (memberID == null) {
                return null;
            }
            return this.mapping.get(memberID);
        }
    }

    public final AtlasMappingItem<AtlasMemberType, AtlasMemberID> getAtlasMappingItem(AtlasMemberType possMember) {
        synchronized (this.mapping) {
            return this.getAtlasMappingItem(this.getIDForMemberType(possMember));
        }

    }

    public final AtlasMemberType getAtlasMember(AtlasMemberID memberId) {
        synchronized (this.mapping) {
            AtlasMappingItem<AtlasMemberType, AtlasMemberID> item = this.getAtlasMappingItem(memberId);
            if (item == null) {
                return null;
            }
            return item.getAtlasMember();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapping);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Atlas))
            return false;
        Atlas<?, ?> other = (Atlas<?, ?>) obj;
        return Objects.equals(mapping, other.mapping);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Atlas [mapping=").append(mapping).append("]");
        return builder.toString();
    }

    public class DepthFirstIterator implements Iterator<AtlasMappingItem<AtlasMemberType, AtlasMemberID>> {

        private Deque<AtlasMemberID> stack;
        private LinkedHashSet<AtlasMemberID> visited;
        private Iterator<AtlasMappingItem<AtlasMemberType, AtlasMemberID>> innerIterator;

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
        public AtlasMappingItem<AtlasMemberType, AtlasMemberID> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }

            if (this.stack.isEmpty()) {
                while (this.innerIterator.hasNext()) { // for each subtree
                    final AtlasMappingItem<AtlasMemberType, AtlasMemberID> member = this.innerIterator.next();
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
                final AtlasMappingItem<AtlasMemberType, AtlasMemberID> mappingItem = getAtlasMappingItem(current);
                if (!this.visited.contains(current)) {
                    this.visited.add(current);
                    this.stack.push(current);
                    return mappingItem;
                }
                final Collection<TargetedTester<AtlasMemberID>> targetedTesters = mappingItem.getTargetedTesters();
                for (final TargetedTester<AtlasMemberID> tester : targetedTesters) {
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

    public final <TT, TID extends Comparable<TID>, T extends Atlas<TT, TID>> Map<AtlasMemberID, TID> translate(
            Supplier<T> starter,
            Function<AtlasMemberType, TT> transformer) {

        if (starter == null) {
            throw new IllegalArgumentException("Must provide an Atlas supplier for translation!");
        }
        final T translation = starter.get();
        if (translation == null) {
            throw new NullPointerException("Cannot translate to a null atlas!");
        }
        final Map<AtlasMemberID, TID> visited = new LinkedHashMap<>();
        if (this.size() == 0) {
            return visited;
        }
        if (transformer == null) {
            throw new IllegalArgumentException("Must provide a transformer function");
        }

        for (final AtlasMappingItem<AtlasMemberType, AtlasMemberID> mappingItem : this.getAtlasMappingItems()) {
            final AtlasMemberType member = mappingItem.getAtlasMember();
            if (member == null) {
                throw new NullPointerException(String.format("Mapping item '%s' must have a member!", mappingItem));
            }
            final AtlasMemberID mappingItemId = this.getIDForMemberType(mappingItem.getAtlasMember());
            final TT translatedMember = transformer.apply(member);
            final TID translatedID = translation.getIDForMemberType(translatedMember);
            translation.addMember(translatedMember);
            visited.put(mappingItemId, translatedID);
        }

        for (final Entry<AtlasMemberID, TID> vistedEntry : visited.entrySet()) {
            final AtlasMappingItem<AtlasMemberType, AtlasMemberID> member = this
                    .getAtlasMappingItem(vistedEntry.getKey());
            final AtlasMappingItem<TT, TID> translatedMember = translation.getAtlasMappingItem(vistedEntry.getValue());
            for (final TargetedTester<AtlasMemberID> tester : member.getTargetedTesters()) {
                final AtlasMemberID targetMemberID = tester.getTargetId();
                final AtlasMappingItem<TT, TID> translatedTarget = translation
                        .getAtlasMappingItem(visited.get(targetMemberID));
                translation.connectOneWay(translatedMember.getAtlasMember(), tester.getDirection(),
                        translatedTarget.getAtlasMember(), tester.getPredicate());
            }
        }

        return visited;
    }

    public final String toMermaid(boolean fence) {
        StringBuilder sb = new StringBuilder();
        StringBuilder edges = new StringBuilder();
        if (fence) {
            sb.append("```mermaid").append("\r\n");
        }
        sb.append("flowchart LR").append("\r\n");
        for (final AtlasMappingItem<AtlasMemberType, AtlasMemberID> mapItem : this.getAtlasMappingItems()) {
            final AtlasMemberType member = mapItem.getAtlasMember();
            final String uuid = this.getIDForMemberType(member).toString();
            sb.append("    ").append(uuid).append("[").append(this.getNameForMemberType(member)).append("]\r\n");
            for (final TargetedTester<AtlasMemberID> dir : mapItem.getTargetedTesters()) {
                String otherUUID = dir.getTargetId().toString();
                edges.append("    ").append(uuid).append("-->|").append(dir.getDirection()).append("|")
                        .append(otherUUID).append("\r\n");
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
