package com.lhf.game.creature.intelligence;

import java.util.ArrayList;
import java.util.Map;

import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.server.client.StringBufferSendStrategy;

public class AIComBundle implements MessageHandler {
    public NonPlayerCharacter npc;
    public BasicAI brain;
    public StringBufferSendStrategy sssb;
    public ArrayList<Command> sent;

    public AIComBundle() {
        this.npc = new NonPlayerCharacter();
        this.brain = new BasicAI(this.npc);
        this.sssb = new StringBufferSendStrategy();
        brain.SetOut(this.sssb);
        this.npc.setController(this.brain);
        this.sent = new ArrayList<>();
        this.npc.setSuccessor(this);
    }

    public String read() {
        String buffer = this.sssb.read();
        System.out.println("***********************" + this.npc.getName() + "**********************");
        System.out.println(buffer);
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        return buffer;
    }

    public void clear() {
        this.sssb.clear();
    }

    @Override
    public void setSuccessor(MessageHandler successor) {
        // no -op
    }

    @Override
    public MessageHandler getSuccessor() {
        return null;
    }

    @Override
    public Map<CommandMessage, String> getCommands() {
        return null;
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return ctx;
    }

    @Override
    public Boolean handleMessage(CommandContext ctx, Command msg) {
        System.out.println(msg.toString());
        this.sent.add(msg);
        return true;
    }

}
