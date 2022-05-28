package com.lhf.server.client;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandInParser;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.GameMessage;
import com.lhf.messages.out.OutMessage;

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
        Command cmd = CommandInParser.parse(value);
        Boolean accepted = false;
        if (cmd.isValid()) {
            this.logger.finest("the message received was deemed" + cmd.getClass().toString());
            this.logger.finer("Post Processing:" + cmd);
            accepted = this.handleMessage(null, cmd);
            if (!accepted) {
                this.logger.warning("Command not accepted:" + cmd.getWhole());
                StringBuilder sb = new StringBuilder();
                sb.append("That command \"").append(cmd.getWhole()).append("\" was not handled.\n")
                        .append("Here are the available commands:\r\n");
                this.sendMsg(new GameMessage(sb.toString()));
                accepted = this.handleHelpMessage(cmd);
            }
        } else {
            // The message was not recognized
            StringBuilder sb = new StringBuilder();
            sb.append("That command \"").append(cmd.getWhole()).append("\" was not recognized.\n")
                    .append("Here are the available commands:\r\n");
            this.logger.fine("Message was bad");
            this.sendMsg(new GameMessage(sb.toString()));
            accepted = this.handleHelpMessage(cmd);
        }
        if (!accepted) {
            this.logger.warning("Command really not accepted/recognized:" + cmd.getWhole());
            this.sendMsg(new GameMessage("You just have no luck, huh?"));
        }
    }

    @Override
    public synchronized void sendMsg(OutMessage msg) {
        this.logger.entering(this.getClass().toString(), "sendMsg()", msg);
        if (this.out == null) {
            this.SetOut(new PrintWriterSendStrategy(System.out));
        }
        this.out.send(msg.toString());
    }

    void disconnect() throws IOException {
    }

    @Override
    public ClientID getClientID() {
        return this.id;
    }

    private Boolean handleHelpMessage(Command msg) {
        StringBuilder sb = new StringBuilder();
        TreeMap<CommandMessage, String> helps = new TreeMap<>(this.gatherHelp());
        CommandMessage cmd = msg.getType();
        if (cmd != null && helps.containsKey(cmd)) {
            sb.append(cmd.getColorTaggedName()).append(":").append("\r\n").append("<description>")
                    .append(helps.get(cmd)).append("</description>").append("\r\n");
        } else {
            for (CommandMessage cmdMsg : helps.keySet()) {
                sb.append(cmdMsg.getColorTaggedName()).append(":").append("\r\n").append("<description>")
                        .append(helps.get(cmdMsg)).append("</description>").append("\r\n");
            }
        }

        this.sendMsg(new GameMessage(sb.toString()));
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
            return this.handleHelpMessage(msg);
        }

        return MessageHandler.super.handleMessage(ctx, msg);
    }

}
