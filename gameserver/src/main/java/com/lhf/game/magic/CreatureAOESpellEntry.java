package com.lhf.game.magic;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.ResourceCost;
import com.lhf.messages.out.CastingMessage;

public class CreatureAOESpellEntry extends SpellEntry {
    /**
     * This class keeps track of which factions are un/affected by the spell.
     */
    public static class AutoTargeted {
        // note that values greater than or equal to zero means that they can be
        // affected by the spell by that many upcasts
        protected final int npc;
        protected final int caster;
        protected final int allies;
        protected final int enemies;
        protected final int renegades;

        public AutoTargeted(boolean isOffensive) {
            if (isOffensive) {
                this.npc = 0; // upcast once to not affect with offensive spell
                this.caster = 1;
                this.allies = 2;
                this.enemies = 3;
                this.renegades = 4; // upcast four times to not affect with offensive spell
                return;
            }
            this.npc = -1;
            this.caster = 0;
            this.allies = 0;
            this.enemies = -2;
            this.renegades = -3; // upcast three times to affect with a beneficial spell
        }

        public AutoTargeted(boolean isOffensive, int npc, int caster, int allies, int enemies) {
            this.npc = npc;
            this.caster = caster;
            this.allies = allies;
            this.enemies = enemies;
            if (isOffensive) {
                // keep them targeted by offensive spells as long as possible
                int maximum = Integer.max(npc, caster);
                maximum = Integer.max(maximum, allies);
                this.renegades = Integer.max(maximum, enemies) + 1;
                return;
            }
            // prevent them from being targeted by beneficial spells as long as possible
            int minimum = Integer.min(npc, caster);
            minimum = Integer.min(minimum, allies);
            this.renegades = Integer.min(minimum, enemies) - 1;
        }

        public AutoTargeted(int npc, int caster, int allies, int enemies, int renegades) {
            this.npc = npc;
            this.caster = caster;
            this.allies = allies;
            this.enemies = enemies;
            this.renegades = renegades;
        }

        public static AutoTargeted upCast(AutoTargeted safe, final int levels, boolean isOffensive) {
            if (safe != null) {
                return safe.upCast(levels, isOffensive);
            }
            return new AutoTargeted(isOffensive).upCast(levels, isOffensive);
        }

        public AutoTargeted upCast(final int levels, boolean isOffensive) {
            if (isOffensive) {
                // upcast to keep people safe from offense (below 0)
                return new AutoTargeted(npc - levels, caster - levels, allies - levels, enemies - levels,
                        renegades - levels);
            } // upcast to affect more people with benefits
            return new AutoTargeted(npc + levels, caster + levels, allies + levels, enemies + levels,
                    renegades + levels);
        }

        public static AutoTargeted override(AutoTargeted one, AutoTargeted two, boolean offensiveOverride) {
            if (one != null) {
                return one.override(two, offensiveOverride);
            } else if (two != null) {
                return two.override(one, offensiveOverride);
            }
            return new AutoTargeted(offensiveOverride);
        }

        public AutoTargeted override(AutoTargeted other, boolean offensiveOverride) {
            if (other == null) {
                return this;
            }
            if (offensiveOverride) {
                // keep them affected (above 0)
                return new AutoTargeted(Integer.max(this.npc, other.npc), Integer.max(this.caster, other.caster),
                        Integer.max(this.allies, other.allies),
                        Integer.max(this.enemies, other.enemies), Integer.max(this.renegades, other.renegades));
            }
            // keep them defended
            return new AutoTargeted(Integer.min(this.npc, other.npc), Integer.min(this.caster, other.caster),
                    Integer.min(this.allies, other.allies),
                    Integer.min(this.enemies, other.enemies), Integer.min(this.renegades, other.renegades));
        }

        public boolean areNPCsTargeted() {
            return npc >= 0;
        }

        public boolean isCasterTargeted() {
            return caster >= 0;
        }

        public boolean areAlliesTargeted() {
            return allies >= 0;
        }

        public boolean areEnemiesTargeted() {
            return enemies >= 0;
        }

        public boolean areRenegadesTargeted() {
            return renegades >= 0;
        }

        public String printAffected() {
            StringJoiner sj = new StringJoiner(", ")
                    .setEmptyValue("nobody");
            if (this.areNPCsTargeted()) {
                sj.add("NPCs");
            }
            if (this.isCasterTargeted()) {
                sj.add("the caster");
            }
            if (this.areAlliesTargeted()) {
                sj.add("the caster's allies");
            }
            if (this.areEnemiesTargeted()) {
                sj.add("the caster's enemies");
            }
            if (this.areRenegadesTargeted()) {
                sj.add("renegades");
            }
            return " Upon casting: " + sj.toString() + " will be affected.";
        }

        public String printUnffected() {
            StringJoiner sj = new StringJoiner(", ")
                    .setEmptyValue("nobody");
            if (!this.areNPCsTargeted()) {
                sj.add("NPCs");
            }
            if (!this.isCasterTargeted()) {
                sj.add("the caster");
            }
            if (!this.areAlliesTargeted()) {
                sj.add("the caster's allies");
            }
            if (!this.areEnemiesTargeted()) {
                sj.add("the caster's enemies");
            }
            if (!this.areRenegadesTargeted()) {
                sj.add("renegades");
            }
            return " Upon casting: " + sj.toString() + " will be not be affected.";
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("NPCs are ").append(npc).append(!this.areNPCsTargeted() ? "UNaffected" : "affected")
                    .append("\r\n");
            sb.append("The caster is ").append(caster).append(!this.isCasterTargeted() ? "UNaffected" : "affected")
                    .append("\r\n");
            sb.append("The caster's allies are ").append(allies)
                    .append(!this.areAlliesTargeted() ? "UNaffected" : "affected")
                    .append("\r\n");
            sb.append("The caster's enemies are ").append(enemies)
                    .append(!this.areEnemiesTargeted() ? "UNaffected" : "affected")
                    .append("\r\n");
            sb.append("Renegades are ").append(renegades)
                    .append(!this.areRenegadesTargeted() ? "UNaffected" : "affected")
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
            if (!(obj instanceof AutoTargeted)) {
                return false;
            }
            AutoTargeted other = (AutoTargeted) obj;
            return allies == other.allies && caster == other.caster && enemies == other.enemies && npc == other.npc
                    && renegades == other.renegades;
        }

    }

    protected final AutoTargeted autoSafe;

    public CreatureAOESpellEntry(ResourceCost level, String name, Set<CreatureEffectSource> effectSources,
            Set<VocationName> allowed, String description, AutoTargeted safe) {
        super(level, name, effectSources, allowed, description);
        this.autoSafe = safe;
    }

    public CreatureAOESpellEntry(ResourceCost level, String name, String invocation,
            Set<CreatureEffectSource> effectSources,
            Set<VocationName> allowed, String description, AutoTargeted safe) {
        super(level, name, invocation, effectSources, allowed, description);
        this.autoSafe = safe;
    }

    public AutoTargeted getAutoSafe() {
        return autoSafe;
    }

    @Override
    public CastingMessage Cast(ICreature caster, ResourceCost castLevel, List<? extends Taggable> targets) {
        StringJoiner sj = new StringJoiner(", ", "Targeting: ", "").setEmptyValue("nothing");
        if (targets != null) {
            for (Taggable taggable : targets) {
                sj.add(taggable.getColorTaggedName());
            }
        }
        return CastingMessage.getBuilder().setCaster(caster).setSpellEntry(this).setCastEffects(sj.toString()).Build();
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
