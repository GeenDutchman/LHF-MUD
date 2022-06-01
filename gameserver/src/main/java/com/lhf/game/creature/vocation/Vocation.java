package com.lhf.game.creature.vocation;

import java.util.HashSet;

import com.lhf.Taggable;
import com.lhf.game.enums.EquipmentTypes;

public abstract class Vocation implements Taggable {
    protected int level;
    protected String name;
    protected HashSet<EquipmentTypes> proficiencies;

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

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public HashSet<EquipmentTypes> getProficiencies() {
        return proficiencies;
    }

    public void setProficiencies(HashSet<EquipmentTypes> proficiencies) {
        this.proficiencies = proficiencies;
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

}
