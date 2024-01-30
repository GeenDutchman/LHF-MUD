package com.lhf.game;

import java.util.Collections;
import java.util.NavigableSet;

import com.lhf.messages.ITickEvent;
import com.lhf.messages.events.GameEvent;

/**
 * This is used to mark an entity as Affectable. That is, magic works on it.
 * 
 * @see com.lhf.game.EntityEffect EntityEffect
 */
public interface AffectableEntity<Effect extends EntityEffect> {

    /**
     * Process the effect and does the actuall application of them to the entity.
     * 
     * @param effect  to be applied
     * @param reverse if it should be reversed
     * @return a resultant message or null
     */
    GameEvent processEffect(Effect effect, boolean reverse);

    /**
     * This applies the effect to the AffectableEntity.
     * 
     * @param effect  the effect to apply
     * @param reverse true if the effect is to be undone
     * @return a message or null
     */
    default GameEvent applyEffect(Effect effect, boolean reverse) {
        GameEvent processed = this.processEffect(effect, reverse);
        final EffectPersistence persistence = effect.getPersistence();
        if (persistence != null && !TickType.INSTANT.equals(persistence.getTickSize())) {
            this.getMutableEffects().add(effect);
        }
        return processed;
    }

    /**
     * This applies the effect to the AffectableEntity.
     * 
     * @param effect the effect to apply
     * @return a message or null
     */
    default GameEvent applyEffect(Effect effect) {
        return applyEffect(effect, false);
    }

    /**
     * This is to be called when it's possible for an effect to expire.
     * 
     * Based on the {@link com.lhf.game.TickType TickType} in the
     * {@link com.lhf.messages.ITickEvent ITickEvent} the
     * effect may or may not be removed.
     * 
     * @see com.lhf.game.TickType TickType
     * @see com.lhf.messages.ITickEvent ITickEvent
     * @param type
     */
    default void tick(ITickEvent tickEvent) {
        if (tickEvent == null) {
            return;
        }
        NavigableSet<Effect> effects = this.getMutableEffects();
        if (effects != null) {
            effects.removeIf(effect -> {
                if (effect.tick(tickEvent.getTickType()) == 0) {
                    this.applyEffect(effect, true);
                    return true;
                }
                return effect.isReadyForRemoval();
            });
        }
    }

    /**
     * Returns an immutable navigable set of <code>EntityEffects</code>
     */
    default NavigableSet<Effect> getEffects() {
        return Collections.unmodifiableNavigableSet(this.getMutableEffects());
    }

    /**
     * Returns a mutable, navigable set of <code>EntityEffects</code>
     */
    NavigableSet<Effect> getMutableEffects();

    default void removeEffectByName(String name) {
        this.getMutableEffects().removeIf(effect -> effect.getName().equals(name));
    }

    default boolean hasEffect(String name) {
        return this.getEffects().stream().anyMatch(effect -> effect.getName().equals(name));
    }
}
