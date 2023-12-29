package com.lhf.game.map;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Function;
import com.lhf.game.map.Land.TraversalTester;

public abstract class Atlas<AtlasMemberType, AtlasMemberID extends Comparable<AtlasMemberID>> {
    protected static final class TargetedTester<TargetIDType> implements Serializable {
        private final Directions direction;
        private final TargetIDType targetId;
        private final TraversalTester predicate;

        protected TargetedTester(Directions direction, TargetIDType targetId, TraversalTester predicate) {
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

        public TraversalTester getPredicate() {
            return predicate;
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

    public final synchronized void connectOneWay(final AtlasMemberID existingMemberID, final Directions toExisting,
            final AtlasMemberType next, final TraversalTester predicate) {
        synchronized (this.mapping) {
            if (existingMemberID == null) {
                throw new IllegalArgumentException("The existing memberID must not be null!");
            }
            final AtlasMappingItem<AtlasMemberType, AtlasMemberID> memberItem = this
                    .getAtlasMappingItem(existingMemberID);
            if (memberItem == null) {
                throw new IllegalStateException(
                        String.format("Cannot retrieve member item for ID %s", existingMemberID));
            }
            if (toExisting == null) {
                throw new IllegalArgumentException("The provided direction must not be null!");
            }
            final AtlasMemberID nextKey = this.getIDForMemberType(next);
            if (nextKey == null) {
                throw new IllegalStateException(
                        String.format("The key obtained from \"next\" argument '%s' must not be null!", next));
            }
            if (next == null) {
                throw new IllegalArgumentException("The 'next' argument must not be null!");
            }
            final AtlasMappingItem<AtlasMemberType, AtlasMemberID> nextEntry = this.emplaceMember(next);
            nextEntry.directions.put(toExisting, new TargetedTester<>(toExisting, existingMemberID, predicate));
        }
    }

    public final synchronized void connectOneWay(final AtlasMemberType existing, final Directions toExisting,
            final AtlasMemberType next,
            TraversalTester predicate) {
        synchronized (this.mapping) {
            if (existing == null) {
                throw new IllegalArgumentException("The 'existing' argument must not be null!");
            }
            final AtlasMemberID existingKey = this.getIDForMemberType(existing);
            this.connectOneWay(existingKey, toExisting, next, predicate);
        }
    }

    public final synchronized void connect(AtlasMemberType existing, Directions toNext, AtlasMemberType next,
            TraversalTester predicate) {
        synchronized (this.mapping) {
            final AtlasMemberID existingKey = this.getIDForMemberType(existing);
            this.connectOneWay(existingKey, toNext.opposite(), next, predicate);
            final AtlasMappingItem<AtlasMemberType, AtlasMemberID> existingMapItem = this.mapping
                    .get(existingKey);
            final AtlasMemberID nextKey = this.getIDForMemberType(next);
            existingMapItem.directions.put(toNext, new TargetedTester<>(toNext, nextKey, predicate));
        }
    }

    public final Set<AtlasMappingItem<AtlasMemberType, AtlasMemberID>> getAtlasMappingItems() {
        synchronized (this.mapping) {
            return this.mapping.values().stream().filter(mapItem -> mapItem != null)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    public final Optional<AtlasMappingItem<AtlasMemberType, AtlasMemberID>> getFirstMappingItem() {
        synchronized (this.mapping) {
            return this.mapping.values().stream().filter(item -> item != null).findFirst();
        }
    }

    public final Set<AtlasMemberType> getAtlasMembers() {
        synchronized (this.mapping) {
            return this.mapping.values().stream().filter(mapItem -> mapItem != null)
                    .map(mapItem -> mapItem.getAtlasMember()).filter(atlasMember -> atlasMember != null)
                    .collect(Collectors.toUnmodifiableSet());
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

    private final <TT, TID extends Comparable<TID>, T extends Atlas<TT, TID>> TT depthFirstTraversal(
            final T translation, final Function<AtlasMemberType, TT> transformer, final Map<AtlasMemberID, TID> visited,
            final AtlasMemberID visiting) {
        synchronized (this.mapping) {
            if (visited.containsKey(visiting)) {
                return translation.getAtlasMember(visited.get(visiting));
            }

            final AtlasMappingItem<AtlasMemberType, AtlasMemberID> mappingItem = this.getAtlasMappingItem(visiting);
            final Collection<TargetedTester<AtlasMemberID>> targetedTesters = mappingItem.getTargetedTesters();
            final TT transformedMember = transformer.apply(mappingItem.getAtlasMember());
            final TID transformedMemberID = translation.getIDForMemberType(transformedMember);
            translation.addMember(transformedMember);
            visited.put(visiting, transformedMemberID);

            for (final TargetedTester<AtlasMemberID> tester : targetedTesters) {
                System.out.printf("%s %s %s\n", visiting, tester.getDirection(), tester.getTargetId());
                final TT visitedItem = this.depthFirstTraversal(translation, transformer, visited, tester.targetId);
                translation.connectOneWay(transformedMember, tester.getDirection(), visitedItem, tester.getPredicate());
            }
            return transformedMember;
        }
    }

    public final <TT, TID extends Comparable<TID>, T extends Atlas<TT, TID>> T translate(Supplier<T> starter,
            Function<AtlasMemberType, TT> transformer) {

        final T translation = starter.get();
        if (this.size() == 0) {
            return translation;
        }

        Map<AtlasMemberID, TID> visited = new LinkedHashMap<>();

        for (AtlasMappingItem<AtlasMemberType, AtlasMemberID> mappingItem : this.getAtlasMappingItems()) {
            this.depthFirstTraversal(translation, transformer, visited,
                    this.getIDForMemberType(mappingItem.getAtlasMember()));
        }

        return translation;
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
