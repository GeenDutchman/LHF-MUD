package com.lhf.game.magic;

import java.util.Set;

import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.map.DMRoomEffectSource;

public class DMRoomTargetingSpellEntry extends RoomTargetingSpellEntry {
    protected final boolean ensoulsUsers;
    protected final boolean sendsOffPlayers;

    public DMRoomTargetingSpellEntry(Integer level, String name, String invocation,
            Set<DMRoomEffectSource> effectSources,
            Set<VocationName> allowed, String description, boolean banishesItems, boolean banishesCreatures,
            boolean ensoulsUsers, boolean sendsOffPlayers) {
        super(level, name, invocation, effectSources, allowed, description, banishesItems, banishesCreatures);
        this.ensoulsUsers = ensoulsUsers;
        this.sendsOffPlayers = sendsOffPlayers;
    }

    public DMRoomTargetingSpellEntry(Integer level, String name, Set<DMRoomEffectSource> effectSources,
            Set<VocationName> allowed, String description, boolean banishesItems, boolean banishesCreatures,
            boolean ensoulsUsers, boolean sendsOffPlayers) {
        super(level, name, effectSources, allowed, description, banishesItems, banishesCreatures);
        this.ensoulsUsers = ensoulsUsers;
        this.sendsOffPlayers = sendsOffPlayers;
    }

    public boolean isEnsoulsUsers() {
        return ensoulsUsers;
    }

    public boolean isSendsOffPlayers() {
        return sendsOffPlayers;
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder(super.printDescription());
        if (this.ensoulsUsers) {
            sb.append("This spell will ensoul users. ").append("\r\n");
        }
        if (this.sendsOffPlayers) {
            sb.append("This spell will send off players. ").append("\r\n");
        }
        return sb.toString();
    }

}
