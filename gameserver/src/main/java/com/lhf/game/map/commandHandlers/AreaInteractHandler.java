package com.lhf.game.map.commandHandlers;

import java.util.Collection;
import java.util.Optional;

import com.lhf.game.item.InteractObject;
import com.lhf.game.item.ItemNameSearchVisitor;
import com.lhf.game.item.ItemPartitionListVisitor;
import com.lhf.game.map.Area.AreaCommandHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.BadTargetSelectedEvent.BadTargetOption;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.InteractMessage;

public class AreaInteractHandler implements AreaCommandHandler {
    private final static String helpString = "\"interact [item]\" Certain items in the room may be interactable. Like \"interact lever\"";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.INTERACT;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        if (ctx == null || ctx.getCreature() == null) {
            return Optional.empty();
        }
        return Optional.of(
                ctx.getCreature().isInBattle() ? AreaInteractHandler.inBattleString : AreaInteractHandler.helpString);
    }

    @Override
    public boolean isEnabled(CommandContext ctx) {
        if (!AreaCommandHandler.super.isEnabled(ctx) || ctx.getCreature().isInBattle()) {
            return false;
        }
        ItemPartitionListVisitor visitor = new ItemPartitionListVisitor();
        ctx.getArea().acceptItemVisitor(visitor);
        return !visitor.getInteractObjects().isEmpty();
    }

    @Override
    public Reply visit(CommandContext ctx, InteractMessage intMessage) {
        if (intMessage == null) {
            return ctx.failhandle();
        }
        if (ctx.getCreature() == null) {
            ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                    .setHelps(ctx.getHelps()).setCommand(intMessage).Build());
            return ctx.handled();
        }
        String name = intMessage.getObject();
        ItemPartitionListVisitor partitionVisitor = new ItemPartitionListVisitor();
        ctx.getArea().getItems().stream().filter(item -> item != null)
                .forEach(item -> item.acceptItemVisitor(partitionVisitor));
        ItemNameSearchVisitor nameSearchVisitor = new ItemNameSearchVisitor(name, 3);
        nameSearchVisitor.copyFrom(partitionVisitor);
        Collection<InteractObject> matches = nameSearchVisitor.getInteractObjects();

        if (matches.size() == 1) {
            InteractObject ro = matches.stream().findFirst().get();
            ro.doAction(ctx);
            return ctx.handled();
        }
        Collection<InteractObject> interactables = partitionVisitor.getInteractObjects();
        ctx.receive(BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.UNCLEAR).setBadTarget(name)
                .setPossibleTargets(interactables).Build());
        return ctx.handled();
    }

}