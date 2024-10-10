package com.lhf.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.lhf.Examinable;
import com.lhf.game.ItemContainer.ItemFilterQuery;
import com.lhf.game.creature.CreatureVisitor;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.GameEventProcessorHub;
import com.lhf.server.client.user.UserID;

public interface CreatureContainer extends Examinable, GameEventProcessorHub {
    /**
     * This returns an immutable Collection of Creatures
     * 
     * @return Immutable Collection
     */
    public abstract Collection<ICreature> getCreatures();

    public abstract boolean onCreatureDeath(ICreature creature);

    public abstract boolean addCreature(ICreature creature);

    public abstract Optional<ICreature> removeCreature(String name);

    public abstract boolean removeCreature(ICreature creature);

    public abstract boolean addPlayer(Player player);

    public abstract Optional<Player> removePlayer(String name);

    public abstract Optional<Player> removePlayer(UserID id);

    public abstract boolean removePlayer(Player player);

    public enum CreatureFilters {
        NAME, FACTION, VOCATION, TYPE, BATTLING;
    }

    public default Collection<ICreature> filterCreatures(String name, List<String> nameRegexes, CreatureFaction faction,
            VocationName vocation, Class<? extends ICreature> clazz, Boolean isBattling) {
        CreatureFilterQuery query = new CreatureFilterQuery().setName(name).setNameRegexes(nameRegexes)
                .needFaction(faction).needVocation(vocation).needClassname(clazz).setBattling(isBattling);
        return this.filterCreatures(query);
    }

    public static final class CreatureFilterQuery implements Predicate<ICreature> {
        private final String name;
        private final List<String> nameRegexes;
        private final NavigableMap<CreatureFaction, Boolean> factions;
        private final NavigableMap<VocationName, Boolean> vocations;
        private final NavigableMap<String, Boolean> classNames; // TODO: replace with SPECIES enum?
        private final Boolean isBattling;
        private final ItemFilterQuery hasItem;
        private final ItemFilterQuery hasItemEquipped;
        private final NavigableMap<Attributes, MultiRollResult> beatsCheck;
        // TODO: filter by health percentage, health number, check result vs DC

        private CreatureFilterQuery() {
            this.name = null;
            this.nameRegexes = null;
            this.factions = null;
            this.vocations = null;
            this.classNames = null;
            this.isBattling = null;
            this.hasItem = null;
            this.hasItemEquipped = null;
            this.beatsCheck = null;
        }

        public CreatureFilterQuery(CreatureFilterQuery copy) {
            if (copy != null) {
                this.name = copy.name;
                if (copy.nameRegexes != null) {
                    this.nameRegexes = Collections.unmodifiableList(copy.nameRegexes);
                } else {
                    this.nameRegexes = null;
                }
                if (copy.factions != null) {
                    this.factions = Collections.unmodifiableNavigableMap(copy.factions);
                } else {
                    this.factions = null;
                }
                if (copy.vocations != null) {
                    this.vocations = Collections.unmodifiableNavigableMap(copy.vocations);
                } else {
                    this.vocations = null;
                }
                if (copy.classNames != null) {
                    this.classNames = Collections.unmodifiableNavigableMap(copy.classNames);
                } else {
                    this.classNames = null;
                }
                this.isBattling = copy.isBattling;
                this.hasItem = copy.hasItem != null ? new ItemFilterQuery(copy.hasItem) : null;
                this.hasItemEquipped = copy.hasItemEquipped != null ? new ItemFilterQuery(copy.hasItemEquipped) : null;
                if (copy.beatsCheck != null) {
                    this.beatsCheck = Collections.unmodifiableNavigableMap(copy.beatsCheck);
                } else {
                    this.beatsCheck = null;
                }
            } else {
                this.name = null;
                this.nameRegexes = null;
                this.factions = null;
                this.vocations = null;
                this.classNames = null;
                this.isBattling = null;
                this.hasItem = null;
                this.hasItemEquipped = null;
                this.beatsCheck = null;
            }
        }

        public CreatureFilterQuery(String name, List<String> nameRegexes, Map<CreatureFaction, Boolean> factions,
                Map<VocationName, Boolean> vocations, Map<String, Boolean> classNames, Boolean isBattling,
                ItemFilterQuery hasItem, ItemFilterQuery hasItemEquipped, Map<Attributes, MultiRollResult> beatsCheck) {
            this.name = name;
            this.nameRegexes = nameRegexes != null ? Collections.unmodifiableList(List.copyOf(nameRegexes)) : null;
            this.factions = factions != null ? Collections.unmodifiableNavigableMap(new TreeMap<>(factions)) : null;
            this.vocations = vocations != null ? Collections.unmodifiableNavigableMap(new TreeMap<>(vocations)) : null;
            this.classNames = classNames != null ? Collections.unmodifiableNavigableMap(new TreeMap<>(classNames))
                    : null;
            this.isBattling = isBattling;
            this.hasItem = hasItem;
            this.hasItemEquipped = hasItemEquipped;
            this.beatsCheck = beatsCheck != null ? Collections.unmodifiableNavigableMap(new TreeMap<>(beatsCheck))
                    : null;
        }

        public static CreatureFilterQuery withName(String name) {
            return new CreatureFilterQuery(name, null, null, null, null, null, null, null, null);
        }

        public static CreatureFilterQuery withRegexName(String regex) {
            return new CreatureFilterQuery(null, List.of(regex), null, null, null, null, null, null, null);
        }

        public static CreatureFilterQuery withFaction(CreatureFaction faction) {
            return new CreatureFilterQuery(null, null, faction != null ? Map.of(faction, true) : null, null, null, null,
                    null, null, null);
        }

        public static CreatureFilterQuery withFactions(Collection<CreatureFaction> factions) {
            return new CreatureFilterQuery(null, null,
                    factions != null ? factions.stream().filter(faction -> faction != null)
                            .collect(Collectors.toMap(faction -> faction, faction -> true)) : null,
                    null, null, null, null, null, null);
        }

        public static CreatureFilterQuery withoutFaction(CreatureFaction faction) {
            return new CreatureFilterQuery(null, null, faction != null ? Map.of(faction, false) : null, null, null,
                    null, null, null, null);
        }

        public static CreatureFilterQuery withoutFactions(Collection<CreatureFaction> factions) {
            return new CreatureFilterQuery(null, null,
                    factions != null ? factions.stream().filter(faction -> faction != null)
                            .collect(Collectors.toMap(faction -> faction, faction -> false)) : null,
                    null, null, null, null, null, null);
        }

        private static <T> T combineAnd(T value1, T value2) {
            if (value1 == null)
                return value2;
            if (value2 == null)
                return value1;
            return value1.equals(value2) ? value1 : null; // Both must match, otherwise null
        }

        // Helper methods to merge attributes according to OR logic
        private static <T> T combineOr(T value1, T value2) {
            return (value1 != null) ? value1 : value2; // At least one must be non-null
        }

        private static List<String> combineRegexesAnd(List<String> firstRegex, List<String> secondRegex) {
            ArrayList<String> list = new ArrayList<>();
            if (firstRegex != null) {
                list.addAll(firstRegex);
            }
            if (secondRegex != null) {
                list.addAll(secondRegex);
            }
            return list;
        }

        private static List<String> combineRegexesOr(List<String> firstRegex, List<String> secondRegex) {
            ArrayList<String> list = new ArrayList<>();
            if (firstRegex != null && secondRegex == null) {
                list.addAll(firstRegex);
            } else if (firstRegex == null && secondRegex != null) {
                list.addAll(secondRegex);
            } else if (firstRegex != null && secondRegex != null) {
                for (final String first : firstRegex) {
                    if (first == null) {
                        continue;
                    }
                    for (final String second : secondRegex) {
                        if (second != null) {
                            list.add(first + "|" + second);
                        }
                    }
                }
            }
            return list;
        }

        private static <T> NavigableMap<T, Boolean> combineMapsAnd(Map<T, Boolean> firstMap,
                Map<T, Boolean> secondMap) {
            TreeMap<T, Boolean> next = null;
            if (firstMap == null && secondMap == null) {
                return next;
            } else if (firstMap != null && secondMap == null) {
                next = new TreeMap<>(firstMap);
            } else if (firstMap == null && secondMap != null) {
                next = new TreeMap<>(secondMap);
            } else if (firstMap != null && secondMap != null) {
                next = new TreeMap<>(firstMap);
                for (Map.Entry<T, Boolean> entry : secondMap.entrySet()) {
                    next.put(entry.getKey(),
                            CreatureFilterQuery.combineAnd(entry.getValue(), firstMap.get(entry.getKey())));
                }
            }
            return next;
        }

        private static <T> NavigableMap<T, Boolean> combineMapsOr(Map<T, Boolean> firstMap, Map<T, Boolean> secondMap) {
            TreeMap<T, Boolean> next = null;
            if (firstMap == null && secondMap == null) {
                return next;
            } else if (firstMap != null && secondMap == null) {
                next = new TreeMap<>(firstMap);
            } else if (firstMap == null && secondMap != null) {
                next = new TreeMap<>(secondMap);
            } else if (firstMap != null && secondMap != null) {
                next = new TreeMap<>(firstMap);
                for (Map.Entry<T, Boolean> entry : secondMap.entrySet()) {
                    next.put(entry.getKey(),
                            CreatureFilterQuery.combineOr(entry.getValue(), firstMap.get(entry.getKey())));
                }
            }
            return next;
        }

        private static NavigableMap<Attributes, MultiRollResult> combineChecksAnd(
                Map<Attributes, MultiRollResult> firstMap, Map<Attributes, MultiRollResult> secondMap) {
            TreeMap<Attributes, MultiRollResult> next = null;
            if (firstMap == null && secondMap == null) {
                return next;
            } else if (firstMap != null && secondMap == null) {
                next = new TreeMap<>(firstMap);
            } else if (firstMap == null && secondMap != null) {
                next = new TreeMap<>(secondMap);
            } else if (firstMap != null && secondMap != null) {
                next = new TreeMap<>(firstMap);
                for (Map.Entry<Attributes, MultiRollResult> entry : secondMap.entrySet()) {
                    next.put(entry.getKey(), MultiRollResult.advantage(entry.getValue(), firstMap.get(entry.getKey())));
                }
            }
            return next;
        }

        private static NavigableMap<Attributes, MultiRollResult> combineChecksOr(
                Map<Attributes, MultiRollResult> firstMap, Map<Attributes, MultiRollResult> secondMap) {
            TreeMap<Attributes, MultiRollResult> next = null;
            if (firstMap == null && secondMap == null) {
                return next;
            } else if (firstMap != null && secondMap == null) {
                next = new TreeMap<>(firstMap);
            } else if (firstMap == null && secondMap != null) {
                next = new TreeMap<>(secondMap);
            } else if (firstMap != null && secondMap != null) {
                next = new TreeMap<>(firstMap);
                for (Map.Entry<Attributes, MultiRollResult> entry : secondMap.entrySet()) {
                    next.put(entry.getKey(),
                            MultiRollResult.disadvantage(entry.getValue(), firstMap.get(entry.getKey())));
                }
            }
            return next;
        }

        public CreatureFilterQuery and(CreatureFilterQuery other) {
            if (other == null) {
                return this;
            } else if (other == this) {
                return this;
            }
            ItemFilterQuery hasItemAnd = null;
            if (this.hasItem != null) {
                hasItemAnd = this.hasItem.and(other.hasItem);
            } else if (other.hasItem != null) {
                hasItemAnd = other.hasItem.and(other.hasItem);
            }
            ItemFilterQuery equippedAnd = null;
            if (this.hasItemEquipped != null) {
                equippedAnd = this.hasItemEquipped.and(other.hasItemEquipped);
            } else if (other.hasItemEquipped != null) {
                equippedAnd = other.hasItemEquipped.and(other.hasItemEquipped);
            }

            return new CreatureFilterQuery(CreatureFilterQuery.combineAnd(this.name, other.name),
                    CreatureFilterQuery.combineRegexesAnd(this.nameRegexes, other.nameRegexes),
                    CreatureFilterQuery.combineMapsAnd(this.factions, other.factions),
                    CreatureFilterQuery.combineMapsAnd(this.vocations, other.vocations),
                    CreatureFilterQuery.combineMapsAnd(this.classNames, other.classNames),
                    CreatureFilterQuery.combineAnd(this.isBattling, other.isBattling), hasItemAnd, equippedAnd,
                    CreatureFilterQuery.combineChecksAnd(this.beatsCheck, other.beatsCheck));
        }

        public CreatureFilterQuery or(CreatureFilterQuery other) {
            if (other == null) {
                return this;
            } else if (other == this) {
                return this;
            }

            ItemFilterQuery hasItemOr = null;
            if (this.hasItem != null) {
                hasItemOr = this.hasItem.or(other.hasItem);
            } else if (other.hasItem != null) {
                hasItemOr = other.hasItem.or(other.hasItem);
            }
            ItemFilterQuery equippedOr = null;
            if (this.hasItemEquipped != null) {
                equippedOr = this.hasItemEquipped.or(other.hasItemEquipped);
            } else if (other.hasItemEquipped != null) {
                equippedOr = other.hasItemEquipped.or(other.hasItemEquipped);
            }
            return new CreatureFilterQuery(CreatureFilterQuery.combineAnd(this.name, other.name),
                    CreatureFilterQuery.combineRegexesOr(this.nameRegexes, other.nameRegexes),
                    CreatureFilterQuery.combineMapsOr(this.factions, other.factions),
                    CreatureFilterQuery.combineMapsOr(this.vocations, other.vocations),
                    CreatureFilterQuery.combineMapsOr(this.classNames, other.classNames),
                    CreatureFilterQuery.combineOr(this.isBattling, other.isBattling), hasItemOr, equippedOr,
                    CreatureFilterQuery.combineChecksOr(this.beatsCheck, other.beatsCheck));
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, nameRegexes, factions, vocations, classNames, isBattling, hasItem,
                    hasItemEquipped);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof CreatureFilterQuery))
                return false;
            CreatureFilterQuery other = (CreatureFilterQuery) obj;
            return Objects.equals(name, other.name) && Objects.equals(nameRegexes, other.nameRegexes)
                    && Objects.equals(factions, other.factions) && Objects.equals(vocations, other.vocations)
                    && Objects.equals(classNames, other.classNames) && Objects.equals(isBattling, other.isBattling)
                    && Objects.equals(hasItem, other.hasItem) && Objects.equals(hasItemEquipped, other.hasItemEquipped);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("CreatureFilterQuery [name=").append(name).append(", nameRegexes=").append(nameRegexes)
                    .append(", factions=").append(factions).append(", vocations=").append(vocations)
                    .append(", classNames=").append(classNames).append(", isBattling=").append(isBattling)
                    .append(", hasItem=").append(hasItem).append(", hasItemEquipped=").append(hasItemEquipped)
                    .append("]");
            return builder.toString();
        }

        private boolean testNames(final ICreature creature) {
            if (creature == null || this.name == null) {
                return false;
            }
            if (this.name != null) {
                return this.name.equalsIgnoreCase(creature.getName());
            } else if (this.nameRegexes != null) {
                for (final String regex : this.nameRegexes) {
                    if (regex == null) {
                        continue;
                    }
                    if (!Pattern.matches(regex, creature.getName())) {
                        return false;
                    }
                }
            }
            return true;
        }

        private <T> boolean testMap(final T creatureAttribute, final Map<T, Boolean> mapping) {
            if (mapping == null) {
                return false;
            }
            for (final Map.Entry<T, Boolean> entry : mapping.entrySet()) {
                Boolean value = entry.getValue();
                if (value == null) {
                    continue;
                }
                if (creatureAttribute == null) {
                    return false;
                }
                if (value && !creatureAttribute.equals(entry.getKey())) {
                    return false;
                } else if (!value && creatureAttribute.equals(entry.getKey())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean test(final ICreature creature) {
            if (creature == null) {
                return false;
            }
            if (!this.testNames(creature)) {
                return false;
            }
            if (!this.testMap(creature.getVocation() != null ? creature.getVocation().getVocationName() : null,
                    this.vocations)) {
                return false;
            }
            if (!this.testMap(creature.getFaction(), this.factions)) {
                return false;
            }
            if (!this.testMap(creature.getClass().getName(), this.classNames)) {
                return false;
            }
            if (isBattling != null && !isBattling.equals(creature.isInBattle())) {
                return false;
            }
            if (hasItem != null && creature.filterItems(hasItem).isEmpty()) {
                return false;
            }
            if (hasItemEquipped != null
                    && creature.getEquipmentSlots().values().stream().filter(hasItemEquipped).findAny().isEmpty()) {
                return false;
            }
            return true;

        }

    }

    public default void acceptCreatureVisitor(CreatureVisitor visitor) {
        for (final ICreature creature : this.getCreatures()) {
            if (creature == null) {
                continue;
            }
            creature.acceptCreatureVisitor(visitor);
        }
    }

    public default Collection<ICreature> filterCreatures(CreatureFilterQuery query) {
        final Collection<ICreature> retrieved = this.getCreatures();
        if (query == null || retrieved == null) {
            return retrieved;
        }
        Supplier<Collection<ICreature>> sortSupplier = () -> new TreeSet<ICreature>();
        return Collections.unmodifiableCollection(
                retrieved.stream().filter(query).collect(Collectors.toCollection(sortSupplier)));
    }

    public default Optional<ICreature> getCreature(String name) {
        return this.filterCreatures(name, null, null, null, null, null).stream().findFirst();
    }

    public default Collection<ICreature> getCreaturesLike(String name) {
        return this.filterCreatures(name, null, null, null, null, null);
    }

    public default Collection<ICreature> getPlayers() {
        return this.filterCreatures(null, null, null, null, Player.class, null);
    }

    public default Optional<Player> getPlayer(UserID id) {
        Optional<ICreature> asCreature = this.getPlayers().stream().filter(
                creature -> creature != null && creature instanceof Player && ((Player) creature).getId().equals(id))
                .findFirst();
        if (asCreature.isPresent()) {
            return Optional.of((Player) asCreature.get());
        }
        return Optional.empty();
    }

    public default Optional<Player> getPlayer(String name) {
        Optional<ICreature> asCreature = this.filterCreatures(name, null, null, null, Player.class, null).stream()
                .findFirst();
        if (asCreature.isPresent()) {
            return Optional.of((Player) asCreature.get());
        }
        return Optional.empty();
    }

    public default boolean hasCreature(String name, boolean asRegex) {
        return this.filterCreatures(asRegex ? null : name, asRegex ? List.of(name) : null, null, null, null, null)
                .size() > 0;
    }

    public default boolean hasCreature(String name) {
        return this.hasCreature(name, false);
    }

    public default boolean hasCreature(ICreature creature) {
        return this.getCreatures().contains(creature);
    }

    @Override
    default Collection<GameEventProcessor> getGameEventProcessors() {
        TreeSet<GameEventProcessor> messengers = new TreeSet<>(GameEventProcessor.getComparator());
        messengers.addAll(this.getCreatures().stream().filter(creature -> creature != null)
                .map(creature -> (GameEventProcessor) creature).toList());
        return messengers;
    }

}
