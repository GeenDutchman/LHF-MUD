package com.lhf.game.events.messages;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.events.GameEvent.GameEventType;
import com.lhf.game.events.GameEventContext;
import com.lhf.game.events.GameEventHandlerNode.GameEventTypeHandler;
import com.lhf.game.events.messages.out.OutMessage;

public class CommandContext extends GameEventContext {

    public static CommandContext fromGameEventContext(GameEventContext ctx) {
        if (ctx instanceof CommandContext ccCtx) {
            return ccCtx;
        }
        CommandContext ccCtx = new CommandContext();
        ccCtx.setClient(ctx.getClientMessenger());
        ccCtx.setUser(ctx.getUser());
        ccCtx.setCreature(ctx.getCreature());
        ccCtx.setRoom(ctx.getRoom());
        ccCtx.setBattleManager(ctx.getBattleManager());
        ccCtx.setDungeon(ctx.getDungeon());
        List<OutMessage> messages = ctx.getMessages();
        if (messages != null) {
            for (OutMessage message : messages) {
                ccCtx.addMessage(message);
            }
        }
        return ccCtx;
    }

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
    public Map<GameEventType, GameEventTypeHandler> addHelps(Map<GameEventType, GameEventTypeHandler> helpsFound) {
        if (this.helps == null) {
            this.helps = new EnumMap<>(CommandMessage.class);
        }
        if (helpsFound != null) {
            for (GameEventTypeHandler handler : helpsFound.values()) {
                if (handler instanceof CommandHandler ch && ch.isEnabled(null)) {
                    this.helps.putIfAbsent(ch.forType(), ch.getHelp(null));
                }
            }
        }
        return helpsFound;
    }

    public CommandContext addHelp(CommandMessage type, String message) {
        if (this.helps == null) {
            this.helps = new EnumMap<>(CommandMessage.class);
        }
        if (type != null && message != null) {
            this.helps.putIfAbsent(type, message);
        }
        return this;
    }

    public Map<CommandMessage, String> getHelps() {
        return Collections.unmodifiableMap(helps);
    }

}
