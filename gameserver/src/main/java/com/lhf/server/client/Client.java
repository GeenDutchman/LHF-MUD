package com.lhf.server.client;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.events.GameEvent;
import com.lhf.game.events.GameEvent.GameEventType;
import com.lhf.game.events.GameEventContext;
import com.lhf.game.events.GameEventContext.Reply;
import com.lhf.game.events.GameEventHandlerNode;
import com.lhf.game.events.messages.ClientMessenger;
import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandBuilder;
import com.lhf.game.events.messages.CommandContext;
import com.lhf.game.events.messages.CommandHandler;
import com.lhf.game.events.messages.CommandMessage;
import com.lhf.game.events.messages.out.BadMessage;
import com.lhf.game.events.messages.out.BadMessage.BadMessageType;
import com.lhf.game.events.messages.out.HelpMessage;
import com.lhf.game.events.messages.out.OutMessage;

public class Client implements GameEventHandlerNode, ClientMessenger {

    private class HelpHandler implements CommandHandler {

        @Override
        public boolean isEnabled(GameEventContext ctx) {
            return true;
        }

        @Override
        public CommandMessage forType() {
            return CommandMessage.HELP;
        }

        @Override
        public String getHelp(GameEventContext ctx) {
            return "Tells you the commands that you can use.  They are case insensitive!";
        }

        @Override
        public Reply handle(GameEventContext ctx, GameEvent event) {
            return Client.this.handleHelpMessage((Command) event, null, CommandHandler.gatherHelps(Client.this));
        }

    }

    protected SendStrategy out;
    protected final ClientID id;
    protected final Logger logger;
    protected transient GameEventHandlerNode _successor;
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

    public GameEventContext.Reply ProcessString(String value) {
        this.logger.log(Level.FINE, "message received: " + value);
        Command cmd = CommandBuilder.parse(value);
        CommandContext ctx = new CommandContext();
        GameEventContext.Reply accepted = ctx.failhandle();
        if (cmd.isValid()) {
            this.logger.log(Level.FINEST, "the message received was deemed" + cmd.getClass().toString());
            this.logger.log(Level.FINER, "Post Processing:" + cmd);
            accepted = this.handleMessage(ctx, cmd);
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

    private GameEventContext.Reply handleHelpMessage(Command msg, BadMessageType badMessageType,
            GameEventContext.Reply reply) {
        Map<CommandMessage, String> helps = reply instanceof CommandContext.Reply
                ? ((CommandContext.Reply) reply).getHelps()
                : Map.of();

        if (badMessageType != null) {
            this.sendMsg(
                    BadMessage.getBuilder().setBadMessageType(badMessageType).setHelps(helps).setCommand(msg).Build());
        } else {
            this.sendMsg(HelpMessage.getHelpBuilder().setHelps(helps)
                    .setSingleHelp(msg == null ? null : msg.getGameEventType())
                    .Build());
        }
        return reply.resolve();
    }

    @Override
    public void setSuccessor(GameEventHandlerNode successor) {
        this._successor = successor;
    }

    @Override
    public GameEventHandlerNode getSuccessor() {
        return this._successor;
    }

    @Override
    public Map<GameEventType, GameEventTypeHandler> getHandlers(GameEventContext ctx) {
        Map<GameEventType, GameEventTypeHandler> cmdMap = new TreeMap<>();
        cmdMap.put(CommandMessage.HELP, this.helpHandler);
        return cmdMap;
    }

    @Override
    public GameEventContext addSelfToContext(GameEventContext ctx) {
        if (ctx == null) {
            ctx = new GameEventContext();
        }
        if (ctx.getClientID() == null) {
            ctx.setClient(this);
        }
        return ctx;
    }

    @Override
    public GameEventContext.Reply handleMessage(GameEventContext ctx, GameEvent msg) {
        ctx = this.addSelfToContext(ctx);
        GameEventContext.Reply reply = GameEventHandlerNode.super.handleMessage(ctx, msg);
        if (msg.getGameEventType() == CommandMessage.HELP) {
            return this.handleHelpMessage(null, null, reply);
        } else if (!reply.isHandled()) {
            return this.handleHelpMessage((Command) msg, BadMessageType.UNHANDLED, reply);
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
