package com.lhf.game;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.lhf.Examinable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.ClientMessengerHub;
import com.lhf.server.client.user.UserID;

public interface CreatureContainer extends Examinable, ClientMessengerHub {
    /**
     * This returns an immutable Collection of Creatures
     * 
     * @return Immutable Collection
     */
    public abstract Collection<Creature> getCreatures();

    public abstract boolean onCreatureDeath(Creature creature);

    public abstract boolean addCreature(Creature creature);

    public abstract Optional<Creature> removeCreature(String name);

    public abstract boolean removeCreature(Creature creature);

    public abstract boolean addPlayer(Player player);

    public abstract Optional<Player> removePlayer(String name);

    public abstract Optional<Player> removePlayer(UserID id);

    public abstract boolean removePlayer(Player player);

    public enum Filters {
        NAME, FACTION, VOCATION, TYPE, BATTLING;
    }

    public default Collection<Creature> filterCreatures(EnumSet<Filters> filters, String name, Integer nameRegexLen,
            CreatureFaction faction, VocationName vocation, Class<? extends Creature> clazz, Boolean isBattling) {
        Collection<Creature> retrieved = this.getCreatures();
        Supplier<Collection<Creature>> sortSupplier = () -> new TreeSet<Creature>();
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

    public default Optional<Creature> getCreature(String name) {
        return this.filterCreatures(EnumSet.of(Filters.NAME), name, null, null, null, null, null).stream().findFirst();
    }

    public default Collection<Creature> getCreaturesLike(String name) {
        return this.filterCreatures(EnumSet.of(Filters.NAME), name, null, null, null, null, null);
    }

    public default Collection<Creature> getPlayers() {
        return this.filterCreatures(EnumSet.of(Filters.TYPE), null, null, null, null, Player.class, null);
    }

    public default Optional<Player> getPlayer(UserID id) {
        Optional<Creature> asCreature = this.getPlayers().stream().filter(
                creature -> creature != null && creature instanceof Player && ((Player) creature).getId().equals(id))
                .findFirst();
        if (asCreature.isPresent()) {
            return Optional.of((Player) asCreature.get());
        }
        return Optional.empty();
    }

    public default Optional<Player> getPlayer(String name) {
        Optional<Creature> asCreature = this
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

    public default boolean hasCreature(Creature creature) {
        return this.getCreatures().contains(creature);
    }

    @Override
    default Collection<ClientMessenger> getClientMessengers() {
        return new TreeSet<ClientMessenger>(this.getCreatures().stream()
                .filter(creature -> creature != null)
                .map(creature -> (ClientMessenger) creature).toList());
    }

}
