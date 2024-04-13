package com.lhf.game.battle.commandHandlers;

import java.util.Optional;

import com.lhf.game.battle.BattleManager.PooledBattleManagerCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.PassMessage;

public class BattlePassHandler implements PooledBattleManagerCommandHandler {

    private static String helpString = "\"pass\" Skips your turn in battle!";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.PASS;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(BattlePassHandler.helpString);
    }

    @Override
    public Reply visit(CommandContext ctx, PassMessage command) {
        if (command == null) {
            return ctx.failhandle();
        }
        return ctx.handled();
    }

}