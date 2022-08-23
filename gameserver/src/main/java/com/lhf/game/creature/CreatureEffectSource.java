package com.lhf.game.creature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.Stats;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;

public class CreatureEffectSource extends EntityEffectSource {

    protected Map<Stats, Integer> statChanges;

    protected Map<Attributes, Integer> attributeScoreChanges;

    protected Map<Attributes, Integer> attributeBonusChanges;

    protected List<DamageDice> damages;

    protected boolean restoreFaction;

    public CreatureEffectSource(String name, EffectPersistence persistence, String description,
            boolean restoreFaction) {
        super(name, persistence, description);
        this.restoreFaction = restoreFaction;
        this.statChanges = new TreeMap<>();
        this.attributeScoreChanges = new TreeMap<>();
        this.attributeBonusChanges = new TreeMap<>();
        this.damages = new ArrayList<>();
        this.restoreFaction = false;
    }

    public Map<Stats, Integer> getStatChanges() {
        return this.statChanges;
    }

    public Map<Attributes, Integer> getAttributeScoreChanges() {
        return this.attributeScoreChanges;
    }

    public Map<Attributes, Integer> getAttributeBonusChanges() {
        return this.attributeBonusChanges;
    }

    public List<DamageDice> getDamages() {
        return this.damages;
    }

    @Override
    public boolean isOffensive() {
        for (DamageDice dd : this.damages) {
            if (!DamageFlavor.HEALING.equals(dd.getFlavor())) {
                return true;
            }
        }
        for (Integer i : this.statChanges.values()) {
            if (i < 0) {
                return true;
            }
        }
        for (Integer i : this.attributeScoreChanges.values()) {
            if (i < 0) {
                return true;
            }
        }
        for (Integer i : this.attributeBonusChanges.values()) {
            if (i < 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage seeOutMessage = super.produceMessage();
        for (DamageDice dd : this.damages) {
            seeOutMessage.addSeen(SeeCategory.DAMAGES, dd);
        }
        return seeOutMessage;
    }

    @Override
    public String printDescription() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.printDescription()).add("\r\n");
        if (this.getStatChanges() != null && this.getStatChanges().size() > 0) {
            sj.add("The target's");
            for (Map.Entry<Stats, Integer> deltas : this.getStatChanges().entrySet()) {
                sj.add(deltas.getKey().toString()).add("stat will change by").add(deltas.getValue().toString());
            }
            sj.add("\r\n");
        }
        if (this.getAttributeScoreChanges() != null && this.getAttributeScoreChanges().size() > 0) {
            sj.add("The target's");
            for (Map.Entry<Attributes, Integer> deltas : this.getAttributeScoreChanges().entrySet()) {
                sj.add(deltas.getKey().toString()).add("score will change by").add(deltas.getValue().toString());
            }
            sj.add("\r\n");
        }
        if (this.getAttributeBonusChanges() != null && this.getAttributeBonusChanges().size() > 0) {
            sj.add("The target's");
            for (Map.Entry<Attributes, Integer> deltas : this.getAttributeBonusChanges().entrySet()) {
                sj.add(deltas.getKey().toString()).add("bonus will change by").add(deltas.getValue().toString());
            }
            sj.add("\r\n");
        }
        if (this.isRestoreFaction()) {
            sj.add("And will attempt to restore").add("the target's").add("faction");
        }
        return sj.toString();
    }

    public boolean isRestoreFaction() {
        return this.restoreFaction;
    }

    // replaces whatever value was in `stats`, if it existed
    public CreatureEffectSource addStatChange(Stats stats, Integer delta) {
        this.getStatChanges().put(stats, delta);
        return this;
    }

    // replaces whatever value was in `attr`, if it existed
    public CreatureEffectSource addAttributeScoreChange(Attributes attr, Integer delta) {
        this.getAttributeScoreChanges().put(attr, delta);
        return this;
    }

    // replaces whatever value was in `attr`, if it existed
    public CreatureEffectSource addAttributeBonusChange(Attributes attr, Integer delta) {
        this.getAttributeBonusChanges().put(attr, delta);
        return this;
    }

    public CreatureEffectSource addDamage(DamageDice damageDice) {
        this.getDamages().add(damageDice);
        return this;
    }

}
