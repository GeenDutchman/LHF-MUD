package com.lhf.messages.out;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.messages.CommandMessage;
import com.lhf.messages.GameEventType;

public class HelpNeededEvent extends GameEvent {

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends GameEvent.Builder<T> {

        private SortedMap<CommandMessage, String> helps = new TreeMap<>();
        private CommandMessage singleHelp;

        protected AbstractBuilder() {
            super(GameEventType.HELP);
        }

        protected AbstractBuilder(GameEventType type) {
            super(type);
        }

        // this stores a copy of helps;
        public T setHelps(Map<CommandMessage, String> helps) {
            this.helps = helps != null ? new TreeMap<>(helps) : new TreeMap<>();
            return this.getThis();
        }

        public T removeHelp(CommandMessage commandMessage) {
            if (this.helps != null && commandMessage != null) {
                this.helps.remove(commandMessage);
            }
            return this.getThis();
        }

        public T addHelp(CommandMessage commandMessage, String description) {
            if (this.helps == null) {
                this.helps = new TreeMap<>();
            }
            this.helps.put(commandMessage, description);
            return this.getThis();
        }

        public Map<CommandMessage, String> getConcreteHelps() {
            return Collections.unmodifiableSortedMap(this.helps);
        }

        public T setSingleHelp(CommandMessage commandMessage) {
            this.singleHelp = commandMessage;
            return this.getThis();
        }

        public CommandMessage getSingleHelp() {
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

    private final Map<CommandMessage, String> helps;
    private final CommandMessage singleHelp;

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
            for (CommandMessage cmdMsg : this.helps.keySet()) {
                sb.append(cmdMsg.getColorTaggedName()).append(":").append("\r\n").append("<description>")
                        .append(helps.get(cmdMsg)).append("</description>").append("\r\n");
            }
        }
        return sb.toString();
    }

    public Map<CommandMessage, String> getHelps() {
        return helps;
    }

    public CommandMessage getSingleHelp() {
        return singleHelp;
    }

    @Override
    public String print() {
        return this.toString();
    }
}
