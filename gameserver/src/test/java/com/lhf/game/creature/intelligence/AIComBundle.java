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

import com.lhf.game.creature.CreatureFactory;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.in.AMessageType;
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

    public BasicAI brain;
    private final GameEventProcessorID gameEventProcessorID;
    @Mock
    public CommandChainHandler mockedWrappedHandler;

    public AIComBundle() {
        this(null);
    }

    public AIComBundle(INPCBuildInfo buildInfo) {
        super();
        if (buildInfo == null) {
            buildInfo = NonPlayerCharacter.getNPCBuilder();
        }
        this.gameEventProcessorID = new GameEventProcessorID();
        this.mockedWrappedHandler = Mockito.mock(CommandChainHandler.class);

        this.brain = AIComBundle.getAIRunner().produceAI();
        CreatureFactory factory = CreatureFactory.withBrainProducer(this, (builder) -> this.brain);
        NonPlayerCharacter npc = factory.buildNPC(buildInfo);
        this.brain.setNPC(npc);
        this.brain.SetOut(this.sssb);
    }

    public INonPlayerCharacter getNPC() {
        return this.brain.getNpc();
    }

    @Override
    protected String getName() {
        return super.getName() + ' ' + this.brain.npc.getName();
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
    public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
        return new EnumMap<>(AMessageType.class);
    }

    @Override
    public Collection<GameEventProcessor> getGameEventProcessors() {
        return Set.of();
    }

    @Override
    public void log(Level logLevel, String logMessage) {
        this.brain.log(logLevel, logMessage);
    }

    @Override
    public void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.brain.log(logLevel, logMessageSupplier);
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
