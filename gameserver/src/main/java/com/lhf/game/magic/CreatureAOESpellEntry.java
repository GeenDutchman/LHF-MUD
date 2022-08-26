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
    /**
     * This class keeps track of which factions are unaffected by the spell.
     */
    public static class AutoSafe {
        // note that below or equal to zero means that they are unaffected
        protected final int npc;
        protected final int caster;
        protected final int allies;
        protected final int enemies;
        protected final int renegades;

        public AutoSafe(boolean isOffensive) {
            this.npc = isOffensive ? 1 : -1;
            this.caster = isOffensive ? 2 : 0;
            this.allies = isOffensive ? 3 : 0;
            this.enemies = isOffensive ? 4 : -2;
            this.renegades = isOffensive ? 5 : -3;
        }

        public AutoSafe(boolean isOffensive, int npc, int caster, int allies, int enemies) {
            this.npc = npc;
            this.caster = caster;
            this.allies = allies;
            this.enemies = enemies;
            int maximum = isOffensive ? Integer.max(npc, caster) : Integer.min(npc, caster);
            maximum = isOffensive ? Integer.max(maximum, allies) : Integer.min(maximum, allies);
            this.renegades = isOffensive ? Integer.max(maximum, enemies) + 1 : Integer.min(maximum, enemies) - 1;
        }

        public AutoSafe(int npc, int caster, int allies, int enemies, int renegades) {
            this.npc = npc;
            this.caster = caster;
            this.allies = allies;
            this.enemies = enemies;
            this.renegades = renegades;
        }

        public static AutoSafe upCast(AutoSafe safe, final int levels, boolean isOffensive) {
            if (safe != null) {
                return safe.upCast(levels, isOffensive);
            }
            return new AutoSafe(0, isOffensive ? 1 : -1, isOffensive ? 2 : 1, isOffensive ? 3 : -1,
                    isOffensive ? 4 : -2);
        }

        public AutoSafe upCast(final int levels, boolean isOffensive) {
            if (isOffensive) {
                return new AutoSafe(npc - levels, caster - levels, allies - levels, enemies - levels,
                        renegades - levels);
            } // upcast to affect more people
            return new AutoSafe(npc + levels, caster + levels, allies + levels, enemies + levels, renegades + levels);
        }

        public static AutoSafe override(AutoSafe one, AutoSafe two, boolean offensiveOverride) {
            if (one != null) {
                return one.override(two, offensiveOverride);
            } else if (two != null) {
                return two.override(one, offensiveOverride);
            }
            return new AutoSafe(offensiveOverride);
        }

        public AutoSafe override(AutoSafe other, boolean offensiveOverride) {
            if (other == null) {
                return this;
            }
            if (offensiveOverride) {
                return new AutoSafe(Integer.max(this.npc, other.npc), Integer.max(this.caster, other.caster),
                        Integer.max(this.allies, other.allies),
                        Integer.max(this.enemies, other.enemies), Integer.max(this.renegades, other.renegades));
            }
            return new AutoSafe(Integer.min(this.npc, other.npc), Integer.min(this.caster, other.caster),
                    Integer.min(this.allies, other.allies),
                    Integer.min(this.enemies, other.enemies), Integer.min(this.renegades, other.renegades));
        }

        public boolean areNpcUnaffected() {
            return npc <= 0;
        }

        public boolean isCasterUnaffected() {
            return caster <= 0;
        }

        public boolean areAlliesUnaffected() {
            return allies <= 0;
        }

        public boolean areEnemiesUnaffected() {
            return enemies <= 0;
        }

        public boolean areRenegadesUnaffected() {
            return renegades <= 0;
        }

        public String printAffected() {
            StringJoiner sj = new StringJoiner(", ")
                    .setEmptyValue("nobody");
            if (!this.areNpcUnaffected()) {
                sj.add("NPCs");
            }
            if (!this.isCasterUnaffected()) {
                sj.add("the caster");
            }
            if (!this.areAlliesUnaffected()) {
                sj.add("the caster's allies");
            }
            if (!this.areEnemiesUnaffected()) {
                sj.add("the caster's enemies");
            }
            if (!this.areRenegadesUnaffected()) {
                sj.add("renegades");
            }
            return " Upon casting: " + sj.toString() + " will be affected.";
        }

        public String printUnffected() {
            StringJoiner sj = new StringJoiner(", ")
                    .setEmptyValue("nobody");
            if (this.areNpcUnaffected()) {
                sj.add("NPCs");
            }
            if (this.isCasterUnaffected()) {
                sj.add("the caster");
            }
            if (this.areAlliesUnaffected()) {
                sj.add("the caster's allies");
            }
            if (this.areEnemiesUnaffected()) {
                sj.add("the caster's enemies");
            }
            if (this.areRenegadesUnaffected()) {
                sj.add("renegades");
            }
            return " Upon casting: " + sj.toString() + " will be not be affected.";
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("NPCs are ").append(npc).append(this.areNpcUnaffected() ? "UNaffected" : "affected")
                    .append("\r\n");
            sb.append("The caster is ").append(caster).append(this.isCasterUnaffected() ? "UNaffected" : "affected")
                    .append("\r\n");
            sb.append("The caster's allies are ").append(allies)
                    .append(this.areAlliesUnaffected() ? "UNaffected" : "affected")
                    .append("\r\n");
            sb.append("The caster's enemies are ").append(enemies)
                    .append(this.areEnemiesUnaffected() ? "UNaffected" : "affected")
                    .append("\r\n");
            sb.append("Renegades are ").append(renegades)
                    .append(this.areRenegadesUnaffected() ? "UNaffected" : "affected")
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
            sj.add(this.isOffensive() ? this.autoSafe.printUnffected() : this.autoSafe.printAffected());
        }
        sj.add("\r\n");

        return sj.toString() + super.printEffectDescriptions();
    }

}
