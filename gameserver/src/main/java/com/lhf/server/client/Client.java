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
    protected final Logger logger;
    protected transient MessageChainHandler _successor;
    protected final HelpHandler helpHandler = new HelpHandler();

    protected Client() {
        this.id = new ClientID();
        this.logger = Logger
                .getLogger(String.format("%s.%d", this.getClass().getName(), this.getClientID().hashCode()));
        this.logger.log(Level.FINEST,
                () -> String.format("Creating client %s.%d", this.getClass().getName(), this.getClientID().hashCode()));
        this._successor = null;
        this.out = null;
    }

    public void SetOut(SendStrategy out) {
        this.out = out;
    }

    public CommandContext.Reply ProcessString(String value) {
        this.logger.log(Level.FINE, "message received: " + value);
        Command cmd = CommandBuilder.parse(value);
        CommandContext ctx = new CommandContext();
        CommandContext.Reply accepted = ctx.failhandle();
        if (cmd.isValid()) {
            this.logger.log(Level.FINEST, "the message received was deemed" + cmd.getClass().toString());
            this.logger.log(Level.FINER, "Post Processing:" + cmd);
            accepted = MessageChainHandler.passUpChain(this, ctx, cmd);
            if (!accepted.isHandled()) {
                this.logger.log(Level.WARNING, "Command not accepted:" + cmd.getWhole());
                accepted = this.handleHelpMessage(cmd, BadMessageType.UNHANDLED, accepted);
            }
        } else {
            // The message was not recognized
            this.logger.log(Level.FINE, "Message was bad");
            accepted = this.handleHelpMessage(cmd, BadMessageType.UNRECOGNIZED, accepted);
        }
        if (!accepted.isHandled()) {
            this.logger.log(Level.WARNING, "Command really not accepted/recognized:" + cmd.getWhole());
            this.handleHelpMessage(cmd, BadMessageType.OTHER, accepted);
        }
        return accepted;
    }

    @Override
    public synchronized void sendMsg(OutMessage msg) {
        this.logger.entering(this.getClass().getName(), "sendMsg()", msg);
        if (this.out == null) {
            this.SetOut(new PrintWriterSendStrategy(System.out));
        }
        this.out.send(msg);
    }

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

        private Predicate<CommandContext> enabledPredicate = (ctx) -> true;

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.HELP;
        }

        @Override
        public boolean isEnabled(CommandContext ctx) {
            return this.enabledPredicate.test(ctx);
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of("Tells you the commands that you can use.  They are case insensitive!");
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return this.enabledPredicate;
        }

        @Override
        public MessageChainHandler getChainHandler() {
            return Client.this;
        }

        @Override
        public boolean setEnabledPredicate(Predicate<CommandContext> predicate) {
            if (predicate == null) {
                this.enabledPredicate = (ctx) -> true;
                return false;
            } else {
                this.enabledPredicate = predicate;
            }
            return true;
        }

        @Override
        public Reply handle(CommandContext ctx, Command cmd) {
            Reply reply = MessageChainHandler.passUpChain(Client.this, ctx, null); // this will collect all the helps
            Client.this.sendMsg(HelpMessage.getHelpBuilder().setHelps(reply.getHelps()));
            return reply.resolve();
        }

    }

    private CommandContext.Reply handleHelpMessage(Command msg, BadMessageType badMessageType,
            CommandContext.Reply reply) {
        Map<CommandMessage, String> helps = reply.getHelps();

        if (badMessageType != null) {
            this.sendMsg(
                    BadMessage.getBuilder().setBadMessageType(badMessageType).setHelps(helps).setCommand(msg).Build());
        } else {
            this.sendMsg(HelpMessage.getHelpBuilder().setHelps(helps).setSingleHelp(msg == null ? null : msg.getType())
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

    public CommandContext.Reply handleChain(CommandContext ctx, Command msg) {
        ctx = this.addSelfToContext(ctx);
        CommandContext.Reply reply = MessageChainHandler.super.handleChain(ctx, msg);
        if (msg.getType() == CommandMessage.HELP) {
            return this.handleHelpMessage(null, null, reply);
        } else if (!reply.isHandled()) {
            return this.handleHelpMessage(msg, BadMessageType.UNHANDLED, reply);
        }

        return reply;
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
