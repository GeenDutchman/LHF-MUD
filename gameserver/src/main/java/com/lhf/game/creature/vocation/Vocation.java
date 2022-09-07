package com.lhf.game.creature.vocation;

import java.util.EnumSet;
import java.util.Objects;

import com.lhf.Taggable;
import com.lhf.game.enums.EquipmentTypes;

public abstract class Vocation implements Taggable, Comparable<Vocation> {
    // TODO: make this a MessageHandler

    public enum VocationName implements Taggable {
        FIGHTER, MAGE, DUNGEON_MASTER, HEALER;

        public static VocationName getVocationName(String value) {
            for (VocationName vName : values()) {
                if (vName.toString().equalsIgnoreCase(value)) {
                    return vName;
                }
            }
            return null;
        }

        public static boolean isDamageFlavor(String value) {
            return VocationName.getVocationName(value) != null;
        }

        @Override
        public String getStartTag() {
            return "<vocation>";
        }

        @Override
        public String getEndTag() {
            return "</vocation>";
        }

        @Override
        public String getColorTaggedName() {
            return this.getStartTag() + this.toString() + this.getEndTag();
        }
    }

    protected int level;
    protected final VocationName name;
    protected final EnumSet<EquipmentTypes> proficiencies;

    protected abstract EnumSet<EquipmentTypes> generateProficiencies();

    public Vocation(VocationName name) {
        this.name = name;
        this.proficiencies = this.generateProficiencies();
        this.level = 1;
    }

    public Vocation(VocationName name, EnumSet<EquipmentTypes> otherProficiencies) {
        this.name = name;
        this.proficiencies = this.generateProficiencies();
        if (otherProficiencies != null) {
            this.proficiencies.addAll(otherProficiencies);
        }
        this.level = 1;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name.toString();
    }

    public EnumSet<EquipmentTypes> getProficiencies() {
        return proficiencies;
    }

    @Override
    public String getColorTaggedName() {
        return this.name.getColorTaggedName();
    }

    @Override
    public String getEndTag() {
        return "</vocation>";
    }

    @Override
    public String getStartTag() {
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
