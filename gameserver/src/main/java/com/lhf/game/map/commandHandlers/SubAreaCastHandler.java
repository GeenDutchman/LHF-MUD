package com.lhf.game.map.commandHandlers;

import java.util.Optional;

import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.PooledSubAreaCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.PooledMessageChainHandler;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.in.AMessageType;

public class SubAreaCastHandler implements PooledSubAreaCommandHandler {
    private final boolean immediateFlush;

    public SubAreaCastHandler() {
        this.immediateFlush = false;
    }

    public SubAreaCastHandler(boolean immediateFlush) {
        this.immediateFlush = immediateFlush;
    }

    @Override
    public AMessageType getHandleType() {
        return AMessageType.CAST;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.empty();
    }

    @Override
    public boolean isPoolingEnabled(CommandContext ctx) {
        if (this.immediateFlush) {
            return false;
        }
        return PooledSubAreaCommandHandler.super.isPoolingEnabled(ctx);
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