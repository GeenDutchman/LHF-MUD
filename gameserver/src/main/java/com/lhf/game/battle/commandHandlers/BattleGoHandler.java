package com.lhf.game.battle.commandHandlers;

import java.util.Map;
import java.util.Optional;

import com.lhf.game.battle.BattleManager.PooledBattleManagerCommandHandler;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandChainHandler.CommandHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BattleCreatureFledEvent;
import com.lhf.messages.in.AMessageType;

public class BattleGoHandler implements PooledBattleManagerCommandHandler {
    private final static String helpString = "\"go [direction]\" Try to move in the desired direction and flee the battle, if that direction exists.  Like \"go east\"";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.GO;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(BattleGoHandler.helpString);
    }

    @Override
    public boolean isEnabled(CommandContext ctx) {
        if (!PooledBattleManagerCommandHandler.super.isEnabled(ctx)) {
            return false;
        }
        final SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
        CommandChainHandler chainHandler = bm.getArea();
        CommandContext copyContext = ctx.copy();

        // this whole block means: if someone further up in the chain has GO, then I
        // have GO, else not
        while (chainHandler != null) {
            copyContext = chainHandler.addSelfToContext(copyContext);
            Map<AMessageType, CommandHandler> handlers = chainHandler.getCommands(copyContext);
            if (handlers != null) {
                CommandHandler handler = handlers.get(AMessageType.GO);
                if (handler != null && handler.isEnabled(copyContext)) {
                    return true;
                }
            }
            chainHandler = chainHandler.getSuccessor();
        }
        return false;
    }

    @Override
    public Reply flushHandle(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            final SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
            Integer check = 10 + bm.getCreatures().size();
            MultiRollResult result = ctx.getCreature().check(Attributes.DEX);
            BattleCreatureFledEvent.Builder builder = BattleCreatureFledEvent.getBuilder()
                    .setRunner(ctx.getCreature())
                    .setRoll(result);
            Reply reply = null;
            if (result.getRoll() >= check) {
                reply = CommandChainHandler.passUpChain(bm, ctx, cmd);
            }
            if (bm.hasCreature(ctx.getCreature())) { // if it is still here, it failed to flee
                builder.setFled(false);
                ctx.receive(builder.setFled(false).setNotBroadcast().Build());
                if (bm.getArea() != null) {
                    bm.getArea().announce(builder.setBroacast().Build(), ctx.getCreature());
                } else {
                    bm.announce(builder.setBroacast().Build(), ctx.getCreature());
                }
            } else {
                builder.setFled(true).setBroacast();
                if (bm.getArea() != null) {
                    bm.getArea().announce(builder.Build(), ctx.getCreature());
                } else {
                    bm.announce(builder.Build(), ctx.getCreature());
                }
            }
            return reply != null ? reply.resolve() : ctx.handled();
        }
        return ctx.failhandle();
    }

}