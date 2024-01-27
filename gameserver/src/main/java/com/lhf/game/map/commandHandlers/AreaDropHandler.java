package com.lhf.game.map.commandHandlers;

import java.util.Optional;

import com.lhf.game.ItemContainer;
import com.lhf.game.LockableItemContainer;
import com.lhf.game.item.IItem;
import com.lhf.game.item.AItem;
import com.lhf.game.map.Area.AreaCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.events.ItemDroppedEvent;
import com.lhf.messages.events.ItemDroppedEvent.DropType;
import com.lhf.messages.events.ItemNotPossessedEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.DropMessage;

public class AreaDropHandler implements AreaCommandHandler {
    private static final String helpString = "\"drop [itemname]\" Drop an item that you have. Like \"drop longsword\"";

    @Override
    public AMessageType getHandleType() {
        return AMessageType.DROP;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(AreaDropHandler.helpString);
    }

    @Override
    public boolean isEnabled(CommandContext ctx) {
        return AreaCommandHandler.super.isEnabled(ctx) && ctx.getCreature().getItems().size() > 1;
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            if (ctx.getCreature() == null) {
                ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                        .setHelps(ctx.getHelps()).setCommand(cmd).Build());
                return ctx.handled();
            }
            DropMessage dMessage = new DropMessage(cmd);
            ItemDroppedEvent.Builder dOutMessage = ItemDroppedEvent.getBuilder();
            if (dMessage.getTargets().size() == 0) {
                ctx.receive(dOutMessage.setDropType(DropType.NO_ITEM));
                return ctx.handled();
            }

            ItemContainer container = ctx.getArea();
            dOutMessage.setDestination(container.getName());
            Optional<String> containerName = dMessage.inContainer();
            if (containerName.isPresent()) {
                // takeOutMessage.setSource(containerName.orElse(null));
                dOutMessage.setDestination(containerName.get());
                Optional<ItemContainer> foundContainer = container.getItems().stream()
                        .filter(item -> item != null && item instanceof ItemContainer
                                && item.checkName(containerName.get().replaceAll("^\"|\"$", "")))
                        .map(item -> (ItemContainer) item).findAny();
                if (foundContainer.isEmpty()) {
                    ctx.receive(dOutMessage.setDropType(DropType.BAD_CONTAINER));
                    return ctx.handled();
                } else if (foundContainer.get() instanceof LockableItemContainer liCon) {
                    if (!liCon.canAccess(ctx.getCreature())) {
                        ctx.receive(dOutMessage.setDropType(DropType.LOCKED_CONTAINER));
                        return ctx.handled();
                    }
                    container = liCon.getBypass();
                } else {
                    container = foundContainer.get();
                }
            }
            dOutMessage.setDestination(container.getName());

            for (String itemName : dMessage.getTargets()) {
                Optional<IItem> maybeTakeable = ctx.getCreature().removeItem(itemName);
                if (maybeTakeable.isEmpty()) {
                    ctx.receive(ItemNotPossessedEvent.getBuilder().setItemType(AItem.class.getSimpleName())
                            .setItemName(itemName).Build());
                    continue;
                }
                IItem takeable = maybeTakeable.get();
                container.addItem(takeable);
                ctx.receive(dOutMessage.setDropType(DropType.SUCCESS).setItem(takeable)
                        .setDestination(container.getName()).Build());
            }
            return ctx.handled();
        }
        return ctx.failhandle();
    }

}