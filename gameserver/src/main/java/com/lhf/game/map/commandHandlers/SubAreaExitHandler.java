package com.lhf.game.map.commandHandlers;

import java.util.Optional;
import java.util.logging.Level;

import com.lhf.game.map.Area;
import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;

public class SubAreaExitHandler implements SubAreaCommandHandler {
    private static final String helpString = "**ENTIRELY** Disconnect and leave Ibaif!";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.EXIT;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(SubAreaExitHandler.helpString);
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            this.log(Level.WARNING, String.format("%s is full-out EXITING sub-area(s)", ctx));
            SubArea first = null;
            for (final SubArea subArea : ctx.getSubAreas()) {
                if (subArea == null) {
                    continue;
                } else if (first == null) {
                    first = subArea;
                }
                subArea.removeCreature(ctx.getCreature());
            }
            if (first != null) {
                Area area = first.getArea();
                if (area != null) {
                    return area.handleChain(ctx, cmd);
                }
                return CommandChainHandler.passUpChain(first, ctx, cmd);
            }
            return ctx.handled();
        }
        return ctx.failhandle();
    }

}