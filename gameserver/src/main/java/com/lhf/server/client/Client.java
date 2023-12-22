package com.lhf.server.client;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.out.BadMessage;
import com.lhf.messages.out.BadMessage.BadMessageType;
import com.lhf.messages.out.HelpMessage;
import com.lhf.messages.out.OutMessage;

public class Client implements MessageChainHandler, ClientMessenger {
    protected SendStrategy out;
    protected final ClientID id;
    protected Logger logger;
    protected transient MessageChainHandler _successor;
    protected final HelpHandler helpHandler = new HelpHandler();

    protected Client() {
        this.id = new ClientID();
        this.logger = Logger
                .getLogger(String.format("%s.%d", this.getClass().getName(), this.getClientID().hashCode()));
        this.log(Level.FINEST,
                () -> String.format("Creating client %s.%d", this.getClass().getName(), this.getClientID().hashCode()));
        this._successor = null;
        this.out = null;
    }

    public void SetOut(SendStrategy out) {
        this.out = out;
    }

    public CommandContext.Reply ProcessString(String value) {
        this.log(Level.FINE, "message received: " + value);
        Command cmd = CommandBuilder.parse(value);
        CommandContext ctx = new CommandContext();
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
    public synchronized void receive(OutMessage msg) {
        this.logger.entering(this.getClass().getName(), "sendMsg()", msg);
        if (this.out == null) {
            this.SetOut(new LoggerSendStrategy(this.logger, Level.FINER));
        }
        this.out.send(msg);
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
    public ClientID getClientID() {
        return this.id;
    }

    private class HelpHandler implements CommandHandler {

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.HELP;
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
        public MessageChainHandler getChainHandler() {
            return Client.this;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            Reply reply = MessageChainHandler.passUpChain(Client.this, ctx, null); // this will collect all the helps
            Client.this.receive(HelpMessage.getHelpBuilder().setHelps(reply.getHelps()));
            return reply.resolve();
        }

    }

    private CommandContext.Reply handleHelpMessage(Command msg, BadMessageType badMessageType,
            CommandContext.Reply reply) {
        Map<CommandMessage, String> helps = reply.getHelps();

        if (badMessageType != null) {
            this.receive(
                    BadMessage.getBuilder().setBadMessageType(badMessageType).setHelps(helps).setCommand(msg).Build());
        } else {
            this.receive(HelpMessage.getHelpBuilder().setHelps(helps).setSingleHelp(msg == null ? null : msg.getType())
                    .Build());
        }
        return reply.resolve();
    }

    @Override
    public void setSuccessor(MessageChainHandler successor) {
        this._successor = successor;
    }

    @Override
    public MessageChainHandler getSuccessor() {
        return this._successor;
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        Map<CommandMessage, CommandHandler> cmdMap = new EnumMap<>(CommandMessage.class);
        cmdMap.put(CommandMessage.HELP, this.helpHandler);
        return cmdMap;
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        if (ctx.getClientID() == null) {
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

}
