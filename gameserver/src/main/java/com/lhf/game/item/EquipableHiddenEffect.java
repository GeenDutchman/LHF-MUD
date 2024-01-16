package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureVisitor;
import com.lhf.game.creature.ICreature;

public class EquipableHiddenEffect extends Equipable {
    protected List<CreatureEffectSource> hiddenEquipEffects;

    public EquipableHiddenEffect(String name, String description) {
        super(name, description);
        this.hiddenEquipEffects = new ArrayList<>();
    }

    public EquipableHiddenEffect(String name, String description, int useSoManyTimes, CreatureVisitor creatureVisitor,
            ItemVisitor itemVisitor) {
        super(name, description, useSoManyTimes, creatureVisitor, itemVisitor);
        this.hiddenEquipEffects = new ArrayList<>();
    }

    protected void copyOverwriteTo(EquipableHiddenEffect other) {
        for (final CreatureEffectSource source : this.hiddenEquipEffects) {
            other.hiddenEquipEffects.add(source.makeCopy());
        }
        super.copyOverwriteTo(other);
    }

    @Override
    public EquipableHiddenEffect makeCopy() {
        EquipableHiddenEffect equipable = new EquipableHiddenEffect(this.getName(), descriptionString,
                this.numCanUseTimes, creatureVisitor, itemVisitor);
        this.copyOverwriteTo(equipable);
        return equipable;
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    public List<CreatureEffectSource> getHiddenEquipEffects() {
        return Collections.unmodifiableList(hiddenEquipEffects);
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
                ICreature.eventAccepter.accept(unequipper,
                        unequipper.applyEffect(new CreatureEffect(effector, unequipper, this), true));
            }
        }
    }
}
