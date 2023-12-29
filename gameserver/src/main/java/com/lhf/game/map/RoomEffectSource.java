package com.lhf.game.map;

import java.util.HashSet;
import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.Monster.MonsterBuilder;
import com.lhf.game.creature.NonPlayerCharacter.NPCBuilder;

public class RoomEffectSource extends EntityEffectSource {

    // TODO: implement banishment

    protected Set<NPCBuilder> npcsToSummon;
    protected Set<MonsterBuilder> monstersToSummon;

    public RoomEffectSource(RoomEffectSource other) {
        super(other.name, other.persistence, other.resistance, other.description);
        this.npcsToSummon = new HashSet<>();
        this.monstersToSummon = new HashSet<>();
    }

    public RoomEffectSource(String name, EffectPersistence persistence, EffectResistance resistance,
            String description) {
        super(name, persistence, resistance, description);
        this.npcsToSummon = new HashSet<>();
        this.monstersToSummon = new HashSet<>();
    }

    public RoomEffectSource addCreatureToSummon(MonsterBuilder toSummon) {
        this.monstersToSummon.add(toSummon);
        return this;
    }

    public RoomEffectSource addCreatureToSummon(NPCBuilder toSummon) {
        this.npcsToSummon.add(toSummon);
        return this;
    }

    public Set<NPCBuilder> getNpcsToSummon() {
        return npcsToSummon;
    }

    public Set<MonsterBuilder> getMonstersToSummon() {
        return monstersToSummon;
    }

    @Override
    public boolean isOffensive() {
        // can be offensive when banished
        return false;
    }

    @Override
    public int aiScore() {
        return (this.monstersToSummon != null ? this.monstersToSummon.size() : 0) +
                (this.npcsToSummon != null ? this.npcsToSummon.size() : 0);
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder(super.printDescription());
        if (this.monstersToSummon != null && this.monstersToSummon.size() > 0) {
            sb.append("\r\nWill summon the following Monsters:\r\n");
            for (final MonsterBuilder builder : this.monstersToSummon) {
                sb.append(builder.toString());
            }
        }
        if (this.npcsToSummon != null && this.npcsToSummon.size() > 0) {
            sb.append("\r\nWill summon the following NPCs:\r\n");
            for (final NPCBuilder builder : this.npcsToSummon) {
                sb.append(builder.toString());
            }
        }
        return sb.toString();
    }

}
