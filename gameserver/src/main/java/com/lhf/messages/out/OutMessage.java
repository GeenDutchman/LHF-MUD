package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public abstract class OutMessage {

    public static abstract class Builder<T extends Builder<T>> {
        private OutMessageType type;
        private boolean broadcast;
        protected T thisObject;

        protected Builder(OutMessageType type) {
            this.type = type;
            this.broadcast = false;
            this.thisObject = this.getThis();
        }

        public T setType(OutMessageType type) {
            this.type = type;
            return this.getThis();
        }

        public OutMessageType getType() {
            return this.type;
        }

        public T setBroacast() {
            this.broadcast = true;
            return this.getThis();
        }

        public T setNotBroadcast() {
            this.broadcast = false;
            return this.getThis();
        }

        public boolean isBroadcast() {
            return this.broadcast;
        }

        public abstract T getThis();

        public abstract OutMessage Build();
    }

    private final OutMessageType type;
    private final boolean broadcast;

    public OutMessage(Builder<?> builder) {
        this.type = builder.getType();
        this.broadcast = builder.isBroadcast();
    }

    public OutMessageType getOutType() {
        return this.type;
    }

    public boolean isBroadcast() {
        return this.broadcast;
    }

    // Called to render as a human-readable string
    public abstract String print();

}
