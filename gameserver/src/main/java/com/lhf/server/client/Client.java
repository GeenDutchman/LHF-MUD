package com.lhf.server.client;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.messages.Command;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.HelpNeededEvent;

public class Client implements CommandInvoker {
    public final static class ClientID implements Comparable<ClientID> {
        private final UUID uuid;

        public ClientID() {
            uuid = UUID.randomUUID();
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ClientID [uuid=").append(uuid).append("]");
            return builder.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ClientID)) {
                return false;
            }
            ClientID other = (ClientID) obj;
            return Objects.equals(uuid, other.uuid);
        }

        @Override
        public int compareTo(ClientID o) {
            return this.uuid.compareTo(o.uuid);
        }

        public UUID getUuid() {
            return uuid;
        }

    }

    protected final ClientID id;
    protected SendStrategy out;
    protected final GameEventProcessorID gameEventProcessorID;
    protected Logger logger;
    protected transient CommandChainHandler _successor;
    protected final HelpHandler helpHandler = new HelpHandler();

    protected Client() {
        this.id = new ClientID();
        this.gameEventProcessorID = new GameEventProcessorID();
        this.logger = Logger
                .getLogger(String.format("%s.%d", this.getClass().getName(), this.getClientID().hashCode()));
        this.log(Level.FINEST,
                () -> String.format("Creating client %s.%d", this.getClass().getName(),
                        this.getEventProcessorID().hashCode()));
        this._successor = null;
        this.out = null;
    }

    public void SetOut(SendStrategy out) {
        this.out = out;
    }

    @Override
    public CommandInvoker getInnerCommandInvoker() {
        return this;
    }

    public CommandContext.Reply ProcessString(String value) {
        this.log(Level.FINE, "message received: " + value);
        Command cmd = CommandBuilder.parse(value);
        CommandContext ctx = new CommandContext();
        ctx.setClient(this);
        CommandContext.Reply accepted = ctx.failhandle();
        if (cmd.isValid()) {
            this.log(Level.FINEST, "the message received was deemed" + cmd.getClass().toString());
            this.log(Level.FINER, "Post Processing:" + cmd);
            accepted = this.handleChain(ctx, cmd);
            if (!accepted.isHandled()) {
                this.log(Level.WARNING, "Command not accepted:" + cmd.getWhole());
                accepted = this.handleHelpMessage(cmd, BadMessageType.UNHANDLED, accepted);
            }
        } else {
            // The message was not recognized
            this.log(Level.FINE, "Message was bad");
            accepted = this.handleHelpMessage(cmd, BadMessageType.UNRECOGNIZED, accepted);
        }
        if (!accepted.isHandled()) {
            this.log(Level.WARNING, "Command really not accepted/recognized:" + cmd.getWhole());
            this.handleHelpMessage(cmd, BadMessageType.OTHER, accepted);
        }
        return accepted;
    }

    @Override
    public Consumer<GameEvent> getAcceptHook() {
        return (event) -> {
            this.logger.entering(this.getClass().getName(), "sendMsg()", event);
            if (this.out == null) {
                this.SetOut(new LoggerSendStrategy(this.logger, Level.FINER));
            }
            this.out.send(event);
        };
    }

    @Override
    public synchronized void log(Level logLevel, String logMessage) {
        this.logger.log(logLevel, logMessage);
    }

    @Override
    public synchronized void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.logger.log(logLevel, logMessageSupplier);
    }

    void disconnect() throws IOException {
    }

    @Override
    public GameEventProcessorID getEventProcessorID() {
        return this.gameEventProcessorID;
    }

    @Override
    public ClientID getClientID() {
        return this.id;
    }

    private class HelpHandler implements CommandHandler {

        @Override
        public AMessageType getHandleType() {
            return AMessageType.HELP;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of("Tells you the commands that you can use.  They are case insensitive!");
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return CommandHandler.defaultPredicate;
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Client.this;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            Reply reply = CommandChainHandler.passUpChain(Client.this, ctx, null); // this will collect all the helps
            Client.eventAccepter.accept(Client.this,
                    HelpNeededEvent.getHelpBuilder().setHelps(reply.getHelps()).Build());
            return reply.resolve();
        }

    }

    private CommandContext.Reply handleHelpMessage(Command msg, BadMessageType badMessageType,
            CommandContext.Reply reply) {
        Map<AMessageType, String> helps = reply.getHelps();

        if (badMessageType != null) {
            Client.eventAccepter.accept(this,
                    BadMessageEvent.getBuilder().setBadMessageType(badMessageType).setHelps(helps).setCommand(msg)
                            .Build());
        } else {
            Client.eventAccepter.accept(this,
                    HelpNeededEvent.getHelpBuilder().setHelps(helps).setSingleHelp(msg == null ? null : msg.getType())
                            .Build());
        }
        return reply.resolve();
    }

    @Override
    public void setSuccessor(CommandChainHandler successor) {
        this._successor = successor;
    }

    @Override
    public CommandChainHandler getSuccessor() {
        return this._successor;
    }

    @Override
    public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
        Map<AMessageType, CommandHandler> cmdMap = new EnumMap<>(AMessageType.class);
        cmdMap.put(AMessageType.HELP, this.helpHandler);
        return cmdMap;
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        if (ctx.getClient() == null) {
            ctx.setClient(this);
        }
        return ctx;
    }

    @Override
    public String getStartTag() {
        return "<client>";
    }

    @Override
    public String getEndTag() {
        return "</client>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.id.toString() + this.getEndTag();
    }

    @Override
    public Collection<GameEventProcessor> getGameEventProcessors() {
        return Set.of();
    }

}
