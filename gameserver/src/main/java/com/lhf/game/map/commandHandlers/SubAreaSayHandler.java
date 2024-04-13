package com.lhf.game.map.commandHandlers;

import java.util.Optional;

import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaCommandHandler;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.SayMessage;

public class SubAreaSayHandler implements SubAreaCommandHandler {
    private static final String helpString = "Says stuff to the people in the area.";

    public SubAreaSayHandler() {
    }

    @Override
    public AMessageType getHandleType() {
        return AMessageType.SAY;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(SubAreaSayHandler.helpString);
    }

    @Override
    public Reply visit(CommandContext ctx, SayMessage command) {
        if (command == null) {
            return ctx.failhandle();
        }
        final SubArea first = this.firstSubArea(ctx);
        if (first == null) {
            return ctx.failhandle();
        }
        if (first.getArea() != null) {
            return first.getArea().applyChain(ctx, command);
        }
        return CommandChainHandler.passUpChain(first, ctx, command);
    }

}