package com.lhf.game.creature.commandHandlers;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import com.lhf.game.creature.CreatureVisitor;
import com.lhf.game.creature.DungeonMaster;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.ICreature.CreatureCommandHandler;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.Monster;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.SummonedMonster;
import com.lhf.game.creature.SummonedNPC;
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
    public Reply visit(CommandContext ctx, FollowMessage command) {
        if (command == null) {
            return ctx.failhandle();
        }
        ICreature creature = ctx.getCreature();
        if (creature == null) {
            return ctx.failhandle();
        }

        AtomicReference<Reply> reply = new AtomicReference<>(ctx.failhandle());

        CreatureVisitor followfilter = new CreatureVisitor() {

            @Override
            public void visit(Player player) {
                reply.set(ctx.failhandle());
            }

            @Override
            public void visit(NonPlayerCharacter npc) {
                reply.set(FollowHandler.this.handleFor(ctx, npc, command));
            }

            @Override
            public void visit(DungeonMaster dungeonMaster) {
                reply.set(FollowHandler.this.handleFor(ctx, dungeonMaster, command));
            }

            @Override
            public void visit(SummonedNPC sNpc) {
                reply.set(FollowHandler.this.handleFor(ctx, sNpc, command));
            }

            @Override
            public void visit(Monster monster) {
                reply.set(FollowHandler.this.handleFor(ctx, monster, command));
            }

            @Override
            public void visit(SummonedMonster sMonster) {
                reply.set(FollowHandler.this.handleFor(ctx, sMonster, command));
            }

        };
        followfilter.accept(creature);
        return reply.get();
    }

}