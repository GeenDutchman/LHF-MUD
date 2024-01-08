package com.lhf.game.map.commandHandlers;

import java.util.Optional;
import java.util.logging.Level;

import com.lhf.game.map.Area.AreaCommandHandler;
import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.in.AMessageType;

public class AreaRestHandler implements AreaCommandHandler {

    private final static String helpString = "\"REST\" puts yourself in state of REST, use \"GO UP\" to get out of it";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.REST;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(AreaRestHandler.helpString);
    }

    @Override
    public boolean isEnabled(CommandContext ctx) {
        return AreaCommandHandler.super.isEnabled(ctx) && ctx.getArea().hasSubAreaSort(SubAreaSort.RECUPERATION);
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd == null || cmd.getType() != AMessageType.REST) {
            return ctx.failhandle();
        }
        ctx = ctx.getArea().addSelfToContext(ctx);
        if (ctx.getCreature() == null) {
            ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                    .setHelps(ctx.getHelps()).setCommand(cmd).Build());
            return ctx.handled();
        }
        final SubArea subArea = ctx.getArea().getSubAreaForSort(SubAreaSort.RECUPERATION);
        if (subArea == null) {
            this.log(Level.WARNING, "No rest sub area found!");
            return ctx.failhandle();
        }
        return subArea.handleChain(ctx, cmd);
    }

}