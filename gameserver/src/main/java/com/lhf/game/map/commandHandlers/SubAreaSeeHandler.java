package com.lhf.game.map.commandHandlers;

import java.util.Optional;

import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.SeeMessage;

public class SubAreaSeeHandler implements SubAreaCommandHandler {
    private static final String helpString = "\"see\" Will give you some information about the area immediately around you.\r\n";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.SEE;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(SubAreaSeeHandler.helpString);
    }

    @Override
    public Reply visit(CommandContext ctx, SeeMessage command) {
        if (command == null) {
            return ctx.failhandle();
        }
        final SubArea first = this.firstSubArea(ctx);
        if (first == null) {
            return ctx.failhandle();
        }
        if (first.getArea() != null && command.getThing() != null) {
            return first.getArea().applyChain(ctx, command);
        }
        ctx.receive(first.produceMessage());
        return ctx.handled();
    }

}