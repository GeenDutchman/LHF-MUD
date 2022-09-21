package com.lhf.game.map;

import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.server.interfaces.NotNull;

public class DMRoomEffectSource extends RoomEffectSource {

    protected Set<String> usernamesToEnsoul;
    protected Set<String> namesToSendOff;

    public DMRoomEffectSource(String name, EffectPersistence persistence, EffectResistance resistance,
            String description) {
        super(name, persistence, resistance, description);
        this.usernamesToEnsoul = new TreeSet<>();
        this.namesToSendOff = new TreeSet<>();
    }

    public DMRoomEffectSource(@NotNull DMRoomEffectSource other) {
        super(other.name, other.persistence, other.resistance, other.description);
        this.usernamesToEnsoul = new TreeSet<>(other.usernamesToEnsoul);
        this.namesToSendOff = new TreeSet<>(other.namesToSendOff);
    }

    public DMRoomEffectSource(@NotNull RoomEffectSource sub) {
        super(sub);
    }

    public DMRoomEffectSource addUsernameToEnsoul(String username) {
        this.usernamesToEnsoul.add(username);
        return this;
    }

    public Set<String> getUsernamesToEnsoul() {
        return usernamesToEnsoul;
    }

    public DMRoomEffectSource addNameToSendOff(String name) {
        this.namesToSendOff.add(name);
        return this;
    }

    public Set<String> getNamesToSendOff() {
        return namesToSendOff;
    }

    public DMRoomEffectSource addName(String username) {
        this.usernamesToEnsoul.add(username);
        this.namesToSendOff.add(name);
        return this;
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder(super.printDescription());
        if (this.usernamesToEnsoul.size() > 0) {
            sb.append("Users it will ensoul: \r\n");
            StringJoiner sj = new StringJoiner(" and ");
            for (String name : this.usernamesToEnsoul) {
                sj.add(name);
            }
            sb.append(sj.toString()).append("\r\n");
        }
        if (this.namesToSendOff.size() > 0) {
            sb.append("Creatures it will send off: \r\n");
            StringJoiner sj = new StringJoiner(" and ");
            for (String name : this.namesToSendOff) {
                sj.add(name);
            }
            sb.append(sj.toString()).append("\r\n");
        }
        return sb.toString();
    }
}
