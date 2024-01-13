package com.lhf.game.map.commandHandlers;

import java.util.Optional;

import com.lhf.game.map.Land;
import com.lhf.game.map.Land.LandCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.SpeakingEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.ShoutMessage;

public class LandShoutHandler implements LandCommandHandler {
    private final static String helpString = "\"shout [message]\" Tells everyone in the dungeon your message!";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.SHOUT;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(LandShoutHandler.helpString);
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            if (ctx.getCreature() == null) {
                ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                        .setHelps(ctx.getHelps()).setCommand(cmd).Build());
                return ctx.handled();
            }
            final ShoutMessage shoutMessage = new ShoutMessage(cmd);
            final Land land = ctx.getLand();
            land.announceDirect(
                    SpeakingEvent.getBuilder().setSayer(ctx.getCreature()).setShouting(true)
                            .setMessage(shoutMessage.getMessage()).Build(),
                    land.getPlayers().stream().filter(player -> player != null)
                            .map(player -> (GameEventProcessor) player)
                            .toList());
            return ctx.handled();
        }
        return ctx.failhandle();
    }

}