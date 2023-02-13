package com.lhf.game.creature.intelligence;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.mockito.Mock;
import org.mockito.Mockito;

import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.server.client.ComBundle;

public class AIComBundle extends ComBundle implements MessageHandler {
    public static AIRunner aiRunner;

    public static AIRunner getAIRunner() {
        if (AIComBundle.aiRunner == null) {
            AIComBundle.aiRunner = new GroupAIRunner(true, 2, 250, TimeUnit.MILLISECONDS);
        }
        return AIComBundle.aiRunner;
    }

    public static AIRunner setAIRunner(AIRunner aiRunner) {
        AIComBundle.aiRunner = aiRunner;
        return AIComBundle.aiRunner;
    }

    public NonPlayerCharacter npc;
    public BasicAI brain;
    @Mock
    public MessageHandler mockedWrappedHandler;

    public AIComBundle() {
        super();
        this.mockedWrappedHandler = Mockito.mock(MessageHandler.class);

        this.npc = NonPlayerCharacter.getNPCBuilder(AIComBundle.getAIRunner()).build();
        this.brain = AIComBundle.getAIRunner().register(this.npc);
        brain.SetOut(this.sssb);
        this.npc.setController(this.brain);
        this.npc.setSuccessor(this);
    }

    @Override
    protected String getName() {
        return super.getName() + ' ' + this.npc.getName();
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
        this.mockedWrappedHandler.handleMessage(ctx, msg);
        return true;
    }

}
