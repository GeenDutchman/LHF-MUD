package com.lhf.game.item;

import java.util.HashSet;
import java.util.Set;

import com.lhf.game.battle.Attack;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.Builder;
import com.lhf.messages.events.SeeEvent.SeeCategory;

public class Weapon extends Equipable {
    protected Set<CreatureEffectSource> effectSources;
    protected DamageFlavor mainFlavor;
    protected WeaponSubtype subtype;
    protected int toHitBonus = 0;

    public Weapon(String name, boolean isVisible, Set<CreatureEffectSource> effectSources, DamageFlavor mainFlavor,
            WeaponSubtype subtype) {
        super(name, isVisible, -1);
        this.effectSources = effectSources != null ? effectSources : new HashSet<>();
        this.mainFlavor = mainFlavor;
        this.subtype = subtype;
    }

    public Attack generateAttack(ICreature attacker) {
        return this.generateAttack(attacker, null);
    }

    protected Attack generateAttack(ICreature attacker, Set<CreatureEffectSource> extraSources) {
        Set<CreatureEffect> effects = new HashSet<>();
        for (CreatureEffectSource source : this.effectSources) {
            effects.add(new CreatureEffect(source, attacker, this));
        }
        if (extraSources != null) {
            for (CreatureEffectSource extra : extraSources) {
                effects.add(new CreatureEffect(extra, attacker, this));
            }
        }
        return new Attack(attacker, this, effects);
    }

    public Set<CreatureEffectSource> getEffectSources() {
        return Set.copyOf(this.effectSources);
    }

    public DamageFlavor getMainFlavor() {
        return this.mainFlavor;
    }

    public WeaponSubtype getSubType() {
        return this.subtype;
    }

    public int getToHitBonus() {
        return toHitBonus;
    }

    @Override
    public SeeEvent produceMessage() {
        SeeEvent.Builder seeOutMessage = (Builder) super.produceMessage().copyBuilder();
        if (this.getEffectSources() != null) {
            for (CreatureEffectSource source : this.getEffectSources()) {
                if (source.getDamages() == null) {
                    continue;
                }
                for (DamageDice dd : source.getDamages()) {
                    seeOutMessage.addSeen(SeeCategory.DAMAGES, dd);
                }
            }
        }
        return seeOutMessage.Build();
    }

}
