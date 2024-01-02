package com.lhf.game.creature.vocation.resourcepools;

import java.util.EnumMap;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

import com.lhf.game.enums.ResourceCost;

public abstract class SlottedResourcePool implements ResourcePool {
    protected class Slots extends IntegerResourcePool {

        protected Slots(int maxAmount, IntUnaryOperator howToCalculate) {
            super(maxAmount, howToCalculate);
        }

        @Override
        public int getLevel() {
            return SlottedResourcePool.this.getLevel();
        }

    }

    protected final EnumMap<ResourceCost, Slots> slots = new EnumMap<>(ResourceCost.class);

    protected SlottedResourcePool(final EnumMap<ResourceCost, Integer> maximums,
            final EnumMap<ResourceCost, IntUnaryOperator> calculators) {
        for (ResourceCost cost : ResourceCost.values()) {
            int max = maximums != null ? maximums.getOrDefault(cost, -1) : -1;
            IntUnaryOperator calculator = calculators != null ? calculators.get(cost) : IntUnaryOperator.identity();
            this.slots.put(cost, new Slots(max, calculator));
        }
    }

    @Override
    public boolean checkCost(ResourceCost costNeeded) {
        if (costNeeded == null || !this.slots.containsKey(costNeeded)) {
            return false;
        }
        Slots retrievedSlot = this.slots.get(costNeeded);
        if (retrievedSlot == null) {
            return false;
        }
        return retrievedSlot.getAmount() > 0;
    }

    @Override
    public ResourceCost payCost(ResourceCost costNeeded) {
        if (!this.checkCost(costNeeded)) {
            return ResourceCost.NO_COST;
        }
        Slots retrievedSlot = this.slots.get(costNeeded);
        if (retrievedSlot == null) {
            return ResourceCost.NO_COST;
        }
        retrievedSlot.payCost(ResourceCost.FIRST_MAGNITUDE); // just a replacement for 1
        return costNeeded;
    }

    @Override
    public String print() {
        return String.format("%s: \n%s", this.getClass().getSimpleName(),
                ResourceCost.NO_COST.toString() + ": Infinite\r\n" +
                        this.slots.entrySet().stream()
                                .filter(entry -> entry.getValue().getLevelMaxAmount() > 0
                                        || ResourceCost.NO_COST.equals(entry.getKey()))
                                .map(entry -> entry.getKey().toString() + " " + entry.getValue().print())
                                .collect(Collectors.joining("\n")));
    }

    @Override
    public void refresh() {
        this.slots.values().stream().forEach(slot -> slot.refresh());
    }

    @Override
    public boolean reload(ResourceCost refill) {
        Slots retrievedSlot = this.slots.get(refill);
        if (retrievedSlot == null) {
            return true;
        }
        return retrievedSlot.reload(ResourceCost.FIRST_MAGNITUDE); // just a replacement for 1
    }

}
