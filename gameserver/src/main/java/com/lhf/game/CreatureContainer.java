package com.lhf.game;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.lhf.Examinable;
import com.lhf.game.creature.CreatureVisitor;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.vocation.Vocation;
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

    public default Collection<ICreature> filterCreatures(EnumSet<CreatureFilters> filters, String name,
            Integer nameRegexLen,
            CreatureFaction faction, VocationName vocation, Class<? extends ICreature> clazz, Boolean isBattling) {
        Collection<ICreature> retrieved = this.getCreatures();
        Supplier<Collection<ICreature>> sortSupplier = () -> new TreeSet<ICreature>();
        return Collections.unmodifiableCollection(retrieved.stream()
                .filter(creature -> creature != null)
                .filter(creature -> !filters.contains(CreatureFilters.NAME)
                        || (name != null && (nameRegexLen != null ? creature.CheckNameRegex(name, nameRegexLen)
                                : creature.checkName(name))))
                .filter(creature -> !filters.contains(CreatureFilters.FACTION)
                        || (faction != null && faction.equals(creature.getFaction())))
                .filter(creature -> {
                    if (!filters.contains(CreatureFilters.VOCATION)) {
                        return true;
                    }
                    Vocation cVocation = creature.getVocation();
                    return cVocation == null ? vocation == null
                            : (vocation != null && vocation.equals(cVocation.getVocationName()));
                })
                .filter(creature -> !filters.contains(CreatureFilters.TYPE)
                        || (clazz != null && clazz.isInstance(creature)))
                .filter(creature -> !filters.contains(CreatureFilters.BATTLING)
                        || (isBattling != null && isBattling == creature.isInBattle()))
                .collect(Collectors.toCollection(sortSupplier)));
    }

    public static class CreatureFilterQuery {
        public EnumSet<CreatureFilters> filters = EnumSet.noneOf(CreatureFilters.class);
        public String name;
        public Integer nameRegexLen;
        public CreatureFaction faction;
        public VocationName vocation;
        public transient Class<? extends ICreature> clazz;
        public Boolean isBattling;

        @Override
        public int hashCode() {
            return Objects.hash(filters, name, nameRegexLen, faction, vocation, clazz, isBattling);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof CreatureFilterQuery))
                return false;
            CreatureFilterQuery other = (CreatureFilterQuery) obj;
            return Objects.equals(filters, other.filters) && Objects.equals(name, other.name)
                    && Objects.equals(nameRegexLen, other.nameRegexLen) && faction == other.faction
                    && vocation == other.vocation && Objects.equals(clazz, other.clazz)
                    && Objects.equals(isBattling, other.isBattling);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("CreatureFilterQuery [filters=").append(filters).append(", name=").append(name)
                    .append(", nameRegexLen=").append(nameRegexLen).append(", faction=").append(faction)
                    .append(", vocation=").append(vocation).append(", clazz=").append(clazz).append(", isBattling=")
                    .append(isBattling).append("]");
            return builder.toString();
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
        if (query == null) {
            return this.filterCreatures(EnumSet.noneOf(CreatureFilters.class), null, null, null, null, null, null);
        }
        return this.filterCreatures(query.filters, query.name, query.nameRegexLen, query.faction, query.vocation,
                query.clazz, query.isBattling);
    }

    public default Optional<ICreature> getCreature(String name) {
        return this.filterCreatures(EnumSet.of(CreatureFilters.NAME), name, null, null, null, null, null).stream()
                .findFirst();
    }

    public default Collection<ICreature> getCreaturesLike(String name) {
        return this.filterCreatures(EnumSet.of(CreatureFilters.NAME), name, null, null, null, null, null);
    }

    public default Collection<ICreature> getPlayers() {
        return this.filterCreatures(EnumSet.of(CreatureFilters.TYPE), null, null, null, null, Player.class, null);
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
        Optional<ICreature> asCreature = this
                .filterCreatures(EnumSet.of(CreatureFilters.TYPE, CreatureFilters.NAME), name, null, null, null,
                        Player.class, null)
                .stream().findFirst();
        if (asCreature.isPresent()) {
            return Optional.of((Player) asCreature.get());
        }
        return Optional.empty();
    }

    public default boolean hasCreature(String name, Integer minimumLength) {
        return this.filterCreatures(EnumSet.of(CreatureFilters.NAME), name, minimumLength, null, null, null, null)
                .size() > 0;
    }

    public default boolean hasCreature(String name) {
        return this.hasCreature(name, 3);
    }

    public default boolean hasCreature(ICreature creature) {
        return this.getCreatures().contains(creature);
    }

    @Override
    default Collection<GameEventProcessor> getGameEventProcessors() {
        TreeSet<GameEventProcessor> messengers = new TreeSet<>(GameEventProcessor.getComparator());
        messengers.addAll(this.getCreatures().stream()
                .filter(creature -> creature != null)
                .map(creature -> (GameEventProcessor) creature).toList());
        return messengers;
    }

}
