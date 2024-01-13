package com.lhf.game.creature.commandHandlers;

import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.ICreature.CreatureCommandHandler;
import com.lhf.game.item.Equipable;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.EquipMessage;

public class EquipHandler implements CreatureCommandHandler {
    private static String helpString = new StringJoiner(" ")
            .add("\"equip [item]\"").add("Equips the item from your inventory to its default slot").add("\r\n")
            .add("\"equip [item] to [slot]\"")
            .add("Equips the item from your inventory to the specified slot, if such exists.")
            .add("In the unlikely event that either the item or the slot's name contains 'to', enclose the name in quotation marks.")
            .toString();

    @Override
    public AMessageType getHandleType() {
        return AMessageType.EQUIP;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(EquipHandler.helpString);
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
        return creature.getItems().stream().anyMatch(item -> item != null && item instanceof Equipable);
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            EquipMessage equipMessage = new EquipMessage(cmd);
            ICreature creature = ctx.getCreature();
            creature.equipItem(equipMessage.getItemName(), equipMessage.getEquipSlot());
            return ctx.handled();
        }
        return ctx.failhandle();
    }

    @Override
    public CommandChainHandler getChainHandler(CommandContext ctx) {
        return ctx.getCreature();
    }

}