package com.lhf.game.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventProcessorHub;
import com.lhf.messages.events.FactionRenegadeJoined;

public enum CreatureFaction {
    PLAYER, MONSTER, NPC, RENEGADE, PET, SWARM;

    public static CreatureFaction getFaction(String value) {
        for (CreatureFaction faction : values()) {
            if (faction.toString().equalsIgnoreCase(value)) {
                return faction;
            }
        }
        return null;
    }

    public static Boolean isCreatureFaction(String value) {
        return CreatureFaction.getFaction(value) != null;
    }

    public static Set<CreatureFaction> competeSet(CreatureFaction aFaction) {
        if (aFaction == null || RENEGADE.equals(aFaction)) {
            return Collections.unmodifiableSet(EnumSet.allOf(CreatureFaction.class));
        }
        switch (aFaction) {
            case MONSTER:
                return Collections.unmodifiableSet(EnumSet.of(PLAYER, RENEGADE, SWARM));
            case NPC:
                return Collections.unmodifiableSet(EnumSet.of(MONSTER, RENEGADE, SWARM));
            case PLAYER:
                return Collections.unmodifiableSet(EnumSet.of(MONSTER, RENEGADE, SWARM));
            case RENEGADE:
                return Collections.unmodifiableSet(EnumSet.allOf(CreatureFaction.class));
            case PET:
                return Collections.unmodifiableSet(EnumSet.noneOf(CreatureFaction.class));
            case SWARM:
                return Collections.unmodifiableSet(EnumSet.of(PLAYER, MONSTER, NPC, RENEGADE, PET));
            default:
                return Collections.unmodifiableSet(EnumSet.allOf(CreatureFaction.class));
        }
    }

    public Set<CreatureFaction> competeSet() {
        return CreatureFaction.competeSet(this);
    }

    public static Set<CreatureFaction> allySet(CreatureFaction aFaction) {
        if (aFaction == null || RENEGADE.equals(aFaction)) {
            return Collections.unmodifiableSet(EnumSet.noneOf(CreatureFaction.class));
        }

        switch (aFaction) {
            case MONSTER:
                return Collections.unmodifiableSet(EnumSet.of(MONSTER, PET));
            case NPC:
                return Collections.unmodifiableSet(EnumSet.of(PLAYER, NPC, PET));
            case PLAYER:
                return Collections.unmodifiableSet(EnumSet.of(PLAYER, NPC, PET));
            case RENEGADE:
                return Collections.unmodifiableSet(EnumSet.noneOf(CreatureFaction.class));
            case PET:
                return Collections.unmodifiableSet(EnumSet.of(PLAYER, MONSTER, NPC, PET));
            case SWARM:
                return Collections.unmodifiableSet(EnumSet.of(SWARM));
            default:
                return Collections.unmodifiableSet(EnumSet.noneOf(CreatureFaction.class));

        }
    }

    public Set<CreatureFaction> allySet() {
        return CreatureFaction.allySet(this);
    }

    public boolean competing(CreatureFaction other) {
        if (other == null || RENEGADE.equals(other)) {
            return true;
        }
        return CreatureFaction.competeSet(this).contains(other);
    }

    public boolean allied(CreatureFaction other) {
        if (other == null || RENEGADE.equals(other)) {
            return false;
        }
        return CreatureFaction.allySet(this).contains(other);
    }

    public static void handleTurnRenegade(ICreature turned, GameEventProcessorHub hub) {
        if (turned == null) {
            return;
        }
        if (!RENEGADE.equals(turned.getFaction())) {
            turned.setFaction(RENEGADE);
            FactionRenegadeJoined.Builder builder = FactionRenegadeJoined.getBuilder(turned);
            ICreature.eventAccepter.accept(turned, builder.setNotBroadcast().Build());
            builder.setBroacast();
            if (hub != null) {
                hub.announce(builder.Build(), turned);
            }
        }
    }

    public static boolean checkAndHandleTurnRenegade(ICreature attacker, ICreature target, GameEventProcessorHub hub) {
        if (!CreatureFaction.RENEGADE.equals(target.getFaction())
                && !CreatureFaction.RENEGADE.equals(attacker.getFaction())
                && attacker.getFaction() != null
                && attacker.getFaction().allied(target.getFaction())) {
            CreatureFaction.handleTurnRenegade(attacker, hub);
            return true;
        }
        return false;
    }

    public static boolean hasCompetitors(final Set<CreatureFaction> factions) {
        if (factions == null || factions.isEmpty() || factions.size() <= 1) {
            return false;
        }
        if (factions.contains(RENEGADE)) {
            return true;
        }
        if (factions.contains(SWARM)) {
            return true;
        }
        if (factions.contains(MONSTER) && (factions.contains(PLAYER) || factions.contains(NPC))) {
            return true;
        }
        return false;
    }

}
