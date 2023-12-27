package com.lhf.game.map;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.lhf.game.map.Land.TraversalTester;

public abstract class Atlas<AtlasMemberType, AtlasMemberID extends Comparable<AtlasMemberID>> {
    protected final class TargetedTester {
        final Directions direction;
        final AtlasMemberID targetId;
        final TraversalTester predicate;

        private TargetedTester(Directions direction, AtlasMemberID targetId, TraversalTester predicate) {
            this.direction = direction;
            this.targetId = targetId;
            this.predicate = predicate;
        }

        public Directions getDirection() {
            return direction;
        }

        public AtlasMemberID getTargetId() {
            return targetId;
        }

        public TraversalTester getPredicate() {
            return predicate;
        }

    }

    protected final class AtlasMappingItem {
        final AtlasMemberType atlasMember;
        public final EnumMap<Directions, TargetedTester> directions;

        private AtlasMappingItem(AtlasMemberType atlasMember) {
            this.atlasMember = atlasMember;
            this.directions = new EnumMap<>(Directions.class);
        }

        public AtlasMemberType getAtlasMember() {
            return atlasMember;
        }

        public AtlasMemberID getAtlasMemberID() {
            return getIDForMemberType(this.atlasMember);
        }

        public Map<Directions, TargetedTester> getDirections() {
            return Collections.unmodifiableMap(this.directions);
        }

        public Set<Directions> getAvailableDirections() {
            return this.getDirections().keySet();
        }

    }

    protected final Map<AtlasMemberID, AtlasMappingItem> mapping;

    public abstract AtlasMemberID getIDForMemberType(AtlasMemberType member);

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
        this.mapping.put(key, new AtlasMappingItem(first));
    }

    public final synchronized void connectOneWay(AtlasMemberType existing, Directions toNext, AtlasMemberType next,
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
            final Atlas<AtlasMemberType, AtlasMemberID>.AtlasMappingItem existingEntry = this.mapping
                    .getOrDefault(existingKey, null);
            if (existingEntry == null) {
                throw new IllegalStateException(
                        String.format("The value obtained from \"existing\" key argument '%s' must not be null!",
                                existingKey));
            }
            if (toNext == null) {
                throw new IllegalArgumentException("The provided direction must not be null!");
            }
            final AtlasMemberID nextKey = this.getIDForMemberType(existing);
            if (nextKey == null) {
                throw new IllegalStateException(
                        String.format("The key obtained from \"next\" argument '%s' must not be null!", next));
            }
            if (next == null) {
                throw new IllegalArgumentException("The 'next' argument must not be null!");
            }
            final Atlas<AtlasMemberType, AtlasMemberID>.AtlasMappingItem nextMappingItem = new AtlasMappingItem(next);
            this.mapping.put(nextKey, nextMappingItem);
            existingEntry.directions.put(toNext, new TargetedTester(toNext, nextKey, predicate));
        }
    }

    public final synchronized void connect(AtlasMemberType existing, Directions toNext, AtlasMemberType next,
            TraversalTester predicate) {
        synchronized (this.mapping) {
            this.connectOneWay(existing, toNext, next, predicate);
            final Atlas<AtlasMemberType, AtlasMemberID>.AtlasMappingItem nextMappingItem = this.mapping
                    .get(this.getIDForMemberType(next));
            final AtlasMemberID existingKey = this.getIDForMemberType(existing);
            nextMappingItem.directions.put(toNext.opposite(),
                    new TargetedTester(toNext.opposite(), existingKey, predicate));
        }
    }

    public final Set<AtlasMemberType> getAtlasMembers() {
        synchronized (this.mapping) {
            return this.mapping.values().stream().filter(mapItem -> mapItem != null)
                    .map(mapItem -> mapItem.getAtlasMember()).filter(atlasMember -> atlasMember != null)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    public final AtlasMappingItem getAtlasMappingItem(AtlasMemberID memberID) {
        synchronized (this.mapping) {
            if (memberID == null) {
                return null;
            }
            return this.mapping.get(memberID);
        }
    }

    public final AtlasMappingItem getAtlasMappingItem(AtlasMemberType possMember) {
        synchronized (this.mapping) {
            return this.getAtlasMappingItem(this.getIDForMemberType(possMember));
        }

    }

}
