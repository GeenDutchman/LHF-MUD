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
import com.lhf.messages.in.ExitMessage;

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
    public Reply visit(CommandContext ctx, ExitMessage command) {
        if (command == null) {
            return ctx.failhandle();
        }
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
                return area.applyChain(ctx, command);
            }
            return CommandChainHandler.passUpChain(first, ctx, command);
        }
        return ctx.handled();
    }

}