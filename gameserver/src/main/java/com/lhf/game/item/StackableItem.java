package com.lhf.game.item;

public class StackableItem extends Usable {

    public static class StackableItemException extends Exception {
        StackableItemException(String message) {
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

    @Override
    public StackableItem makeCopy() {
        return new StackableItem(this.getName(), this.checkVisibility(), this.count);
    }

    @Override
    public void acceptVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    private int getCount() {
        return this.count;
    }

    public int getMax() {
        return this.MAX;
    }

    public StackableItem meld(StackableItem other) throws StackableItemException {
        if (this.getName().equals(other.getName())) {
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
        throw new StackableItemException("These cannot be melded, they are not stacks of the same thing!\r\n");
    }

    public StackableItem add(int howMany) throws StackableItemException {
        int increasedCount = howMany + this.count;
        if (increasedCount > this.MAX) {
            throw new StackableItemException("You cannot add that much to this stack! That is "
                    + (increasedCount - this.MAX) + " too many!\r\n");
        }
        this.count = increasedCount;
        return this;
    }

    public StackableItem add() throws StackableItemException {
        return this.add(1);
    }

    private StackableItem take(int howMany) throws StackableItemException {
        int decreasedCount = this.count - howMany;
        if (decreasedCount < 0) {
            throw new StackableItemException(
                    "You cannot take that much from this stack! That is " + decreasedCount * -1 + " too many!\r\n");
        }
        this.count = decreasedCount;
        return this;
    }

    public StackableItem take() throws StackableItemException {
        return this.take(1);
    }
}
