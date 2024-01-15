package com.lhf.game.map.commandHandlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Level;

import com.lhf.game.creature.ICreature;
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemNameSearchVisitor;
import com.lhf.game.item.Usable;
import com.lhf.game.map.Area.AreaCommandHandler;
import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.BadTargetSelectedEvent.BadTargetOption;
import com.lhf.messages.events.ItemUsedEvent;
import com.lhf.messages.events.ItemUsedEvent.UseOutMessageOption;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.UseMessage;

public class AreaUseHandler implements AreaCommandHandler {

    private final static String helpString = new StringJoiner(" ")
            .add("\"use [itemname]\"").add("Uses an item that you have on yourself, if applicable.")
            .add("Like \"use potion\"").add("\r\n")
            .add("\"use [itemname] on [otherthing]\"")
            .add("Uses an item that you have on something or someone else, if applicable.")
            .add("Like \"use potion on Bob\"")
            .toString();

    @Override
    public AMessageType getHandleType() {
        return AMessageType.USE;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(AreaUseHandler.helpString);
    }

    @Override
    public boolean isEnabled(CommandContext ctx) {
        return AreaCommandHandler.super.isEnabled(ctx)
                && ctx.getCreature().getItems().stream().anyMatch(item -> item != null && item instanceof Usable);
    }

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd == null || cmd.getType() != this.getHandleType()) {
            return ctx.failhandle();
        }
        if (ctx.getCreature() == null) {
            ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                    .setHelps(ctx.getHelps()).setCommand(cmd).Build());
            return ctx.handled();
        }
        UseMessage useMessage = new UseMessage(cmd);
        ItemNameSearchVisitor visitor = new ItemNameSearchVisitor(useMessage.getUsefulItem());
        ctx.getCreature().acceptItemVisitor(visitor);
        Optional<Usable> maybeItem = visitor.getUsable();
        if (maybeItem.isEmpty()) {
            ctx.receive(ItemUsedEvent.getBuilder().setSubType(UseOutMessageOption.NO_USES)
                    .setItemUser(ctx.getCreature()).Build());
            return ctx.handled();
        }
        Usable usable = maybeItem.get();
        if (useMessage.getTarget() == null || useMessage.getTarget().isBlank()) {
            usable.doUseAction(ctx, ctx.getCreature());
            return ctx.handled();
        }
        Collection<ICreature> maybeCreature = ctx.getArea().getCreaturesLike(useMessage.getTarget());
        if (maybeCreature.size() == 1) {
            List<ICreature> creatureList = new ArrayList<>(maybeCreature);
            ICreature targetCreature = creatureList.get(0);
            // if we aren't in battle, but our target is in battle, join the battle
            if (!ctx.getCreature().isInBattle() && targetCreature.isInBattle()) {
                final SubArea subArea = ctx.getArea().getSubAreaForSort(SubAreaSort.BATTLE);
                if (subArea == null) {
                    this.log(Level.SEVERE, String.format(
                            "How can we target someone in battle without the Room having a battle sub area? %s",
                            ctx.getArea().getSubAreas()));
                    return ctx.failhandle();
                }
                subArea.addCreature(ctx.getCreature());
                return subArea.handleChain(ctx, cmd);
            }
            usable.doUseAction(ctx, creatureList.get(0));
            return ctx.handled();
        } else if (maybeCreature.size() > 1) {
            ctx.receive(BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.UNCLEAR)
                    .setBadTarget(useMessage.getTarget()).setPossibleTargets(maybeCreature).Build());
            return ctx.handled();
        }
        Optional<Item> maybeRoomItem = ctx.getArea().getItem(useMessage.getTarget());
        if (maybeRoomItem.isPresent()) {
            usable.doUseAction(ctx, maybeRoomItem.get());
            return ctx.handled();
        }
        Optional<Item> maybeInventory = ctx.getCreature().getItem(useMessage.getTarget());
        if (maybeInventory.isPresent()) {
            usable.doUseAction(ctx, maybeInventory.get());
            return ctx.handled();
        }
        ctx.receive(BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.UNCLEAR)
                .setBadTarget(useMessage.getTarget()).Build());
        return ctx.handled();

    }

}