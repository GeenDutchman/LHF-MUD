package com.lhf.game.creature.commandHandlers;

import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.ICreature.CreatureCommandHandler;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.UnequipMessage;

public class UnequipHandler implements CreatureCommandHandler {
    private static String helpString = new StringJoiner(" ").add("\"unequip [item]\"")
            .add("Unequips the item (if equipped) and places it in your inventory").add("\r\n")
            .add("\"unequip [slot]\"")
            .add("Unequips the item that is in the specified slot (if equipped) and places it in your inventory")
            .toString();

    @Override
    public AMessageType getHandleType() {
        return AMessageType.UNEQUIP;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(UnequipHandler.helpString);
    }

    @Override
    public boolean isEnabled(CommandContext ctx) {
        if (ctx == null) {
            return false;
        }
        ICreature creature = ctx.getCreature();
        if (creature == null || !creature.isAlive()) {
            return false;
        }
        return creature.getEquipmentSlots().values().size() > 0;
    }

    @Override
    public Reply visit(CommandContext ctx, UnequipMessage command) {
        if (command == null) {
            return ctx.failhandle();
        }
        ICreature creature = ctx.getCreature();
        creature.unequipItem(EquipmentSlots.getEquipmentSlot(command.getUnequipWhat()), command.getUnequipWhat());
        return ctx.handled();
    }

    @Override
    public CommandChainHandler getChainHandler(CommandContext ctx) {
        return ctx.getCreature();
    }

}