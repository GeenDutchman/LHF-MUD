package com.lhf.server.client;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.BadMessage;
import com.lhf.messages.out.BadMessage.BadMessageType;
import com.lhf.messages.out.HelpMessage;
import com.lhf.messages.out.OutMessage;

public class Client implements MessageHandler, ClientMessenger {
    protected SendStrategy out;
    protected ClientID id;
    protected Logger logger;
    protected transient MessageHandler _successor;

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

    void disconnect() throws IOException {
    }

    @Override
    public ClientID getClientID() {
        return this.id;
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
    public void setSuccessor(MessageHandler successor) {
        this._successor = successor;
    }

    @Override
    public MessageHandler getSuccessor() {
        return this._successor;
    }

    @Override
    public Map<CommandMessage, String> getCommands(CommandContext ctx) {
        Map<CommandMessage, String> cmdMap = new EnumMap<>(CommandMessage.class);
        cmdMap.put(CommandMessage.HELP, "Tells you the commands that you can use.  They are case insensitive!");
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
    public CommandContext.Reply handleMessage(CommandContext ctx, Command msg) {
        ctx = this.addSelfToContext(ctx);
        CommandContext.Reply reply = MessageHandler.super.handleMessage(ctx, msg);
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
