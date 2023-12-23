package com.lhf.game.creature.intelligence;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;

import org.mockito.Mock;
import org.mockito.Mockito;

import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.GameEventProcessor;
import com.lhf.server.client.ComBundle;

public class AIComBundle extends ComBundle implements CommandChainHandler {
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

    public INonPlayerCharacter npc;
    public BasicAI brain;
    private final GameEventProcessorID gameEventProcessorID;
    @Mock
    public CommandChainHandler mockedWrappedHandler;

    public AIComBundle() {
        super();
        this.gameEventProcessorID = new GameEventProcessorID();
        this.mockedWrappedHandler = Mockito.mock(CommandChainHandler.class);

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
    public String getColorTaggedName() {
        return this.getStartTag() + this.getName() + this.getEndTag();
    }

    @Override
    public String getEndTag() {
        return "</AIComBundle>";
    }

    @Override
    public String getStartTag() {
        return "<AIComBundle>";
    }

    @Override
    public void setSuccessor(CommandChainHandler successor) {
        // no -op
    }

    @Override
    public CommandChainHandler getSuccessor() {
        return null;
    }

    @Override
    public GameEventProcessorID getEventProcessorID() {
        return this.gameEventProcessorID;
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        return new EnumMap<>(CommandMessage.class);
    }

    @Override
    public Collection<GameEventProcessor> getGameEventProcessors() {
        return Set.of();
    }

    @Override
    public void log(Level logLevel, String logMessage) {
        this.npc.log(logLevel, logMessage);
    }

    @Override
    public void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.npc.log(logLevel, logMessageSupplier);
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return ctx;
    }

    @Override
    public Reply handleChain(CommandContext ctx, Command cmd) {
        return this.mockedWrappedHandler.handleChain(ctx, cmd);
    }

    @Override
    public Reply handle(CommandContext ctx, Command cmd) {
        this.print(cmd.toString(), true);
        return this.mockedWrappedHandler.handle(ctx, cmd);
    }

}
