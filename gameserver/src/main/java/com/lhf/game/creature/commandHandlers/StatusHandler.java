package com.lhf.game.creature.commandHandlers;

import java.util.Optional;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.ICreature.CreatureCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.CreatureStatusRequestedEvent;
import com.lhf.messages.in.AMessageType;

public class StatusHandler implements CreatureCommandHandler {
    private final static String helpString = "\"status\" Show you how much HP you currently have, among other things.";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.STATUS;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(StatusHandler.helpString);
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            ICreature creature = ctx.getCreature();
            ctx.receive(
                    CreatureStatusRequestedEvent.getStatusBuilder().setNotBroadcast().setFromCreature(creature, true)
                            .Build());
            return ctx.handled();
        }
        return ctx.failhandle();
    }

    @Override
    public CommandChainHandler getChainHandler(CommandContext ctx) {
        return ctx.getCreature();
    }

}