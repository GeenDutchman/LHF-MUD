package com.lhf.game.creature.vocation;

import java.util.HashSet;
import java.util.Objects;

import com.lhf.Taggable;
import com.lhf.game.enums.EquipmentTypes;

public abstract class Vocation implements Taggable, Comparable<Vocation> {
    protected int level;
    protected final String name;
    protected final HashSet<EquipmentTypes> proficiencies;

    public Vocation(String name) {
        this.name = name;
        this.proficiencies = new HashSet<>();
        this.level = 1;
    }

    public Vocation(String name, HashSet<EquipmentTypes> proficiencies) {
        this.name = name;
        this.proficiencies = proficiencies;
        this.level = 1;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public HashSet<EquipmentTypes> getProficiencies() {
        return proficiencies;
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTagName() + this.getName() + this.getEndTagName();
    }

    @Override
    public String getEndTagName() {
        return "</vocation>";
    }

    @Override
    public String getStartTagName() {
        return "<vocation>";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, proficiencies);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vocation)) {
            return false;
        }
        Vocation other = (Vocation) obj;
        return Objects.equals(name, other.name) && Objects.equals(proficiencies, other.proficiencies);
    }

    @Override
    public int compareTo(Vocation o) {
        if (o == null) {
            return 1;
        }
        int levelCmp = this.level - o.getLevel();
        if (levelCmp != 0) {
            return levelCmp;
        }
        return this.name.compareTo(o.name);
    }

}
