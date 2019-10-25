package com.lhf.game.map.objects.item;

public abstract class StackableItem extends Item {

    public static class StackableItemException extends Exception {
        public StackableItemException(String message) {
            super(message);
        }
    }

    private final int MAX = 20;
    private int count;

    public StackableItem(String name, boolean isVisible) {
        super(name, isVisible);
        this.count = 1;
    }

    public StackableItem(String name, boolean isVisible, int initialCount) {
        super(name, isVisible);
        this.count = initialCount;
    }

    public int getCount() {
        return this.count;
    }

    public int getMax() {
        return this.MAX;
    }

    public StackableItem meld(StackableItem other) throws StackableItemException {
        //TODO: check this logic
        if (this.checkName(other.getName())) {
            int total = this.count + other.getCount();
            if (total > this.MAX) {
                int surplus = total - this.MAX;
                this.count = this.MAX;
                other.count = surplus;
                return this;
            } else {
                this.count = total;
                other.count = 0;
                return this;
            }
        }
        throw new StackableItemException("These cannot be melded, they are not stacks of the same thing!");
    }
}
