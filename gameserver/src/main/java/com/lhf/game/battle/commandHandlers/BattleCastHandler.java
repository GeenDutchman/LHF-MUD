package com.lhf.game.battle.commandHandlers;

import java.util.Optional;

import com.lhf.game.battle.BattleManager.PooledBattleManagerCommandHandler;
import com.lhf.game.map.SubArea;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.PooledMessageChainHandler;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.in.AMessageType;

public class BattleCastHandler implements PooledBattleManagerCommandHandler {

    @Override
    public AMessageType getHandleType() {
        return AMessageType.CAST;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.empty();
    }

    @Override
    public Reply flushHandle(CommandContext ctx, Command cmd) {
        if (ctx.getCreature() == null) {
            ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                    .setHelps(ctx.getHelps()).setCommand(cmd).Build());
            return ctx.handled();
        }
        final SubArea first = this.firstSubArea(ctx);
        if (first == null) {
            return ctx.failhandle();
        }
        if (first.getArea() != null) {
            return first.getArea().handleChain(ctx, cmd);
        }
        return PooledMessageChainHandler.flushUpChain(first, ctx, cmd);
    }

}
