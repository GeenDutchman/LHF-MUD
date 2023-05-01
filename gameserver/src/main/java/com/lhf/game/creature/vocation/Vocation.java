package com.lhf.game.creature.vocation;

import java.util.Objects;
import java.util.SortedMap;

import com.lhf.Taggable;
import com.lhf.game.battle.Attack;
import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.enums.CreatureFaction;

public abstract class Vocation implements Taggable, Comparable<Vocation>, ActionChooser {
    // TODO: redesign Vocation vs VocationName (decorator pattern?)

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

        public static boolean isVocationName(String value) {
            return VocationName.getVocationName(value) != null;
        }

        public boolean isCubeHolder() {
            return this != FIGHTER;
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

    public abstract Statblock createNewDefaultStatblock(String creatureRace);

    protected Vocation(VocationName name) {
        this.name = name;
        this.level = 1;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name.toString();
    }

    public VocationName getVocationName() {
        return this.name;
    }

    public Attack modifyAttack(Attack attack) {
        return attack;
    }

    @Override
    public SortedMap<String, Float> chooseTarget(BattleMemories battleMemories, CreatureFaction myFaction) {
        // TODO Auto-generated method stub
        return null;
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
        return Objects.hash(name);
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
        return Objects.equals(name, other.name);
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Vocation [name=").append(name).append(", level=").append(level).append("]");
        return builder.toString();
    }

}
