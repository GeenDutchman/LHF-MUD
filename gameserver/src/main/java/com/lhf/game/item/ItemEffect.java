package com.lhf.game.item;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.ICreature;

public class ItemEffect extends EntityEffect {

    public ItemEffect(ItemEffectSource source, ICreature creatureResponsible, Taggable generatedBy) {
        super(source, creatureResponsible, generatedBy);
    }

    public ItemEffect(ItemEffect other) {
        super(other.source, other.creatureResponsible, other.generatedBy);
    }

    public ItemEffectSource getSource() {
        return (ItemEffectSource) this.source;
    }

    public GameEventProcessorCapability.Delta getGameEventProcessorCapabilityDelta() {
        return this.getSource().getGameEventProcessorCapabilityDelta();
    }

    public InteractableCapability.Delta getInteractableCapabilityDelta() {
        return this.getSource().getInteractableCapabilityDelta();
    }

    public RenameCapability.Delta getRenameCapabilityDelta() {
        return this.getSource().getRenameCapabilityDelta();
    }

    public UsableCapability.Delta getUsableCapabilityDelta() {
        return this.getSource().getUsableCapabilityDelta();
    }

    public LockingCapability.Delta getLockingCapabilityDelta() {
        return this.getSource().getLockingCapabilityDelta();
    }

}
