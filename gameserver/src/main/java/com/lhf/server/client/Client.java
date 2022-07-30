package com.lhf.server.client;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.BadMessage;
import com.lhf.messages.out.HelpMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.BadMessage.BadMessageType;

public class Client implements MessageHandler, ClientMessenger {
    protected SendStrategy out;
    protected ClientID id;
    protected Logger logger;
    protected MessageHandler _successor;

    public Client() {
        this.id = new ClientID();
        this.logger = Logger.getLogger(this.getClass().getName());
        this.logger.finest("Creating Client");
        this._successor = null;
        this.out = null;
    }

    public void SetOut(SendStrategy out) {
        this.out = out;
    }

    public void ProcessString(String value) {
        this.logger.fine("message received: " + value);
        Command cmd = CommandBuilder.parse(value);
        Boolean accepted = false;
        if (cmd.isValid()) {
            this.logger.finest("the message received was deemed" + cmd.getClass().toString());
            this.logger.finer("Post Processing:" + cmd);
            accepted = this.handleMessage(null, cmd);
            if (!accepted) {
                this.logger.warning("Command not accepted:" + cmd.getWhole());
                accepted = this.handleHelpMessage(cmd, BadMessageType.UNHANDLED);
            }
        } else {
            // The message was not recognized
            this.logger.fine("Message was bad");
            accepted = this.handleHelpMessage(cmd, BadMessageType.UNRECOGNIZED);
        }
        if (!accepted) {
            this.logger.warning("Command really not accepted/recognized:" + cmd.getWhole());
            this.handleHelpMessage(cmd, BadMessageType.OTHER);
        }
    }

    @Override
    public synchronized void sendMsg(OutMessage msg) {
        this.logger.entering(this.getClass().toString(), "sendMsg()", msg);
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

    private Boolean handleHelpMessage(Command msg, BadMessageType badMessageType) {
        TreeMap<CommandMessage, String> helps = new TreeMap<>(this.gatherHelp());

        if (badMessageType != null) {
            this.sendMsg(new BadMessage(badMessageType, helps, msg));
        } else {
            this.sendMsg(new HelpMessage(helps, msg == null ? null : msg.getType()));
        }
        return true;
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
    public Map<CommandMessage, String> getCommands() {
        Map<CommandMessage, String> cmdMap = new TreeMap<>();
        cmdMap.put(CommandMessage.HELP, "Tells you the commands that you can use.  They are case insensitive!");
        return cmdMap;
    }

    @Override
    public Boolean handleMessage(CommandContext ctx, Command msg) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        ctx.setClient(this);
        if (msg.getType() == CommandMessage.HELP) {
            return this.handleHelpMessage(null, null);
        }

        return MessageHandler.super.handleMessage(ctx, msg);
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
