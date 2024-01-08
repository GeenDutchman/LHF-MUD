package com.lhf.game.map.commandHandlers;

import java.util.Optional;

import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;

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
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == AMessageType.SAY) {
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