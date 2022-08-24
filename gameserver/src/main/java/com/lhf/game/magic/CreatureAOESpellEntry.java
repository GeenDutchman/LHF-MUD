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
        protected final int npc;
        protected final int caster;
        protected final int allies;
        protected final int enemies;
        protected final int renegades;

        public AutoSafe() {
            this.npc = 1;
            this.caster = 2;
            this.allies = 3;
            this.enemies = 4;
            this.renegades = 5;
        }

        public AutoSafe(int npc, int caster, int allies, int enemies) {
            this.npc = npc;
            this.caster = caster;
            this.allies = allies;
            this.enemies = enemies;
            int maximum = Integer.max(npc, caster);
            maximum = Integer.max(maximum, allies);
            this.renegades = Integer.max(maximum, enemies) + 1;
        }

        public AutoSafe(int npc, int caster, int allies, int enemies, int renegades) {
            this.npc = npc;
            this.caster = caster;
            this.allies = allies;
            this.enemies = enemies;
            this.renegades = renegades;
        }

        public static AutoSafe upCast(AutoSafe safe, final int levels) {
            if (safe != null) {
                return safe.upCast(levels);
            }
            return new AutoSafe(0, 1, 2, 3, 4);
        }

        public AutoSafe upCast(final int levels) {
            return new AutoSafe(npc - levels, caster - levels, allies - levels, enemies - levels, renegades - levels);
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
            return new AutoSafe(Integer.min(this.npc, other.npc), Integer.min(this.caster, other.caster),
                    Integer.min(this.allies, other.allies),
                    Integer.min(this.enemies, other.enemies), Integer.min(this.renegades, other.renegades));
        }

        public boolean areNpcSafe() {
            return npc <= 0;
        }

        public boolean isCasterSafe() {
            return caster <= 0;
        }

        public boolean areAlliesSafe() {
            return allies <= 0;
        }

        public boolean areEnemiesSafe() {
            return enemies <= 0;
        }

        public boolean areRenegadesSafe() {
            return renegades <= 0;
        }

        public String printAffected() {
            StringJoiner sj = new StringJoiner(", ")
                    .setEmptyValue("nobody");
            if (!this.areNpcSafe()) {
                sj.add("NPCs");
            }
            if (!this.isCasterSafe()) {
                sj.add("the caster");
            }
            if (!this.areAlliesSafe()) {
                sj.add("the caster's allies");
            }
            if (!this.areEnemiesSafe()) {
                sj.add("the caster's enemies");
            }
            if (!this.areRenegadesSafe()) {
                sj.add("renegades");
            }
            return " Upon casting: " + sj.toString() + " will be affected.";
        }

        public String printUnffected() {
            StringJoiner sj = new StringJoiner(", ")
                    .setEmptyValue("nobody");
            if (this.areNpcSafe()) {
                sj.add("NPCs");
            }
            if (this.isCasterSafe()) {
                sj.add("the caster");
            }
            if (this.areAlliesSafe()) {
                sj.add("the caster's allies");
            }
            if (this.areEnemiesSafe()) {
                sj.add("the caster's enemies");
            }
            if (this.areRenegadesSafe()) {
                sj.add("renegades");
            }
            return " Upon casting: " + sj.toString() + " will be not be affected.";
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("NPCs unaffected ").append(this.npc).append(" levels above casting level.").append("\r\n");
            sb.append("Caster unaffected ").append(this.caster).append(" levels above casting level.").append("\r\n");
            sb.append("Allies unaffected ").append(this.allies).append(" levels above casting level.").append("\r\n");
            sb.append("Enimies unaffected ").append(this.enemies).append(" levels above casting level.").append("\r\n");
            sb.append("Renegades unaffected ").append(this.renegades).append(" levels above casting level.")
                    .append("\r\n");

            return sb.toString();
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
