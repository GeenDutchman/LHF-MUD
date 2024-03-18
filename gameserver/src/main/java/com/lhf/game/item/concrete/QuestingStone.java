package com.lhf.game.item.concrete;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

import com.lhf.game.CreatureContainer.CreatureFilterQuery;
import com.lhf.game.CreatureContainer.CreatureFilters;
import com.lhf.game.creature.DungeonMaster;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.QuestSource;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Usable;
import com.lhf.game.map.Area;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.ItemUsedEvent;
import com.lhf.messages.events.ItemUsedEvent.UseOutMessageOption;

public class QuestingStone extends Usable {

    private final static Predicate<ICreature> produceUserLimiter() {
        CreatureFilterQuery queryA = new CreatureFilterQuery();
        queryA.filters = EnumSet.of(CreatureFilters.TYPE);
        queryA.clazz = NonPlayerCharacter.class;
        CreatureFilterQuery queryB = new CreatureFilterQuery();
        queryB.filters = EnumSet.of(CreatureFilters.TYPE);
        queryB.clazz = DungeonMaster.class;
        return queryA.or(queryB);
    }

    public QuestingStone(QuestSource source) {
        super(source.getName(), Set.of(source));
        this.descriptionString = String.format("Questiong stone for the quest: %s", source.getName());
    }

    @Override
    public boolean useOn(CommandContext ctx, ICreature creature) {
        final Predicate<ICreature> checker = QuestingStone.produceUserLimiter();
        if (checker != null && !checker.test(ctx.getCreature())) {
            ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature())
                    .setUsable(this).setTarget(creature).setSubType(UseOutMessageOption.NO_USES)
                    .setMessage("You cannot use this.");
            ctx.receive(useOutMessage);
            return false;
        }
        return super.useOn(ctx, creature);
    }

    @Override
    public boolean useOn(CommandContext ctx, Area area) {
        final Predicate<ICreature> checker = QuestingStone.produceUserLimiter();
        if (checker != null && !checker.test(ctx.getCreature())) {
            ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature())
                    .setUsable(this).setTarget(area).setSubType(UseOutMessageOption.NO_USES)
                    .setMessage("You cannot use this.");
            ctx.receive(useOutMessage);
            return false;
        }
        return super.useOn(ctx, area);
    }

    @Override
    public boolean useOn(CommandContext ctx, IItem item) {
        final Predicate<ICreature> checker = QuestingStone.produceUserLimiter();
        if (checker != null && !checker.test(ctx.getCreature())) {
            ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature())
                    .setUsable(this).setTarget(item).setSubType(UseOutMessageOption.NO_USES)
                    .setMessage("You cannot use this.");
            ctx.receive(useOutMessage);
            return false;
        }
        return super.useOn(ctx, item);
    }

}
