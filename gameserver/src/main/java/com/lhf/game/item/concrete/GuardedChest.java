package com.lhf.game.item.concrete;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.inventory.InventoryOwner;
import com.lhf.messages.events.ItemInteractionEvent;

public class GuardedChest extends Chest {
    protected final Set<String> guards = new TreeSet<>();

    public GuardedChest(ChestDescriptor descriptor) {
        super(descriptor);
    }

    protected GuardedChest(String name) {
        super(name);
    }

    @Override
    public void doAction(ICreature creature) {
        if (creature == null) {
            return;
        }
        ItemInteractionEvent.Builder builder = ItemInteractionEvent.getBuilder().setTaggable(this);
        this.updateGuards();
        if (this.canAccess(creature)) {
            super.doAction(creature);
            return;
        }
        StringJoiner sj = new StringJoiner(", ", " It is guarded by: ", ". ").setEmptyValue("");
        this.listGuards().stream().filter(name -> name != null).forEachOrdered(name -> sj.add(name));
        String message = String.format("%s finds that they cannot access %s.%s%s", creature.getColorTaggedName(),
                this.getColorTaggedName(), this.isUnlocked() ? "" : " It is locked. ", sj.toString());
        builder.setDescription(message);
        this.broadcast(creature, builder);
        this.interactCount++;
    }

    private void updateGuards() {
        if (this.area != null) {
            for (Iterator<String> guardIterator = this.guards.iterator(); guardIterator.hasNext();) {
                final String guardName = guardIterator.next();
                if (guardName == null || guardName.isEmpty()) {
                    guardIterator.remove();
                    continue;
                }
                if (!this.area.hasCreature(guardName)) {
                    guardIterator.remove();
                }
            }
        }
    }

    public Set<String> listGuards() {
        return Collections.unmodifiableSet(this.guards);
    }

    /**
     * Adds guards to the chest, ignores dead guards and null
     * 
     * @param guard
     * @return this for builder chaining
     */
    public GuardedChest addGuard(ICreature guard) {
        if (guard != null && guard.isAlive()) {
            this.guards.add(guard.getName());
        }
        return this;
    }

    public GuardedChest addGuard(String name) {
        if (name != null && !name.isBlank()) {
            this.guards.add(name);
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
        return this.guards.stream().anyMatch(guard -> guard != null && guard.equals(attempter.getName()))
                || super.canAccess(attempter);
    }

    @Override
    public boolean isAuthorized(InventoryOwner attemtper) {
        this.updateGuards();
        return this.guards.stream().anyMatch(guard -> guard != null && guard.equals(attemtper.getName()))
                || super.isAuthorized(attemtper);
    }

    @Override
    public boolean accessUnlocks() {
        return false;
    }

}
