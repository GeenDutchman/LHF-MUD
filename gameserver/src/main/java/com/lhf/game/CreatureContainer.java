package com.lhf.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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
        private String name;
        private ArrayList<String> nameRegexes = new ArrayList<>();
        private TreeMap<CreatureFaction, Boolean> factions = new TreeMap<>();
        private TreeMap<VocationName, Boolean> vocations = new TreeMap<>();
        private TreeMap<String, Boolean> classNames = new TreeMap<>(); // TODO: replace with SPECIES enum?
        private Boolean isBattling;
        private ItemFilterQuery hasItem;
        private ItemFilterQuery hasItemEquipped;
        // TODO: filter by health percentage, health number, check result vs DC

        public CreatureFilterQuery() {
        }

        public CreatureFilterQuery(CreatureFilterQuery copy) {
            if (copy != null) {
                this.name = copy.name;
                if (copy.nameRegexes != null) {
                    this.nameRegexes.addAll(copy.nameRegexes);
                }
                if (copy.factions != null) {
                    this.factions.putAll(copy.factions);
                }
                if (copy.classNames != null) {
                    this.classNames.putAll(copy.classNames);
                }
                this.isBattling = copy.isBattling;
                this.hasItem = copy.hasItem != null ? new ItemFilterQuery(copy.hasItem) : null;
                this.hasItemEquipped = copy.hasItemEquipped != null ? new ItemFilterQuery(copy.hasItemEquipped) : null;
            }
        }

        private <T> T combineAnd(T value1, T value2) {
            if (value1 == null)
                return value2;
            if (value2 == null)
                return value1;
            return value1.equals(value2) ? value1 : null; // Both must match, otherwise null
        }

        // Helper methods to merge attributes according to OR logic
        private <T> T combineOr(T value1, T value2) {
            return (value1 != null) ? value1 : value2; // At least one must be non-null
        }

        private ArrayList<String> combineRegexesAnd(List<String> firstRegex, List<String> secondRegex) {
            ArrayList<String> list = new ArrayList<>();
            if (firstRegex != null) {
                list.addAll(firstRegex);
            }
            if (secondRegex != null) {
                list.addAll(secondRegex);
            }
            return list;
        }

        private ArrayList<String> combineRegexesOr(List<String> firstRegex, List<String> secondRegex) {
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

        private <T> TreeMap<T, Boolean> combineMapsAnd(Map<T, Boolean> firstMap, Map<T, Boolean> secondMap) {
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
                    next.put(entry.getKey(), this.combineAnd(entry.getValue(), firstMap.get(entry.getKey())));
                }
            }
            return next;
        }

        private <T> TreeMap<T, Boolean> combineMapsOr(Map<T, Boolean> firstMap, Map<T, Boolean> secondMap) {
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
                    next.put(entry.getKey(), this.combineOr(entry.getValue(), firstMap.get(entry.getKey())));
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
            CreatureFilterQuery next = new CreatureFilterQuery();
            next.setName(this.combineAnd(this.name, other.name));
            next.nameRegexes = this.combineRegexesAnd(this.nameRegexes, other.nameRegexes);
            next.factions = this.combineMapsAnd(this.factions, other.factions);
            next.vocations = this.combineMapsAnd(this.vocations, other.vocations);
            next.classNames = this.combineMapsAnd(this.classNames, other.classNames);
            next.isBattling = this.combineAnd(this.isBattling, other.isBattling);
            if (this.hasItem != null) {
                next.hasItem = this.hasItem.and(other.hasItem);
            } else if (other.hasItem != null) {
                next.hasItem = other.hasItem.and(other.hasItem);
            }
            if (this.hasItemEquipped != null) {
                next.hasItemEquipped = this.hasItemEquipped.and(other.hasItemEquipped);
            } else if (other.hasItemEquipped != null) {
                next.hasItemEquipped = other.hasItemEquipped.and(other.hasItemEquipped);
            }
            return next;
        }

        public CreatureFilterQuery or(CreatureFilterQuery other) {
            if (other == null) {
                return this;
            } else if (other == this) {
                return this;
            }
            CreatureFilterQuery next = new CreatureFilterQuery();
            next.setName(this.combineOr(this.name, other.name));
            next.nameRegexes = this.combineRegexesOr(this.nameRegexes, other.nameRegexes);
            next.factions = this.combineMapsOr(this.factions, other.factions);
            next.vocations = this.combineMapsOr(this.vocations, other.vocations);
            next.classNames = this.combineMapsOr(this.classNames, other.classNames);
            next.isBattling = this.combineOr(this.isBattling, other.isBattling);
            if (this.hasItem != null) {
                next.hasItem = this.hasItem.or(other.hasItem);
            } else if (other.hasItem != null) {
                next.hasItem = other.hasItem.or(other.hasItem);
            }
            if (this.hasItemEquipped != null) {
                next.hasItemEquipped = this.hasItemEquipped.or(other.hasItemEquipped);
            } else if (other.hasItemEquipped != null) {
                next.hasItemEquipped = other.hasItemEquipped.or(other.hasItemEquipped);
            }
            return next;
        }

        public CreatureFilterQuery setName(String name) {
            this.name = name;
            if (name != null && nameRegexes != null) {
                this.nameRegexes.clear();
            }
            return this;
        }

        public CreatureFilterQuery addNameRegex(String regex) {
            if (this.nameRegexes == null) {
                this.nameRegexes = new ArrayList<>();
            }
            if (regex != null) {
                this.nameRegexes.add(regex);
                this.name = null;
            }
            return this;
        }

        public CreatureFilterQuery setNameRegexes(List<String> regexes) {
            if (this.nameRegexes == null) {
                this.nameRegexes = new ArrayList<>();
            }
            if (regexes == null || regexes.isEmpty()) {
                return this;
            }
            this.nameRegexes = new ArrayList<>(regexes);
            this.name = null;
            return this;
        }

        public CreatureFilterQuery needFaction(CreatureFaction faction) {
            if (faction == null) {
                return this;
            }
            if (factions == null) {
                this.factions = new TreeMap<>();
            }
            this.factions.put(faction, true);
            return this;
        }

        public CreatureFilterQuery forbiddenFaction(CreatureFaction faction) {
            if (faction == null) {
                return this;
            }
            if (factions == null) {
                this.factions = new TreeMap<>();
            }
            this.factions.put(faction, false);
            return this;
        }

        public CreatureFilterQuery clearFactions() {
            if (factions != null) {
                this.factions.clear();
            }
            return this;
        }

        public CreatureFilterQuery needVocation(VocationName name) {
            if (name == null) {
                return this;
            }
            if (this.vocations == null) {
                this.vocations = new TreeMap<>();
            }
            this.vocations.put(name, true);
            return this;
        }

        public CreatureFilterQuery forbiddenVocation(VocationName name) {
            if (name == null) {
                return this;
            }
            if (this.vocations == null) {
                this.vocations = new TreeMap<>();
            }
            this.vocations.put(name, false);
            return this;
        }

        public CreatureFilterQuery clearVocations() {
            if (vocations != null) {
                this.vocations.clear();
            }
            return this;
        }

        public CreatureFilterQuery needClassname(Class<? extends ICreature> clazz) {
            if (clazz == null) {
                return this;
            }
            if (this.classNames == null) {
                this.classNames = new TreeMap<>();
            }
            this.classNames.put(clazz.getName(), true);
            return this;
        }

        public CreatureFilterQuery needClassname(ICreature creature) {
            if (creature == null) {
                return this;
            }
            if (this.classNames == null) {
                this.classNames = new TreeMap<>();
            }
            this.classNames.put(creature.getClass().getName(), true);
            return this;
        }

        public CreatureFilterQuery forbiddenClassname(Class<? extends ICreature> clazz) {
            if (clazz == null) {
                return this;
            }
            if (this.classNames == null) {
                this.classNames = new TreeMap<>();
            }
            this.classNames.put(clazz.getName(), false);
            return this;
        }

        public CreatureFilterQuery forbiddenClassname(ICreature creature) {
            if (creature == null) {
                return this;
            }
            if (this.classNames == null) {
                this.classNames = new TreeMap<>();
            }
            this.classNames.put(creature.getClass().getName(), false);
            return this;
        }

        public CreatureFilterQuery clearClassnames() {
            if (classNames != null) {
                this.classNames.clear();
            }
            return this;
        }

        public CreatureFilterQuery battlingRequired() {
            this.isBattling = true;
            return this;
        }

        public CreatureFilterQuery battlingForbidden() {
            this.isBattling = false;
            return this;
        }

        public CreatureFilterQuery battlingNotMatter() {
            this.isBattling = null;
            return this;
        }

        public CreatureFilterQuery setBattling(Boolean battling) {
            this.isBattling = battling;
            return this;
        }

        public CreatureFilterQuery mustHaveItemLike(ItemFilterQuery query) {
            this.hasItem = query;
            return this;
        }

        public CreatureFilterQuery mustHaveItemEquippedLike(ItemFilterQuery query) {
            this.hasItemEquipped = query;
            return this;
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
