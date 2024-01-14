package com.lhf.game.map.commandHandlers;

import java.util.List;
import java.util.Optional;

import com.lhf.game.item.InteractObject;
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemPartitionListVisitor;
import com.lhf.game.map.Area.AreaCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.BadTargetSelectedEvent.BadTargetOption;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.ItemInteractionEvent.InteractOutMessageType;
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
        return Optional
                .of(ctx.getCreature().isInBattle() ? AreaInteractHandler.inBattleString
                        : AreaInteractHandler.helpString);
    }

    @Override
    public boolean isEnabled(CommandContext ctx) {
        if (!AreaCommandHandler.super.isEnabled(ctx) || ctx.getCreature().isInBattle()) {
            return false;
        }
        ItemPartitionListVisitor visitor = new ItemPartitionListVisitor();
        ctx.getArea().acceptVisitor(visitor);
        return !visitor.getInteractObjects().isEmpty();
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            InteractMessage intMessage = new InteractMessage(cmd);
            if (ctx.getCreature() == null) {
                ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                        .setHelps(ctx.getHelps()).setCommand(cmd).Build());
                return ctx.handled();
            }
            String name = intMessage.getObject();
            List<Item> matches = ctx.getArea().getItems().stream()
                    .filter(ro -> ro != null && ro.CheckNameRegex(name, 3)).toList();

            if (matches.size() == 1) {
                Item ro = matches.get(0);
                if (ro instanceof InteractObject) {
                    InteractObject ex = (InteractObject) ro;
                    ctx.receive(ex.doUseAction(ctx.getCreature()));
                } else {
                    ctx.receive(ItemInteractionEvent.getBuilder().setTaggable(ro)
                            .setSubType(InteractOutMessageType.CANNOT).Build());
                }
                return ctx.handled();
            }
            List<InteractObject> interactables = ctx.getArea().getItems().stream()
                    .filter(ro -> ro != null && ro.checkVisibility() && ro instanceof InteractObject)
                    .map(ro -> (InteractObject) ro).toList();
            ctx.receive(BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.UNCLEAR).setBadTarget(name)
                    .setPossibleTargets(interactables).Build());
            return ctx.handled();
        }
        return ctx.failhandle();
    }

}