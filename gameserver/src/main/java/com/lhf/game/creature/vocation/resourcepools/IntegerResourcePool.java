package com.lhf.game.creature.vocation.resourcepools;

import java.util.function.IntUnaryOperator;

import com.lhf.game.enums.ResourceCost;

public abstract class IntegerResourcePool implements ResourcePool {
    protected int amount;
    protected int levelMaxAmount;
    protected final int maxAmount;
    protected final IntUnaryOperator refreshCalculation;

    protected IntegerResourcePool(int maxAmount, IntUnaryOperator refreshCalculation) {
        this.maxAmount = maxAmount;
        this.refreshCalculation = refreshCalculation != null ? refreshCalculation : IntUnaryOperator.identity();
        this.refresh();
    }

    public int getAmount() {
        return amount;
    }

    public int getLevelMaxAmount() {
        return levelMaxAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    @Override
    public boolean checkCost(ResourceCost costNeeded) {
        if (costNeeded == null) {
            return false;
        }
        return costNeeded.toInt() <= this.amount;
    }

    @Override
    public ResourceCost payCost(ResourceCost costNeeded) {
        if (!this.checkCost(costNeeded)) {
            return ResourceCost.NO_COST;
        }
        this.amount -= costNeeded.toInt();
        return costNeeded;
    }

    @Override
    public String print() {
        return String.format("%s: %d/%d", this.getClass().getSimpleName(), this.amount, this.levelMaxAmount);
    }

    @Override
    public void refresh() {
        int level = this.getLevel();
        this.levelMaxAmount = this.refreshCalculation.applyAsInt(level);
        this.levelMaxAmount = Integer.max(this.levelMaxAmount, 0);
        if (this.maxAmount > 0) {
            this.levelMaxAmount = Integer.min(this.levelMaxAmount, this.maxAmount);
        }
        this.amount = this.levelMaxAmount;
    }

    @Override
    public boolean reload(ResourceCost refill) {
        if (refill != null) {
            this.amount += refill.toInt();
            if (this.amount > this.levelMaxAmount) {
                this.amount = this.levelMaxAmount;
            }
        }
        return this.amount == this.levelMaxAmount;
    }
}
