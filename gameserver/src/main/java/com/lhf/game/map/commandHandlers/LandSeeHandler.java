package com.lhf.game.map.commandHandlers;

import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.game.map.Area;
import com.lhf.game.map.Land.LandCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.in.AMessageType;

public class LandSeeHandler implements LandCommandHandler {
    private final static String helpString = new StringJoiner(" ")
            .add("\"see\"").add("Will give you some information about your surroundings.\r\n")
            .add("\"see [name]\"").add("May tell you more about the object with that name.")
            .toString();

    @Override
    public AMessageType getHandleType() {
        return AMessageType.SEE;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(LandSeeHandler.helpString);
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            final Area presentRoom = ctx.getArea();
            if (presentRoom != null) {
                SeeEvent roomSeen = presentRoom.produceMessage();
                ctx.receive(roomSeen);
                return ctx.handled();
            }
        }
        return ctx.failhandle();
    }

}