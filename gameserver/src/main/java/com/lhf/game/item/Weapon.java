package com.lhf.game.item;

import java.util.HashSet;
import java.util.Set;

import com.lhf.game.battle.Attack;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;

public class Weapon extends Equipable {
    protected Set<CreatureEffectSource> effectSources;
    protected DamageFlavor mainFlavor;
    protected WeaponSubtype subtype;

    public Weapon(String name, boolean isVisible, Set<CreatureEffectSource> effectSources, DamageFlavor mainFlavor,
            WeaponSubtype subtype) {
        super(name, isVisible, -1);
        this.effectSources = effectSources != null ? effectSources : new HashSet<>();
        this.mainFlavor = mainFlavor;
        this.subtype = subtype;
    }

    public Attack generateAttack(Creature attacker) {
        return this.generateAttack(attacker, null);
    }

    protected Attack generateAttack(Creature attacker, Set<CreatureEffectSource> extraSources) {
        Set<CreatureEffect> effects = new HashSet<>();
        for (CreatureEffectSource source : this.effectSources) {
            effects.add(new CreatureEffect(source, attacker, this));
        }
        if (extraSources != null) {
            for (CreatureEffectSource extra : extraSources) {
                effects.add(new CreatureEffect(extra, attacker, this));
            }
        }
        MultiRollResult toHit = this.calculateToHit(attacker);
        return new Attack(attacker, this, toHit, effects);
    }

    private MultiRollResult calculateToHit(Creature attacker) {
        int attributeBonus = 0;
        AttributeBlock retrieved = attacker.getAttributes();
        Integer str = retrieved.getMod(Attributes.STR);
        Integer dex = retrieved.getMod(Attributes.DEX);
        MultiRollResult toHit = null;
        switch (this.getSubType()) {
            case CREATUREPART:
                // fallthrough
            case FINESSE:
                if (dex > str) {
                    attributeBonus = dex;
                    toHit = attacker.check(Attributes.DEX);
                } else {
                    attributeBonus = str;
                    toHit = attacker.check(Attributes.STR);
                }
                break;
            case PRECISE:
                attributeBonus = dex;
                toHit = attacker.check(Attributes.DEX);
                break;
            case MARTIAL:
                // fallthrough
            default:
                attributeBonus = str;
                toHit = attacker.check(Attributes.STR);
                break;
        }
        toHit.addBonus(attributeBonus);
        return toHit;
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

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage seeOutMessage = super.produceMessage();
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
        return seeOutMessage;
    }

}
