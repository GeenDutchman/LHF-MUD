package com.lhf.game.creature.vocation.resourcepools;

import com.lhf.game.enums.ResourceCost;

public interface ResourcePool {

    /** Checks to see if the cost can be paid from the pool */
    public default boolean checkCost(ResourceCost costNeeded) {
        return false;
    }

    /**
     * Pays *at least* the cost from the pool.
     * Will return either `costNeeded` or a higher magnitude of it was necessary.
     * If `{@link #checkCost(ResourceCost)}` would return false, this method will
     * return {@link com.lhf.game.enums.ResourceCost#NO_COST}.
     * 
     * @param costNeeded how much power is needed to pay
     * @return how much was actually paid
     */
    public default ResourceCost payCost(ResourceCost costNeeded) {
        return ResourceCost.NO_COST;
    }

    /**
     * Pays *at least* the cost from the pool.
     * Will return either `costNeeded` or a higher magnitude of it was necessary.
     * If `{@link #checkCost(ResourceCost)}` would return false, this method will
     * return {@link com.lhf.game.enums.ResourceCost#NO_COST}.
     * 
     * @param costNeeded how much power is needed to pay
     * @return how much was actually paid
     */
    public default ResourceCost payCost(int costNeeded) {
        return this.payCost(ResourceCost.fromInt(costNeeded));
    }

    /**
     * Fills the resource pool back up all the way, with all the way defined by
     * level
     */
    public void refresh();

    /**
     * Gets the level for the refresh
     */
    public int getLevel();

    /**
     * Adds `refill` amount of the resource back to the pool.
     * Cannot overflow the pool.
     * 
     * @param refill
     * @return false if not full, else true
     */
    public boolean reload(ResourceCost refill);

    /**
     * Adds `refill` amount of the resource back to the pool.
     * Cannot overflow the pool. `refil` <= 0 will be ignored.
     * 
     * @param refill
     */
    public default void reload(int refill) {
        while (refill > 0) {
            ResourceCost toRefill = ResourceCost.fromInt(refill);
            refill = refill - toRefill.toInt();
            this.reload(toRefill);
        }
    }

    /** Displays the information about the pool */
    public String print();
}