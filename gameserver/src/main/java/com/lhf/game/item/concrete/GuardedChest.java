package com.lhf.game.item.concrete;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.inventory.InventoryOwner;

public class GuardedChest extends Chest {
    protected final Set<Creature> guards = new TreeSet<>();

    public GuardedChest(ChestDescriptor descriptor, boolean isVisible, boolean removeOnEmpty) {
        super(descriptor, isVisible, false, removeOnEmpty);
    }

    private void updateGuards() {
        for (Iterator<Creature> guardIterator = this.guards.iterator(); guardIterator.hasNext();) {
            Creature guard = guardIterator.next();
            if (guard == null || !guard.isAlive()) {
                guardIterator.remove();
            }
        }
    }

    public Set<String> listGuards() {
        return this.guards.stream().filter(guard -> guard != null && guard.isAlive()).map(guard -> guard.getName())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Adds guards to the chest, ignores dead guards and null
     * 
     * @param guard
     * @return this for builder chaining
     */
    public GuardedChest addGuard(Creature guard) {
        if (guard != null && guard.isAlive()) {
            this.guards.add(guard);
        }
        return this;
    }

    @Override
    public boolean isUnlocked() {
        this.updateGuards();
        return this.guards.isEmpty() && super.isUnlocked();
    }

    @Override
    public boolean canAccess(InventoryOwner attempter) {
        this.updateGuards();
        return this.guards.stream().anyMatch(guard -> guard != null && guard.getName().equals(attempter.getName()))
                || super.canAccess(attempter);
    }

    @Override
    public boolean isAuthorized(InventoryOwner attemtper) {
        this.updateGuards();
        return this.guards.stream().anyMatch(guard -> guard != null && guard.getName().equals(attemtper.getName()))
                || super.isAuthorized(attemtper);
    }

    @Override
    public boolean accessUnlocks() {
        return false;
    }

}
