package com.lhf.game.magic;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.messages.out.CastingMessage;

public class CreatureAOESpellEntry extends SpellEntry {
    public static class AutoSafe {
        protected final boolean npc;
        protected final boolean caster;
        protected final boolean allies;
        protected final boolean enemies;
        protected final boolean renegades;

        public AutoSafe() {
            this.npc = false;
            this.caster = false;
            this.allies = false;
            this.enemies = false;
            this.renegades = false;
        }

        public AutoSafe(boolean npc, boolean caster, boolean allies, boolean enemies) {
            this.npc = npc;
            this.caster = caster;
            this.allies = allies;
            this.enemies = enemies;
            this.renegades = false;
        }

        public AutoSafe(boolean npc, boolean caster, boolean allies, boolean enemies, boolean renegades) {
            this.npc = npc;
            this.caster = caster;
            this.allies = allies;
            this.enemies = enemies;
            this.renegades = renegades;
        }

        public static AutoSafe override(AutoSafe one, AutoSafe two) {
            if (one != null) {
                return one.override(two);
            } else if (two != null) {
                return two.override(one);
            }
            return new AutoSafe();
        }

        public AutoSafe override(AutoSafe other) {
            if (other == null) {
                return this;
            }
            return new AutoSafe(this.npc || other.npc, this.caster || other.caster, this.allies || other.allies,
                    this.enemies || other.enemies, this.renegades || other.renegades);
        }

        public boolean isNpc() {
            return npc;
        }

        public boolean isCaster() {
            return caster;
        }

        public boolean isAllies() {
            return allies;
        }

        public boolean isEnemies() {
            return enemies;
        }

        public boolean isRenegades() {
            return renegades;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(", ")
                    .setEmptyValue("all the creatures in the room");
            if (this.npc) {
                sj.add("NPC's");
            }
            if (this.caster) {
                sj.add("the caster");
            }
            if (this.allies) {
                sj.add("the caster's allies");
            }
            if (this.enemies) {
                sj.add("the caster's enemies");
            }
            if (this.renegades) {
                sj.add("renegades");
            }
            return " Upon casting: " + sj.toString() + " will be affected.";
        }

        @Override
        public int hashCode() {
            return Objects.hash(allies, caster, enemies, npc, renegades);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AutoSafe)) {
                return false;
            }
            AutoSafe other = (AutoSafe) obj;
            return allies == other.allies && caster == other.caster && enemies == other.enemies && npc == other.npc
                    && renegades == other.renegades;
        }

    }

    protected final AutoSafe autoSafe;

    public CreatureAOESpellEntry(Integer level, String name, Set<CreatureEffectSource> effectSources,
            Set<VocationName> allowed, String description, AutoSafe safe) {
        super(level, name, effectSources, allowed, description);
        this.autoSafe = safe;
    }

    public CreatureAOESpellEntry(Integer level, String name, String invocation,
            Set<CreatureEffectSource> effectSources,
            Set<VocationName> allowed, String description, AutoSafe safe) {
        super(level, name, invocation, effectSources, allowed, description);
        this.autoSafe = safe;
    }

    public AutoSafe getAutoSafe() {
        return autoSafe;
    }

    @Override
    public CastingMessage Cast(Creature caster, int castLevel, List<? extends Taggable> targets) {
        return new CastingMessage(caster, this, null);
    }

    @Override
    public String printDescription() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(this.description);
        if (this.autoSafe != null) {
            sj.add(this.autoSafe.toString());
        }
        sj.add("\r\n");

        return sj.toString() + super.printEffectDescriptions();
    }

}
