package com.lhf.game.battle.commandHandlers;

import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.game.battle.BattleManager.PooledBattleManagerCommandHandler;
import com.lhf.game.item.Usable;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;

public class BattleUseHandler implements PooledBattleManagerCommandHandler {
    private final static String helpString = new StringJoiner(" ")
            .add("\"use [itemname]\"").add("Uses an item that you have on yourself, if applicable.")
            .add("Like \"use potion\"").add("\r\n")
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
    public Reply flushHandle(CommandContext ctx, Command cmd) {
        // TODO: #127 test me!
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            Reply reply = CommandChainHandler.passUpChain(this.firstSubArea(ctx), ctx, cmd);
            return reply;
        }
        return ctx.failhandle();
    }

}