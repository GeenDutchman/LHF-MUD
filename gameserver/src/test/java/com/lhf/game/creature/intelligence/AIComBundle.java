package com.lhf.game.creature.intelligence;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.server.client.StringBufferSendStrategy;

public class AIComBundle implements MessageHandler {
    public static AIRunner aiRunner;

    public static AIRunner getAIRunner() {
        if (AIComBundle.aiRunner == null) {
            AIComBundle.aiRunner = new GroupAIRunner(true);
        }
        return AIComBundle.aiRunner;
    }

    public static AIRunner setAIRunner(AIRunner aiRunner) {
        AIComBundle.aiRunner = aiRunner;
        return AIComBundle.aiRunner;
    }

    public NonPlayerCharacter npc;
    public StringBufferSendStrategy sssb;
    public ArrayList<Command> sent;
    public BasicAI brain;

    public AIComBundle() {
        this.npc = new NonPlayerCharacter();
        this.brain = AIComBundle.getAIRunner().register(this.npc);
        this.sssb = new StringBufferSendStrategy();
        brain.SetOut(this.sssb);
        this.npc.setController(this.brain);
        this.sent = new ArrayList<>();
        this.npc.setSuccessor(this);
    }

    protected void print(String buffer, boolean sending) {
        System.out.println("***********************" + this.npc.getName() + "**********************");
        for (String part : buffer.split("\n")) {
            System.out.print(sending ? ">>> " : "<<< ");
            System.out.println(part);
        }
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    }

    public String read() {
        String buffer = this.sssb.read();
        this.print(buffer, false);
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
        return new EnumMap<>(CommandMessage.class);
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return ctx;
    }

    @Override
    public boolean handleMessage(CommandContext ctx, Command msg) {
        this.print(msg.toString(), true);
        this.sent.add(msg);
        return true;
    }

}
