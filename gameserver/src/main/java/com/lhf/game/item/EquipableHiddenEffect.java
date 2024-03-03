package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.CommandContext;

public class EquipableHiddenEffect extends Equipable {
    protected List<CreatureEffectSource> hiddenEquipEffects;
    protected Set<CreatureEffectSource> hiddenUseEffects;

    public EquipableHiddenEffect(String name, String description) {
        super(name, description);
        this.hiddenEquipEffects = new ArrayList<>();
    }

    public EquipableHiddenEffect(String name, String description, int useSoManyTimes,
            Set<CreatureEffectSource> useOnCreatureEffectSources,
            Set<CreatureEffectSource> hiddenUseOnCreatureEffectSources) {
        super(name, description, useSoManyTimes, useOnCreatureEffectSources);
        this.hiddenEquipEffects = new ArrayList<>();
        this.hiddenUseEffects = hiddenUseOnCreatureEffectSources;
    }

    public EquipableHiddenEffect(EquipableHiddenEffect other) {
        this(other.getName(), other.descriptionString, other.numCanUseTimes, other.creatureUseEffects,
                other.hiddenUseEffects);
        for (final CreatureEffectSource source : other.hiddenEquipEffects) {
            this.hiddenEquipEffects.add(source);
        }
    }

    @Override
    public EquipableHiddenEffect makeCopy() {
        if (this.numCanUseTimes < 0) {
            return this;
        }
        return new EquipableHiddenEffect(this);
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    public List<CreatureEffectSource> getHiddenEquipEffects() {
        return hiddenEquipEffects != null ? Collections.unmodifiableList(hiddenEquipEffects) : List.of();
    }

    public Set<CreatureEffectSource> getHiddenUseEffects() {
        return hiddenUseEffects != null ? Collections.unmodifiableSet(hiddenUseEffects) : Set.of();
    }

    @Override
    public void onEquippedBy(ICreature equipper) {
        super.onEquippedBy(equipper);
        for (final CreatureEffectSource effector : this.getHiddenEquipEffects()) {
            ICreature.eventAccepter.accept(equipper,
                    equipper.applyEffect(new CreatureEffect(effector, equipper, this)));
        }
    }

    @Override
    public void onUnequippedBy(ICreature unequipper) {
        super.onUnequippedBy(unequipper);
        for (final CreatureEffectSource effector : this.getHiddenEquipEffects()) {
            if (effector.getPersistence() != null
                    && TickType.CONDITIONAL.equals(effector.getPersistence().getTickSize())) {
                ICreature.eventAccepter.accept(unequipper, unequipper.repealEffect(effector.getName()));
            }
        }
    }

    @Override
    protected void applyCreatureEffects(CommandContext ctx, ICreature creature) {
        if (creature == null) {
            return;
        }
        for (final CreatureEffectSource source : this.getCreatureUseEffects()) {
            final CreatureEffect effect = new CreatureEffect(source, ctx.getCreature(), EquipableHiddenEffect.this);
            this.sendNotice(ctx, creature, creature.applyEffect(effect));
        }
        for (final CreatureEffectSource source : this.getHiddenUseEffects()) {
            final CreatureEffect effect = new CreatureEffect(source, ctx.getCreature(), EquipableHiddenEffect.this);
            this.sendNotice(ctx, creature, creature.applyEffect(effect));
        }
    }
}
