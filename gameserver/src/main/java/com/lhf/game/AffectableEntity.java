package com.lhf.game;

import java.util.Collections;
import java.util.NavigableSet;

import com.lhf.game.EffectPersistence.TickType;
import com.lhf.messages.out.OutMessage;

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
    OutMessage processEffect(EntityEffect effect, boolean reverse);

    /**
     * Checks if the effect is of the correct type to be applicable to the entity.
     * 
     * @param effect to check
     * @return true if it'll work, false otherwise
     */
    boolean isCorrectEffectType(EntityEffect effect);

    /**
     * Determines whether the effect should be stored.
     * Defaults to the value of not <code>reverse</code> or whether the effect's
     * tick size is not instant.
     * 
     * @param effect  to be examined
     * @param reverse if the effect is to be reversed
     * @return true if it should be stored, false otherwise
     */
    default boolean shouldAdd(EntityEffect effect, boolean reverse) {
        return !reverse && effect.getPersistence().getTickSize() != TickType.INSTANT;
    }

    /**
     * Determines whether the effect should be removed from storage.
     * Defaults to the value of <code>reverse</code> or whether the effects ticker
     * has gone down.
     * 
     * @param effect  to be examined if it matters
     * @param reverse if the effect is to be reversed
     * @return true if it should be removed, false otherwise
     */
    default boolean shouldRemove(EntityEffect effect, boolean reverse) {
        return reverse || effect.getPersistence().getTicker().getCountdown() <= 0;
    }

    /**
     * This applies the effect to the AffectableEntity.
     * 
     * @param effect  the effect to apply
     * @param reverse true if the effect is to be undone
     * @return a message or null
     */
    default OutMessage applyEffect(Effect effect, boolean reverse) {
        if (!this.isCorrectEffectType(effect)) {
            return null;
        }
        OutMessage processed = this.processEffect(effect, reverse);
        if (this.shouldAdd(effect, reverse)) {
            this.getMutableEffects().add(effect);
        } else if (this.shouldAdd(effect, reverse)) {
            this.getMutableEffects().remove(effect);
        }
        return processed;
    }

    /**
     * This applies the effect to the AffectableEntity.
     * 
     * @param effect the effect to apply
     * @return a message or null
     */
    default OutMessage applyEffect(Effect effect) {
        return applyEffect(effect, false);
    }

    /**
     * This is to be called when it's possible for an effect to expire.
     * 
     * Based on the {@link com.lhf.game.EffectPersistence.TickType TickType} the
     * effect may or may not be removed.
     * 
     * @see com.lhf.game.EffectPersistence.TickType TickType
     * @param type
     */
    default void tick(TickType type) {
        this.getMutableEffects().removeIf(effect -> {
            if (effect.tick(type) == 0) {
                this.applyEffect(effect, true);
                return true;
            }
            return false;
        });
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
