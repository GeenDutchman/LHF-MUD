package com.lhf.game.events.messages;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.lhf.game.events.GameEventContext;

public class CommandContext extends GameEventContext {
    protected EnumMap<CommandMessage, String> helps = new EnumMap<>(CommandMessage.class);

    public class Reply extends GameEventContext.Reply {
        protected Reply(boolean isHandled) {
            super(isHandled);
        }

        public Map<CommandMessage, String> getHelps() {
            if (CommandContext.this.helps == null) {
                CommandContext.this.helps = new EnumMap<>(CommandMessage.class);
            }
            return Collections.unmodifiableMap(CommandContext.this.helps);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("CommandReply [handled=").append(handled)
                    .append(",messageTypes=")
                    .append(this.getMessages().stream().map(outMessage -> outMessage.getOutType()).toList())
                    .append(",helps=").append(this.getHelps().keySet())
                    .append("]");
            return builder.toString();
        }

    }

    @Override
    public Reply failhandle() {
        return this.new Reply(false);
    }

    @Override
    public Reply handled() {
        return this.new Reply(true);
    }

    /**
     * Adds help data to the context, returns the provided helps found
     * 
     * @param helpsFound help data to collect in the context
     * @return the helpsFound
     */
    public Map<CommandMessage, String> addHelps(Map<CommandMessage, String> helpsFound) {
        if (this.helps == null) {
            this.helps = new EnumMap<>(CommandMessage.class);
        }
        if (helpsFound != null) {
            for (Map.Entry<CommandMessage, String> entry : helpsFound.entrySet()) {
                this.helps.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        return helpsFound;
    }

    public Map<CommandMessage, String> getHelps() {
        return Collections.unmodifiableMap(helps);
    }

}
