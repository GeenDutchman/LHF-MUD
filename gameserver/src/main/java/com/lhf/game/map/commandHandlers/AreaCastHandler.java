package com.lhf.game.map.commandHandlers;

import java.util.Optional;

import com.lhf.game.map.Area.AreaCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.in.AMessageType;

public class AreaCastHandler implements AreaCommandHandler {

    @Override
    public AMessageType getHandleType() {
        return AMessageType.CAST;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.empty();
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (ctx.getCreature() == null) {
            ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                    .setHelps(ctx.getHelps()).setCommand(cmd).Build());
            return ctx.handled();
        }
        return ctx.failhandle(); // let a successor (ThirdPower) handle it
    }

    @Override
    public CommandChainHandler getChainHandler(CommandContext ctx) {
        return ctx.getArea();
    }

}