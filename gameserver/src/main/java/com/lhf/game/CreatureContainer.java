package com.lhf.game;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.lhf.Examinable;
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

    public enum Filters {
        NAME, FACTION, VOCATION, TYPE, BATTLING;
    }

    public default Collection<ICreature> filterCreatures(EnumSet<Filters> filters, String name, Integer nameRegexLen,
            CreatureFaction faction, VocationName vocation, Class<? extends ICreature> clazz, Boolean isBattling) {
        Collection<ICreature> retrieved = this.getCreatures();
        Supplier<Collection<ICreature>> sortSupplier = () -> new TreeSet<ICreature>();
        return Collections.unmodifiableCollection(retrieved.stream()
                .filter(creature -> creature != null)
                .filter(creature -> !filters.contains(Filters.NAME)
                        || (name != null && (nameRegexLen != null ? creature.CheckNameRegex(name, nameRegexLen)
                                : creature.checkName(name))))
                .filter(creature -> !filters.contains(Filters.FACTION)
                        || (faction != null && faction.equals(creature.getFaction())))
                .filter(creature -> {
                    if (!filters.contains(Filters.VOCATION)) {
                        return true;
                    }
                    Vocation cVocation = creature.getVocation();
                    return cVocation == null ? vocation == null
                            : (vocation != null && vocation.equals(cVocation.getVocationName()));
                })
                .filter(creature -> !filters.contains(Filters.TYPE) || (clazz != null && clazz.isInstance(creature)))
                .filter(creature -> !filters.contains(Filters.BATTLING)
                        || (isBattling != null && isBattling == creature.isInBattle()))
                .collect(Collectors.toCollection(sortSupplier)));
    }

    public default Optional<ICreature> getCreature(String name) {
        return this.filterCreatures(EnumSet.of(Filters.NAME), name, null, null, null, null, null).stream().findFirst();
    }

    public default Collection<ICreature> getCreaturesLike(String name) {
        return this.filterCreatures(EnumSet.of(Filters.NAME), name, null, null, null, null, null);
    }

    public default Collection<ICreature> getPlayers() {
        return this.filterCreatures(EnumSet.of(Filters.TYPE), null, null, null, null, Player.class, null);
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
                .filterCreatures(EnumSet.of(Filters.TYPE, Filters.NAME), name, null, null, null, Player.class, null)
                .stream().findFirst();
        if (asCreature.isPresent()) {
            return Optional.of((Player) asCreature.get());
        }
        return Optional.empty();
    }

    public default boolean hasCreature(String name, Integer minimumLength) {
        return this.filterCreatures(EnumSet.of(Filters.NAME), name, minimumLength, null, null, null, null).size() > 0;
    }

    public default boolean hasCreature(String name) {
        return this.hasCreature(name, 3);
    }

    public default boolean hasCreature(ICreature creature) {
        return this.getCreatures().contains(creature);
    }

    @Override
    default Collection<GameEventProcessor> getClientMessengers() {
        TreeSet<GameEventProcessor> messengers = new TreeSet<>(GameEventProcessor.getComparator());
        messengers.addAll(this.getCreatures().stream()
                .filter(creature -> creature != null)
                .map(creature -> (GameEventProcessor) creature).toList());
        return messengers;
    }

}
