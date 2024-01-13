package com.lhf.game.map.commandHandlers;

import java.util.Optional;

import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;

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
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == AMessageType.SHOUT) {
            final SubArea first = this.firstSubArea(ctx);
            if (first == null) {
                return ctx.failhandle();
            }
            if (first.getArea() != null) {
                return first.getArea().handleChain(ctx, cmd);
            }
            return CommandChainHandler.passUpChain(first, ctx, cmd);
        }
        return ctx.failhandle();
    }

}