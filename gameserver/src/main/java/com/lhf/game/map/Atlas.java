package com.lhf.game.map;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    protected Atlas(final AtlasMemberType first) {
        if (first == null) {
            throw new IllegalArgumentException("Cannot make an Atlas with a null first!");
        }
        final AtlasMemberID key = this.getIDForMemberType(first);
        if (key == null) {
            throw new IllegalStateException(
                    String.format("The key obtained from the first entry '%s' must not be null!", first));
        }
        this.mapping = new LinkedHashMap<>(); // keep insertion order
        this.mapping.put(key, new AtlasMappingItem<>(first));
    }

    public int size() {
        return this.mapping.size();
    }

    public final synchronized void connectOneWay(AtlasMemberType existing, Directions toExisting, AtlasMemberType next,
            TraversalTester predicate) {
        synchronized (this.mapping) {
            if (existing == null) {
                throw new IllegalArgumentException("The 'existing' argument must not be null!");
            }
            final AtlasMemberID existingKey = this.getIDForMemberType(existing);
            if (existingKey == null) {
                throw new IllegalStateException(
                        String.format("The key obtained from \"existing\" argument '%s' must not be null!", existing));
            }
            final AtlasMappingItem<AtlasMemberType, AtlasMemberID> existingEntry = this.mapping
                    .getOrDefault(existingKey, null);
            if (existingEntry == null) {
                throw new IllegalStateException(
                        String.format("The value obtained from \"existing\" key argument '%s' must not be null!",
                                existingKey));
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
            final AtlasMappingItem<AtlasMemberType, AtlasMemberID> nextEntry = this.mapping.computeIfAbsent(nextKey,
                    key -> new AtlasMappingItem<>(next));
            nextEntry.directions.put(toExisting, new TargetedTester<>(toExisting, nextKey, predicate));
        }
    }

    public final synchronized void connect(AtlasMemberType existing, Directions toNext, AtlasMemberType next,
            TraversalTester predicate) {
        synchronized (this.mapping) {
            this.connectOneWay(existing, toNext.opposite(), next, predicate);
            final AtlasMemberID existingKey = this.getIDForMemberType(existing);
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

    protected AtlasMappingItem<AtlasMemberType, AtlasMemberID> potentialNewMapping(AtlasMemberID id,
            AtlasMemberType nextMember) {
        return this.mapping.computeIfAbsent(id, key -> new AtlasMappingItem<>(nextMember));
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
            translation.potentialNewMapping(transformedMemberID, transformedMember);
            visited.put(visiting, transformedMemberID);

            for (final TargetedTester<AtlasMemberID> tester : targetedTesters) {
                System.out.printf("%s %s %s\n", visiting, tester.getDirection(), tester.getTargetId());
                final TT visitedItem = this.depthFirstTraversal(translation, transformer, visited, tester.targetId);
                translation.connectOneWay(transformedMember, tester.getDirection(), visitedItem, tester.getPredicate());
            }
            return transformedMember;
        }
    }

    public final <TT, TID extends Comparable<TID>, T extends Atlas<TT, TID>> T translate(Function<TT, T> starter,
            Function<AtlasMemberType, TT> transformer) {

        final AtlasMappingItem<AtlasMemberType, AtlasMemberID> firstMapitem = this.getAtlasMappingItems().stream()
                .findFirst().orElse(null);
        if (firstMapitem == null) {
            throw new IllegalStateException("Cannot translate empty Atlas!");
        }
        final AtlasMemberType first = firstMapitem.getAtlasMember();
        final AtlasMemberID firstID = this.getIDForMemberType(first);
        final TT transformedFirst = transformer.apply(first);
        final T translation = starter.apply(transformedFirst);
        Map<AtlasMemberID, TID> visited = new LinkedHashMap<>();
        visited.put(firstID, translation.getIDForMemberType(transformedFirst)); // should prevent first from being
                                                                                // remade

        // special check for those who link to first
        for (final TargetedTester<AtlasMemberID> tester : firstMapitem.getTargetedTesters()) {
            System.out.printf("%s %s %s\n", firstID, tester.getDirection(), tester.getTargetId());
            final TT visitedItem = this.depthFirstTraversal(translation, transformer, visited, tester.targetId);
            translation.connectOneWay(transformedFirst, tester.getDirection(), visitedItem, tester.getPredicate());
        }

        // anyone else?
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
