package com.lhf.game.map.commandHandlers;

import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.game.creature.ICreature;
import com.lhf.game.map.Area.AreaCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadSpeakingTargetEvent;
import com.lhf.messages.events.SpeakingEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.SayMessage;
import com.lhf.server.client.CommandInvoker;

public class AreaSayHandler implements AreaCommandHandler {

    private static final String helpString = new StringJoiner(" ")
            .add("\"say [message]\"").add("Tells everyone in your current room your message").add("\r\n")
            .add("\"say [message] to [name]\"")
            .add("Will tell a specific person somewhere in your current room your message.")
            .add("If your message contains the word 'to', put your message in quotes like")
            .add("\"say 'They are taking the hobbits to Isengard' to Aragorn\"")
            .add("\r\n")
            .toString();

    @Override
    public AMessageType getHandleType() {
        return AMessageType.SAY;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(AreaSayHandler.helpString);
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            SayMessage sMessage = new SayMessage(cmd);
            SpeakingEvent.Builder speakMessage = SpeakingEvent.getBuilder().setSayer(ctx.getCreature())
                    .setMessage(sMessage.getMessage());
            if (sMessage.getTarget() != null) {
                boolean sent = false;
                Optional<ICreature> optTarget = ctx.getArea().getCreature(sMessage.getTarget());
                if (optTarget.isPresent()) {
                    CommandInvoker sayer = ctx.getClient();
                    if (ctx.getCreature() != null) {
                        sayer = ctx.getCreature();
                    } else if (ctx.getUser() != null) {
                        sayer = ctx.getUser();
                    }
                    ICreature target = optTarget.get();
                    speakMessage.setSayer(sayer).setHearer(target);
                    ICreature.eventAccepter.accept(target, speakMessage.Build());
                    sent = true;
                }
                if (!sent) {
                    ctx.receive(BadSpeakingTargetEvent.getBuilder().setCreatureName(sMessage.getTarget()));
                }
            } else {
                ctx.getArea().announce(speakMessage.Build());
            }
            return ctx.handled();
        }
        return ctx.failhandle();
    }

}