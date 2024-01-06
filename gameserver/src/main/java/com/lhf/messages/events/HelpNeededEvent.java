package com.lhf.messages.events;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.messages.GameEventType;
import com.lhf.messages.in.AMessageType;

public class HelpNeededEvent extends GameEvent {

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends GameEvent.Builder<T> {

        private SortedMap<AMessageType, String> helps = new TreeMap<>();
        private AMessageType singleHelp;

        protected AbstractBuilder() {
            super(GameEventType.HELP);
        }

        protected AbstractBuilder(GameEventType type) {
            super(type);
        }

        // this stores a copy of helps;
        public T setHelps(Map<AMessageType, String> helps) {
            this.helps = helps != null ? new TreeMap<>(helps) : new TreeMap<>();
            return this.getThis();
        }

        public T removeHelp(AMessageType commandMessage) {
            if (this.helps != null && commandMessage != null) {
                this.helps.remove(commandMessage);
            }
            return this.getThis();
        }

        public T addHelp(AMessageType commandMessage, String description) {
            if (this.helps == null) {
                this.helps = new TreeMap<>();
            }
            this.helps.put(commandMessage, description);
            return this.getThis();
        }

        public Map<AMessageType, String> getConcreteHelps() {
            return Collections.unmodifiableSortedMap(this.helps);
        }

        public T setSingleHelp(AMessageType commandMessage) {
            this.singleHelp = commandMessage;
            return this.getThis();
        }

        public AMessageType getSingleHelp() {
            return this.singleHelp;
        }

    }

    public static class Builder extends AbstractBuilder<Builder> {
        protected Builder() {
            super();
        }

        @Override
        public HelpNeededEvent Build() {
            return new HelpNeededEvent(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }
    }

    private final Map<AMessageType, String> helps;
    private final AMessageType singleHelp;

    public static Builder getHelpBuilder() {
        return new Builder();
    }

    protected HelpNeededEvent(AbstractBuilder<?> builder) {
        super(builder);
        this.helps = builder.getConcreteHelps();
        this.singleHelp = builder.getSingleHelp();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.singleHelp != null && this.helps.containsKey(this.singleHelp)) {
            sb.append(this.singleHelp.getColorTaggedName()).append(":").append("\r\n").append("<description>")
                    .append(this.helps.get(this.singleHelp)).append("</description>").append("\r\n");
        } else {
            for (AMessageType cmdMsg : this.helps.keySet()) {
                sb.append(cmdMsg.getColorTaggedName()).append(":").append("\r\n").append("<description>")
                        .append(helps.get(cmdMsg)).append("</description>").append("\r\n");
            }
        }
        return sb.toString();
    }

    public Map<AMessageType, String> getHelps() {
        return helps;
    }

    public AMessageType getSingleHelp() {
        return singleHelp;
    }

    @Override
    public String print() {
        return this.toString();
    }
}
