package com.lhf.game.map.commandHandlers;

import java.util.EnumSet;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.PatternSyntaxException;

import com.lhf.game.ItemContainer;
import com.lhf.game.ItemContainer.ItemFilters;
import com.lhf.game.LockableItemContainer;
import com.lhf.game.item.Item;
import com.lhf.game.item.Takeable;
import com.lhf.game.map.Area.AreaCommandHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.events.ItemTakenEvent;
import com.lhf.messages.events.ItemTakenEvent.TakeOutType;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.TakeMessage;

public class AreaTakeHandler implements AreaCommandHandler {
    private final static String helpString = new StringJoiner(" ").add("\"take [item]\"")
            .add("Take an item from the room and add it to your inventory.\n")
            .add("\"take [item] from \"[someone]'s corpse\"")
            .add("Take an item from a container of some kind, just double-quote the container name")
            .toString();

    @Override
    public AMessageType getHandleType() {
        return AMessageType.TAKE;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        if (ctx == null || ctx.getCreature() == null) {
            return Optional.empty();
        }
        return Optional
                .of(ctx.getCreature().isInBattle() ? AreaTakeHandler.inBattleString : AreaTakeHandler.helpString);
    }

    @Override
    public boolean isEnabled(CommandContext ctx) {
        return AreaCommandHandler.super.isEnabled(ctx) && ctx.getArea()
                .filterItems(EnumSet.of(ItemFilters.TYPE), null, null, null, Takeable.class, null).size() > 0;
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == AMessageType.TAKE) {
            if (ctx.getCreature() == null) {
                ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                        .setHelps(ctx.getHelps()).setCommand(cmd).Build());
                return ctx.handled();
            }
            TakeMessage tMessage = new TakeMessage(cmd);

            ItemTakenEvent.Builder takeOutMessage = ItemTakenEvent.getBuilder();

            ItemContainer container = ctx.getArea();
            takeOutMessage.setSource(container);
            Optional<String> containerName = tMessage.fromContainer();
            if (containerName.isPresent()) {
                takeOutMessage.setSource(containerName.orElse(null));
                Optional<ItemContainer> foundContainer = container.getItems().stream()
                        .filter(item -> item != null && item instanceof ItemContainer
                                && item.checkName(containerName.get().replaceAll("^\"|\"$", "")))
                        .map(item -> (ItemContainer) item).findAny();
                if (foundContainer.isEmpty()) {
                    ctx.receive(takeOutMessage.setSubType(TakeOutType.BAD_CONTAINER).Build());
                    return ctx.handled();
                }
                if (foundContainer.get() instanceof LockableItemContainer liCon) {
                    if (!liCon.canAccess(ctx.getCreature())) {
                        ctx.receive(takeOutMessage.setSubType(TakeOutType.LOCKED_CONTAINER).Build());
                        return ctx.handled();
                    }
                    container = liCon.getBypass();
                } else {
                    container = foundContainer.get();
                }
            }

            for (String thing : tMessage.getTargets()) {
                takeOutMessage.setAttemptedName(thing);
                if (thing.length() < 3) {
                    ctx.receive(takeOutMessage.setSubType(TakeOutType.SHORT).Build());
                    continue;
                }
                if (thing.matches("[^ a-zA-Z_-]+") || thing.contains("*")) {
                    ctx.receive(takeOutMessage.setSubType(TakeOutType.INVALID).Build());
                    continue;
                }
                try {
                    Optional<Item> maybeItem = container.getItems().stream()
                            .filter(item -> item.CheckNameRegex(thing, 3))
                            .findAny();
                    if (maybeItem.isEmpty()) {
                        if (thing.equalsIgnoreCase("all") || thing.equalsIgnoreCase("everything")) {
                            ctx.receive(takeOutMessage.setSubType(TakeOutType.GREEDY).Build());
                        } else {
                            ctx.receive(takeOutMessage.setSubType(TakeOutType.NOT_FOUND).Build());
                        }
                        continue;
                    }
                    Item item = maybeItem.get();
                    takeOutMessage.setItem(item);
                    if (item instanceof Takeable takeableItem) {
                        ctx.getCreature().addItem(takeableItem);
                        container.removeItem(takeableItem);
                        ctx.receive(takeOutMessage.setSubType(TakeOutType.FOUND_TAKEN).Build());
                        continue;
                    }
                    ctx.receive(takeOutMessage.setSubType(TakeOutType.NOT_TAKEABLE).Build());
                } catch (PatternSyntaxException pse) {
                    pse.printStackTrace();
                    ctx.receive(takeOutMessage.setSubType(TakeOutType.UNCLEVER).Build());
                }
            }
            while (container instanceof LockableItemContainer.Bypass bypass) {
                container = bypass.getOrigin();
            }
            if (container instanceof LockableItemContainer liCon && liCon instanceof Item liConItem) {
                if (liCon.isRemoveOnEmpty() && liCon.isEmpty()) {
                    ctx.getArea().removeItem(liConItem);
                }
            }
            return ctx.handled();
        }
        return ctx.failhandle();
    }

    @Override
    public CommandChainHandler getChainHandler(CommandContext ctx) {
        return ctx.getArea();
    }
}