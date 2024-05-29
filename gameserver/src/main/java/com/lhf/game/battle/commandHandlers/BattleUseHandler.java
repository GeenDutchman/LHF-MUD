package com.lhf.game.battle.commandHandlers;

import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.game.battle.BattleManager.PooledBattleManagerCommandHandler;
import com.lhf.game.item.Usable;
import com.lhf.game.map.SubArea;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.PooledMessageChainHandler;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.UseMessage;

public class BattleUseHandler implements PooledBattleManagerCommandHandler {
    private final static String helpString = new StringJoiner(" ").add("\"use [itemname]\"")
            .add("Uses an item that you have on yourself, if applicable.").add("Like \"use potion\"").add("\r\n")
            .add("\"use [itemname] on [otherthing]\"")
            .add("Uses an item that you have on something or someone else, if applicable.")
            .add("Like \"use potion on Bob\"").toString();

    @Override
    public AMessageType getHandleType() {
        return AMessageType.USE;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(BattleUseHandler.helpString);
    }

    @Override
    public boolean isEnabled(CommandContext ctx) {
        return PooledBattleManagerCommandHandler.super.isEnabled(ctx)
                && ctx.getCreature().getItems().stream().anyMatch(item -> item != null && item instanceof Usable);
    }

    @Override
    public Reply visit(CommandContext ctx, UseMessage command) {
        if (command == null) {
            return ctx.failhandle();
        }
        if (ctx.getCreature() == null) {
            ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                    .setHelps(ctx.getHelps()).setCommand(command).Build());
            return ctx.handled();
        }
        final SubArea first = this.firstSubArea(ctx);
        if (first == null) {
            return ctx.failhandle();
        }
        if (first.getArea() != null) {
            return first.getArea().applyChain(ctx, command);
        }
        return PooledMessageChainHandler.flushUpChain(first, ctx, command);
    }

}