package com.lhf.game.creature.commandHandlers;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Level;

import com.lhf.game.creature.ICreature.CreatureCommandHandler;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.FollowMessage;

public class FollowHandler implements CreatureCommandHandler {
    private static String helpString = new StringJoiner(" ").add("\"follow [personName]\"")
            .add("Attemps to set this NPC to follow the person whose name exactly matches.")
            .add("If this NPC is already following a person, this command will fail.").add("\r\n")
            .add("\"follow [person] with override\"").add("Will override any previous following with the current.")
            .toString();

    @Override
    public CommandChainHandler getChainHandler(CommandContext ctx) {
        return ctx.getCreature();
    }

    @Override
    public boolean isEnabled(CommandContext ctx) {
        return CreatureCommandHandler.super.isEnabled(ctx) && ctx.getCreature() instanceof INonPlayerCharacter;
    }

    @Override
    public AMessageType getHandleType() {
        return AMessageType.FOLLOW;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(FollowHandler.helpString);
    }

    private Reply handleFor(CommandContext ctx, INonPlayerCharacter npc, FollowMessage message) {
        if (message.isOverride() || npc.getLeaderName() == null) {
            npc.setLeaderName(message.getPersonToFollow());
        } else {
            npc.log(Level.INFO, () -> String.format("Cannot follow %s because I am already following %s",
                    message.getPersonToFollow(), npc.getLeaderName()));
        }
        return ctx.handled();
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()
                && ctx.getCreature() instanceof INonPlayerCharacter npc) {
            FollowMessage followMessage = new FollowMessage(cmd);
            return this.handleFor(ctx, npc, followMessage);
        }
        return ctx.failhandle();
    }

}