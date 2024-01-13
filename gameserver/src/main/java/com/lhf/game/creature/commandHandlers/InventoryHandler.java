package com.lhf.game.creature.commandHandlers;

import java.util.Optional;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.ICreature.CreatureCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;

public class InventoryHandler implements CreatureCommandHandler {
    private final static String helpString = "\"inventory\" List what you have in your inventory and what you have equipped";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.INVENTORY;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(InventoryHandler.helpString);
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            ICreature creature = ctx.getCreature();
            ctx.receive(creature.getInventory().getInventoryOutMessage(creature.getEquipmentSlots()));
            return ctx.handled();
        }
        return ctx.failhandle();
    }

    @Override
    public CommandChainHandler getChainHandler(CommandContext ctx) {
        return ctx.getCreature();
    }

}