package com.lhf.game.map.commandHandlers;

import java.util.Optional;

import com.lhf.game.map.RestArea.RestingCommandHandler;
import com.lhf.game.map.SubArea;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;

public class RestingRestHandler implements RestingCommandHandler {
    private final static String helpString = "\"REST\" puts yourself in state of REST, use \"GO UP\" to get out of it";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.REST;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(RestingRestHandler.helpString);
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd == null || !AMessageType.REST.equals(cmd.getType())) {
            return ctx.failhandle();
        }
        final SubArea ra = this.firstSubArea(ctx);
        ra.addCreature(ctx.getCreature());
        return ctx.handled();
    }

}