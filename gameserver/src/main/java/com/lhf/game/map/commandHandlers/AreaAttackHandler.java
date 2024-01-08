package com.lhf.game.map.commandHandlers;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Level;

import com.lhf.game.map.Area;
import com.lhf.game.map.SubArea;
import com.lhf.game.map.Area.AreaCommandHandler;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.in.AMessageType;

public class AreaAttackHandler implements AreaCommandHandler {
    private final static String helpString = new StringJoiner(" ")
            .add("\"attack [name]\"").add("Attacks a creature").add("\r\n")
            .add("\"attack [name] with [weapon]\"").add("Attack the named creature with a weapon that you have.")
            .add("In the unlikely event that either the creature or the weapon's name contains 'with', enclose the name in quotation marks.")
            .toString();

    @Override
    public AMessageType getHandleType() {
        return AMessageType.ATTACK;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(AreaAttackHandler.helpString);
    }

    @Override
    public boolean isEnabled(CommandContext ctx) {
        if (!AreaCommandHandler.super.isEnabled(ctx)) {
            return false;
        }
        Area room = ctx.getArea();
        if (!room.hasSubAreaSort(SubAreaSort.BATTLE)) {
            room.log(Level.WARNING, () -> String.format("No battle manager for room: %s", room.getName()));
            return false;
        }
        return room.getCreatures().size() > 1;
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd == null || cmd.getType() != this.getHandleType()) {
            return ctx.failhandle();
        }
        ctx = ctx.getArea().addSelfToContext(ctx);
        if (ctx.getCreature() == null) {
            ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                    .setHelps(ctx.getHelps()).setCommand(cmd).Build());
            return ctx.handled();
        }
        final SubArea subArea = ctx.getArea().getSubAreaForSort(SubAreaSort.BATTLE);
        if (subArea == null) {
            this.log(Level.WARNING, "No battle sub area found!");
            return ctx.failhandle();
        }
        return subArea.handleChain(ctx, cmd);
    }

    @Override
    public CommandChainHandler getChainHandler(CommandContext ctx) {
        return ctx.getArea();
    }

}