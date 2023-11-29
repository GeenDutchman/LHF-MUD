package com.lhf.game;

public interface LockableItemContainer extends Lockable, ItemContainer {

    public Bypass<? extends LockableItemContainer> getBypass();

    public boolean isRemoveOnEmpty();

    public interface Bypass<T extends LockableItemContainer> extends ItemContainer {
        public T getOrigin();

        public default boolean isRemoveOnEmpty() {
            return this.getOrigin().isRemoveOnEmpty();
        }

        @Override
        public default String getName() {
            return this.getOrigin().getName();
        }

        @Override
        public default String printDescription() {
            return this.getOrigin().printDescription();
        }
    }
}
