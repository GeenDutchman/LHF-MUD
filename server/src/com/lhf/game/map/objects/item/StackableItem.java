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

    public StackableItem add(int howMany) throws StackableItemException {
        int increasedCount = howMany + this.count;
        if (increasedCount > this.MAX) {
            throw new StackableItemException(new StringBuilder("You cannot add that much to this stack! That is ").append(increasedCount - this.MAX).append(" too many!").toString());
        }
        this.count = increasedCount;
        return this;
    }

    public StackableItem add() throws StackableItemException {
        return this.add(1);
    }

    public StackableItem take(int howMany) throws StackableItemException {
        int decreasedCount = this.count - howMany;
        if (decreasedCount < 0) {
            throw new StackableItemException(new StringBuilder("You cannot take that much from this stack! That is ").append(decreasedCount * -1).append(" too many!").toString());
        }
        this.count = decreasedCount;
        return this;
    }

    public StackableItem take() throws StackableItemException {
        return this.take(1);
    }
}
