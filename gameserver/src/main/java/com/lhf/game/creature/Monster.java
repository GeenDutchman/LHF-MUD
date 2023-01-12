package com.lhf.game.creature;

import java.util.Objects;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.MonsterAI;

public class Monster extends NonPlayerCharacter {
    private boolean activelyHostile;
    private static long serialNumber = 0;
    private long monsterNumber;

    private MonsterAI aiType;

    public Monster() {
        super();
        this.activelyHostile = false;
        this.setSerialNumber();
        this.aiType = MonsterAI.RANDOM;
        this.setFaction(CreatureFaction.MONSTER);
    }

    public Monster(String name, Statblock statblock) {
        super(NameGenerator.Generate(name), statblock);
        this.activelyHostile = true;
        this.setSerialNumber();
        this.aiType = MonsterAI.RETALIATORY;
        this.setFaction(CreatureFaction.MONSTER);
    }

    @Override
    public void restoreFaction() {
        this.setFaction(CreatureFaction.MONSTER);
    }

    public void setAiType(MonsterAI newType) {
        this.aiType = newType;
    }

    public void setActivelyHostile(boolean setting) {
        this.activelyHostile = setting;
    }

    private void setSerialNumber() {
        this.monsterNumber = Monster.serialNumber;
        Monster.serialNumber++;
    }

    public boolean isActivelyHostile() {
        return this.activelyHostile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        Monster monster = (Monster) o;
        return monsterNumber == monster.monsterNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(monsterNumber) + Objects.hash(this.getName()) * 13;
    }

}
