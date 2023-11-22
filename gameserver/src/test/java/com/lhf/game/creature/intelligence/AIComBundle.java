package com.lhf.game.creature.intelligence;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.mockito.Mock;
import org.mockito.Mockito;

import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.events.GameEventContext;
import com.lhf.game.events.GameEventHandler;
import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;
import com.lhf.server.client.ComBundle;

public class AIComBundle extends ComBundle implements GameEventHandler {
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
    public GameEventHandler mockedWrappedHandler;

    public AIComBundle() {
        super();
        this.mockedWrappedHandler = Mockito.mock(GameEventHandler.class);

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
    public void setSuccessor(GameEventHandler successor) {
        // no -op
    }

    @Override
    public GameEventHandler getSuccessor() {
        return null;
    }

    @Override
    public Map<CommandMessage, String> getCommands(GameEventContext ctx) {
        return new EnumMap<>(CommandMessage.class);
    }

    @Override
    public GameEventContext addSelfToContext(GameEventContext ctx) {
        return ctx;
    }

    @Override
    public GameEventContext.Reply handleMessage(GameEventContext ctx, GameEvent msg) {
        this.print(msg.toString(), true);
        this.mockedWrappedHandler.handleMessage(ctx, msg);
        return ctx.handled();
    }

}
