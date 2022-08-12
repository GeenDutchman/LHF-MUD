package com.lhf.game.magic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import com.lhf.Taggable;
import com.lhf.game.EffectPersistence;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;
import com.lhf.messages.out.CastingMessage;
import com.lhf.messages.out.SeeOutMessage;

public class CreatureTargetingSpellEntry extends SpellEntry {
    protected final boolean singleTarget;
    protected Map<Stats, Integer> statChanges;
    protected Map<Attributes, Integer> attributeScoreChanges;
    protected Map<Attributes, Integer> attributeBonusChanges;
    protected List<DamageDice> damages;
    protected final boolean restoreFaction;

    // TODO: add boolean AOE

    private void init() {
        this.statChanges = new TreeMap<>();
        this.attributeScoreChanges = new TreeMap<>();
        this.attributeBonusChanges = new TreeMap<>();
        this.damages = new ArrayList<>();
    }

    public CreatureTargetingSpellEntry(Integer level, String name, EffectPersistence persistence, String description,
            VocationName... allowed) {
        super(level, name, persistence, description, allowed);
        this.singleTarget = false;
        this.restoreFaction = false;
        this.init();
    }

    public CreatureTargetingSpellEntry(Integer level, String name, String invocation, EffectPersistence persistence,
            String description, VocationName... allowed) {
        super(level, name, invocation, persistence, description, allowed);
        this.singleTarget = false;
        this.restoreFaction = false;
        this.init();
    }

    public CreatureTargetingSpellEntry(Integer level, String name, EffectPersistence persistence, String description,
            boolean singleTarget, boolean restoreFaction, VocationName... allowed) {
        super(level, name, persistence, description, allowed);
        this.singleTarget = singleTarget;
        this.restoreFaction = restoreFaction;
        this.init();
    }

    public CreatureTargetingSpellEntry(Integer level, String name, String invocation, EffectPersistence persistence,
            String description, boolean singleTarget, boolean restoreFaction, VocationName... allowed) {
        super(level, name, invocation, persistence, description, allowed);
        this.singleTarget = singleTarget;
        this.restoreFaction = restoreFaction;
        this.init();
    }

    public CreatureTargetingSpellEntry(CreatureTargetingSpellEntry other) {
        super(other);
        this.singleTarget = other.singleTarget;
        this.restoreFaction = other.restoreFaction;
        this.statChanges = new TreeMap<>(other.getStatChanges());
        this.attributeScoreChanges = new TreeMap<>(other.getAttributeScoreChanges());
        this.attributeBonusChanges = new TreeMap<>(other.getAttributeBonusChanges());
        this.damages = new ArrayList<>(other.getDamages());
    }

    public boolean isSingleTarget() {
        return singleTarget;
    }

    public Map<Stats, Integer> getStatChanges() {
        return statChanges;
    }

    public void setStatChanges(Map<Stats, Integer> statChanges) {
        this.statChanges = statChanges;
    }

    public Map<Attributes, Integer> getAttributeScoreChanges() {
        return attributeScoreChanges;
    }

    public void setAttributeScoreChanges(Map<Attributes, Integer> attributeScoreChanges) {
        this.attributeScoreChanges = attributeScoreChanges;
    }

    public Map<Attributes, Integer> getAttributeBonusChanges() {
        return attributeBonusChanges;
    }

    public void setAttributeBonusChanges(Map<Attributes, Integer> attributeBonusChanges) {
        this.attributeBonusChanges = attributeBonusChanges;
    }

    public List<DamageDice> getDamages() {
        return damages;
    }

    public void setDamages(List<DamageDice> damages) {
        this.damages = damages;
    }

    public boolean isRestoreFaction() {
        return restoreFaction;
    }

    @Override
    public SeeOutMessage produceMessage() {
        return new SeeOutMessage(this);
    }

    @Override
    public CastingMessage Cast(Creature caster, int castLevel, List<? extends Taggable> targets) {
        return new CastingMessage(caster, this, null);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.singleTarget) {
            sj.add("Targets only one creature.");
        } else {
            sj.add("Can target multiple creatures.");
        }
        sj.add("\r\n");
        if (this.damages.size() > 0) {
            sj.add("The target will be damaged with:");
            for (DamageDice dd : this.damages) {
                sj.add(dd.toString());
            }
            sj.add("\r\n");
        }
        if (this.getStatChanges().size() > 0) {
            sj.add("The target's");
            for (Map.Entry<Stats, Integer> deltas : this.getStatChanges().entrySet()) {
                sj.add(deltas.getKey().toString()).add("stat will change by").add(deltas.getValue().toString());
            }
            sj.add("\r\n");
        }
        if (this.getAttributeScoreChanges().size() > 0) {
            sj.add("The target's");
            for (Map.Entry<Attributes, Integer> deltas : this.getAttributeScoreChanges().entrySet()) {
                sj.add(deltas.getKey().toString()).add("score will change by").add(deltas.getValue().toString());
            }
            sj.add("\r\n");
        }
        if (this.getAttributeBonusChanges().size() > 0) {
            sj.add("The target's");
            for (Map.Entry<Attributes, Integer> deltas : this.getAttributeBonusChanges().entrySet()) {
                sj.add(deltas.getKey().toString()).add("bonus will change by").add(deltas.getValue().toString());
            }
            sj.add("\r\n");
        }
        if (this.restoreFaction) {
            sj.add("And will attempt to restore").add("the target's").add("faction");
        }
        return super.toString() + sj.toString();
    }
}
