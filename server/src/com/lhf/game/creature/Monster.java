package com.lhf.game.creature;

import java.util.Objects;

public class Monster extends Creature {
    private boolean activelyHostile;
    private static long serialNumber = 0;
    private long monsterNumber;

    public Monster() {
        super();
        this.activelyHostile = false;
        this.setName("Monster");
        this.setSerialNumber();
    }

    public Monster(String name, Statblock statblock) {
        super(name, statblock);
        this.activelyHostile = true;
        this.setSerialNumber();
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Monster monster = (Monster) o;
        return monsterNumber == monster.monsterNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(monsterNumber) + Objects.hash(this.getName()) * 13;
    }
}
