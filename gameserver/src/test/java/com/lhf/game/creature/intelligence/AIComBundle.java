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
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.server.client.ComBundle;

public class AIComBundle extends ComBundle implements MessageChainHandler {
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
    public MessageChainHandler mockedWrappedHandler;

    public AIComBundle() {
        super();
        this.mockedWrappedHandler = Mockito.mock(MessageChainHandler.class);

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
    public void setSuccessor(MessageChainHandler successor) {
        // no -op
    }

    @Override
    public MessageChainHandler getSuccessor() {
        return null;
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        return new EnumMap<>(CommandMessage.class);
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return ctx;
    }

    @Override
    public Reply handleChain(CommandContext ctx, Command cmd) {
        this.print(cmd.toString(), true);
        this.mockedWrappedHandler.handleChain(ctx, cmd);
        return ctx.handled();
    }

}
