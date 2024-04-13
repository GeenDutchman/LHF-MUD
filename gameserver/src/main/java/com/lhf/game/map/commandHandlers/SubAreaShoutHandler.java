package com.lhf.game.map.commandHandlers;

import java.util.Optional;

import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaCommandHandler;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.ShoutMessage;

public class SubAreaShoutHandler implements SubAreaCommandHandler {
    private static final String helpString = "Shouts stuff to the people in the land.";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.SHOUT;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(SubAreaShoutHandler.helpString);
    }

    @Override
    public Reply visit(CommandContext ctx, ShoutMessage command) {
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