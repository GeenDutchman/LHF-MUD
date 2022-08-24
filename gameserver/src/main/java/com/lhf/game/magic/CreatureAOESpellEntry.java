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

        public boolean isNpc(int base, int level) {
            return base + npc <= level;
        }

        public boolean isCaster(int base, int level) {
            return base + caster <= level;
        }

        public boolean isAllies(int base, int level) {
            return base + allies <= level;
        }

        public boolean isEnemies(int base, int level) {
            return base + enemies <= level;
        }

        public boolean isRenegades(int base, int level) {
            return base + renegades <= level;
        }

        public String atLevel(int base, int level) {
            StringJoiner sj = new StringJoiner(", ")
                    .setEmptyValue("all the creatures in the room");
            if (this.isNpc(base, level)) {
                sj.add("NPC's");
            }
            if (this.isCaster(base, level)) {
                sj.add("the caster");
            }
            if (this.isAllies(base, level)) {
                sj.add("the caster's allies");
            }
            if (this.isEnemies(base, level)) {
                sj.add("the caster's enemies");
            }
            if (this.isRenegades(base, level)) {
                sj.add("renegades");
            }
            return " Upon casting: " + sj.toString() + " will be affected.";
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("NPC's affected ").append(this.npc).append(" levels above base.").append("\r\n");
            sb.append("Caster affected ").append(this.caster).append(" levels above base.").append("\r\n");
            sb.append("Allies affected ").append(this.allies).append(" levels above base.").append("\r\n");
            sb.append("Enimies affected ").append(this.enemies).append(" levels above base.").append("\r\n");
            sb.append("Renegades affected ").append(this.renegades).append(" levels above base.").append("\r\n");

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
