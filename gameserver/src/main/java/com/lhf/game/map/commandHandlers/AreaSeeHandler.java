package com.lhf.game.map.commandHandlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.Examinable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Item;
import com.lhf.game.map.Area.AreaCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.SeeMessage;

// only used to examine items and creatures in this room
public class AreaSeeHandler implements AreaCommandHandler {

    private final static String helpString = new StringJoiner(" ")
            .add("\"see\"").add("Will give you some information about your surroundings.\r\n")
            .add("\"see [name]\"").add("May tell you more about the object with that name.")
            .toString();

    @Override
    public AMessageType getHandleType() {
        return AMessageType.SEE;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(AreaSeeHandler.helpString);
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            SeeMessage sMessage = new SeeMessage(cmd);
            if (sMessage.getThing() != null && !sMessage.getThing().isBlank()) {
                String name = sMessage.getThing();
                Collection<ICreature> found = ctx.getArea().getCreaturesLike(name);
                // we should be able to see people in a fight
                if (found.size() == 1) {
                    ArrayList<ICreature> foundList = new ArrayList<ICreature>(found);
                    ctx.receive(((SeeEvent.Builder) foundList.get(0).produceMessage().copyBuilder())
                            .addExtraInfo("They are in the room with you. ").Build());
                    return ctx.handled();
                }

                if (ctx.getCreature() != null && ctx.getCreature().isInBattle()) {
                    ctx.receive(SeeEvent.getBuilder()
                            .setDeniedReason("You are in a fight right now, you are too busy to examine that!")
                            .Build());
                    return ctx.handled();
                }

                for (IItem ro : ctx.getArea().getItems()) {
                    if (ro.CheckNameRegex(name, 3)) {
                        ctx.receive(ro.produceMessage(SeeEvent.getBuilder().setExaminable(ro)
                                .addExtraInfo("You see it in the room with you. ")));
                        return ctx.handled();
                    }
                }

                if (ctx.getCreature() != null) {
                    ICreature creature = ctx.getCreature();
                    for (Item thing : creature.getEquipmentSlots().values()) {
                        if (thing.CheckNameRegex(name, 3)) {
                            if (thing instanceof Examinable) {
                                ctx.receive(((SeeEvent.Builder) thing.produceMessage().copyBuilder())
                                        .addExtraInfo("You have it equipped. ").Build());
                                return ctx.handled();
                            }
                            ctx.receive(SeeEvent.getBuilder().setExaminable(thing)
                                    .addExtraInfo("You have it equipped. ").Build());
                            return ctx.handled();
                        }
                    }

                    Optional<IItem> maybeThing = creature.getInventory().getItem(name);
                    if (maybeThing.isPresent()) {
                        IItem thing = maybeThing.get();
                        if (thing instanceof Examinable) {
                            ctx.receive(((SeeEvent.Builder) thing.produceMessage().copyBuilder())
                                    .addExtraInfo("You see it in your inventory. ").Build());
                            return ctx.handled();
                        }
                        ctx.receive(SeeEvent.getBuilder().setExaminable(thing)
                                .addExtraInfo("You see it in your inventory. ").Build());
                        return ctx.handled();
                    }
                }

                ctx.receive(
                        SeeEvent.getBuilder().setDeniedReason("You couldn't find " + name + " to examine. ")
                                .Build());
                return ctx.handled();
            } else {
                ctx.receive(ctx.getArea().produceMessage());
                return ctx.handled();
            }
        }
        return ctx.failhandle();
    }
}