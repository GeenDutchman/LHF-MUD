package com.lhf.game.map.commandHandlers;

import java.util.EnumSet;
import java.util.Optional;

import com.lhf.game.map.Directions;
import com.lhf.game.map.RestArea.RestingCommandHandler;
import com.lhf.game.map.SubArea;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadGoEvent;
import com.lhf.messages.events.BadGoEvent.BadGoType;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.GoMessage;

/**
 * Reduces options to just "Go UP"
 */
public class RestingGoHandler implements RestingCommandHandler {
    private static final String helpString = "Use the command <command>GO UP</command> to get out of bed. ";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.GO;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(RestingGoHandler.helpString);
    }

    @Override
    public Reply visit(CommandContext ctx, GoMessage command) {
        if (command == null) {
            return ctx.failhandle();
        }
        final SubArea ra = this.firstSubArea(ctx);
        if (Directions.UP.equals(command.getDirection())) {
            ra.removeCreature(ctx.getCreature());
            return ctx.handled();
        } else {
            ctx.receive(BadGoEvent.getBuilder().setSubType(BadGoType.DNE).setAttempted(goMessage.getDirection())
                    .setAvailable(EnumSet.of(Directions.UP)).Build());
            return ctx.handled();
        }
    }

}