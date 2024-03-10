package com.lhf.game.item;

import java.util.HashSet;
import java.util.Set;

import com.lhf.game.battle.Attack;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.SeeCategory;

public class Weapon extends Equipable {
    protected Set<CreatureEffectSource> effectSources;
    protected DamageFlavor mainFlavor;
    protected WeaponSubtype subtype;
    protected int toHitBonus = 0;

    public Weapon(String name, String description, Set<CreatureEffectSource> effectSources, DamageFlavor mainFlavor,
            WeaponSubtype subtype) {
        super(name, description);
        this.effectSources = new HashSet<>();
        if (effectSources != null) {
            for (final CreatureEffectSource source : effectSources) {
                this.effectSources.add(source);
            }
        }
        this.mainFlavor = mainFlavor;
        this.subtype = subtype;
    }

    protected Weapon(Weapon other) {
        this(other.getName(), other.descriptionString, other.effectSources, other.mainFlavor, other.subtype);
        this.toHitBonus = other.toHitBonus;
    }

    @Override
    public Weapon makeCopy() {
        if (this.numCanUseTimes < 0) {
            return this;
        }
        return new Weapon(this);
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
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
    public SeeEvent produceMessage(SeeEvent.ABuilder<?> seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = (SeeEvent.ABuilder<?>) super.produceMessage(seeOutMessage).copyBuilder();
        }
        if (this.getEffectSources() != null) {
            for (CreatureEffectSource source : this.getEffectSources()) {
                if (source.getOnApplication() == null) {
                    continue;
                }
                for (DamageDice dd : source.getOnApplication().getDamages()) {
                    seeOutMessage.addSeen(SeeCategory.DAMAGES, dd);
                }
            }
        }
        return seeOutMessage.Build();
    }

}
