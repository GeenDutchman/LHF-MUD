package com.lhf.game.map;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.lewd.LewdProduct;
import com.lhf.game.lewd.VrijPartij;
import com.lhf.game.map.commandHandlers.RestingGoHandler;
import com.lhf.game.map.commandHandlers.RestingRestHandler;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.PooledMessageChainHandler;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.LewdEvent;
import com.lhf.messages.events.LewdEvent.LewdOutMessageType;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.LewdInMessage;

public class RestArea extends SubArea {
    public enum LewdStyle {
        PRUDE, QUICKIE, LEISURELY;
    }

    private final LewdStyle lewd;
    private final LewdProduct lewdProduct;
    private final ArrayDeque<VrijPartij> parties;

    public static interface IRestAreaBuildInfo extends ISubAreaBuildInfo {
        public abstract LewdStyle getLewd();

        public abstract LewdProduct getLewdProduct();
    }

    public static final class Builder implements IRestAreaBuildInfo {
        protected final SubAreaBuilderID id;
        protected final String className;
        protected final SubAreaBuilder delegate;
        private LewdStyle lewd;
        private LewdProduct lewdProduct;

        public static Builder getInstance() {
            return new Builder();
        }

        public Builder() {
            this.id = new SubAreaBuilderID();
            this.className = this.getClass().getName();
            this.delegate = new SubAreaBuilder(SubAreaSort.RECUPERATION);
            this.lewd = LewdStyle.PRUDE;
        }

        @Override
        public SubAreaBuilderID getSubAreaBuilderID() {
            return this.id;
        }

        public LewdStyle getLewd() {
            if (this.lewd == null) {
                this.lewd = LewdStyle.PRUDE;
            }
            return lewd;
        }

        public Builder setLewd(LewdStyle lewd) {
            this.lewd = lewd != null ? lewd : LewdStyle.PRUDE;
            return this;
        }

        public LewdProduct getLewdProduct() {
            return lewdProduct;
        }

        public Builder setLewdProduct(LewdProduct lewdProduct) {
            this.lewdProduct = lewdProduct;
            if (lewdProduct != null && LewdStyle.PRUDE.equals(this.getLewd())) {
                this.lewd = LewdStyle.LEISURELY;
            }
            return this;
        }

        @Override
        public SubAreaSort getSubAreaSort() {
            return delegate.getSubAreaSort();
        }

        public Builder setAllowCasting(SubAreaCasting allowCasting) {
            delegate.setAllowCasting(allowCasting);
            return this;
        }

        public SubAreaCasting isAllowCasting() {
            return delegate.isAllowCasting();
        }

        public Builder setWaitMilliseconds(int count) {
            delegate.setWaitMilliseconds(count);
            return this;
        }

        public int getWaitMilliseconds() {
            return delegate.getWaitMilliseconds();
        }

        public Builder addCreatureQuery(CreatureFilterQuery query) {
            delegate.addCreatureQuery(query);
            return this;
        }

        public Builder resetCreatureQueries() {
            delegate.resetCreatureQueries();
            return this;
        }

        public Set<CreatureFilterQuery> getCreatureQueries() {
            return delegate.getCreatureQueries();
        }

        public boolean isQueryOnBuild() {
            return delegate.isQueryOnBuild();
        }

        public Builder setQueryOnBuild(boolean queryOnBuild) {
            delegate.setQueryOnBuild(queryOnBuild);
            return this;
        }

        @Override
        public void acceptBuildInfoVisitor(ISubAreaBuildInfoVisitor visitor) {
            visitor.visit(this);
        }

        public RestArea build(Area area) {
            return new RestArea(this, area);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    protected class RestThread extends RoundThread {
        protected RestThread() {
            super(RestArea.this.getName());
        }

        @Override
        public void onThreadStart() {
            // does nothing
        }

        @Override
        public void onRoundStart() {
            ItemInteractionEvent.Builder iom = ItemInteractionEvent.getBuilder().setPerformed();
            final long minutes = TimeUnit.MINUTES.convert(RestArea.this.getTurnWaitCount(), TimeUnit.MILLISECONDS);
            final long seconds = TimeUnit.SECONDS
                    .convert(RestArea.this.getTurnWaitCount() - (TimeUnit.MINUTES.toMillis(minutes)),
                            TimeUnit.MILLISECONDS);
            iom.setDescription(String.format("This round %d of resting will complete in %d m %d s", this.getPhase(),
                    minutes, seconds));
            RestArea.eventAccepter.accept(RestArea.this, iom.Build());
            synchronized (RestArea.this.parties) {
                final VrijPartij first = RestArea.this.parties.peekFirst();
                if (first != null) {
                    first.propose();
                }
            }
        }

        @Override
        public void onRoundEnd() {
            this.logger.log(Level.FINE, "Ending round");
            RestArea.this.flush();
            final Collection<ICreature> creatures = RestArea.this.getCreatures();
            if (creatures == null || creatures.isEmpty()) {
                this.logger.log(Level.FINE, "No creatures found, ending thread");
                this.killIt();
                return;
            }
            for (final ICreature creature : creatures) {
                if (creature == null || !creature.isAlive() || creature.isInBattle()) {
                    continue;
                }
                EnumSet<Attributes> sleepAttrs = EnumSet.of(Attributes.CON, Attributes.INT);
                Attributes best = creature.getHighestAttributeBonus(sleepAttrs);
                MultiRollResult sleepCheck = creature.check(best);
                creature.updateHitpoints(sleepCheck.getTotal());
                final Vocation creatureVocation = creature.getVocation();
                if (creatureVocation != null) {
                    creatureVocation.onRestTick();
                }
                ItemInteractionEvent.Builder iom = ItemInteractionEvent.getBuilder().setPerformed()
                        .setDescription(String.format("You slept and got back %s hit points, leaving you %s!",
                                sleepCheck.getColorTaggedName(), creature.getHealthBucket().getColorTaggedName()))
                        .setTaggable(RestArea.this);
                ICreature.eventAccepter.accept(creature, iom.Build());
            }
        }

        @Override
        public void onThreadEnd() {
            this.killIt();
            synchronized (RestArea.this.roundThread) {
                RestArea.this.roundThread.set(null);
            }
        }

        @Override
        protected void onRegister(ICreature creature) {
            // does nothing
        }

        @Override
        protected void onArriaval(ICreature creature) {
            // does nothing
        }

        @Override
        protected void onArriveAndDeregister(ICreature creature) {
            // does nothing
        }
    }

    protected RestArea(IRestAreaBuildInfo builder, Area area) {
        super(builder, area);
        this.lewd = builder.getLewd();
        this.lewdProduct = builder.getLewdProduct();
        this.parties = new ArrayDeque<>();
        if (!LewdStyle.PRUDE.equals(this.lewd)) {
            this.cmds.put(AMessageType.LEWD, new RestingLewdHandler());
            this.cmds.put(AMessageType.PASS, new RestingPassHandler());
        }
    }

    @Override
    public boolean onCreatureDeath(ICreature creature) {
        if (creature == null || creature.isAlive()) {
            return false;
        }
        ICreature.announceDeath(creature); // forward it
        return true;
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder("This is a rest area.");
        if (!LewdStyle.PRUDE.equals(this.lewd)) {
            sb.append("...");
        }
        if (this.hasRunningThread("printDescription")) {
            sb.append(" There is resting going on.");
        }
        return sb.toString();
    }

    @Override
    public SeeEvent produceMessage(SeeEvent.Builder seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeEvent.getBuilder().setExaminable(this);
        }
        for (final ICreature creature : this.getCreatures()) {
            if (creature == null || !creature.getSubAreaSorts().contains(this.getSubAreaSort())) {
                continue;
            }
            seeOutMessage.addSeen("Resting", creature);
        }
        return super.produceMessage(seeOutMessage);
    }

    @Override
    public String getStartTag() {
        return "<rest_area>";
    }

    @Override
    public String getEndTag() {
        return "</rest_area>";
    }

    @Override
    public boolean empool(ICreature key, IPoolEntry entry) {
        if (key == null || entry == null) {
            this.log(Level.WARNING, "Cannot emplace with null key or pool");
            return false;
        }
        synchronized (this.actionPools) {
            this.addCreature(key);
            Deque<IPoolEntry> queue = this.actionPools.get(key);
            if (queue != null) {
                return queue.offerLast(entry);
            }
        }
        return false;
    }

    @Override
    public ICreature keyFromContext(CommandContext ctx) {
        return ctx.getCreature();
    }

    @Override
    public boolean isReadyToFlush(ICreature key) {
        Deque<IPoolEntry> pool = this.actionPools.get(key);
        return pool == null || pool.size() >= MAX_POOLED_ACTIONS;
    }

    @Override
    public synchronized void flush() {
        synchronized (this.actionPools) {
            this.log(Level.FINER, "Now flushing actionpool");
            this.actionPools.entrySet().stream().forEach(entry -> {
                final ICreature creature = entry.getKey();
                if (creature == null || !creature.isAlive()
                        || !creature.getSubAreaSorts().contains(SubAreaSort.RECUPERATION)) {
                    this.log(Level.WARNING, () -> String
                            .format("Creature %s is dead or not in rest area, cannot perform action", creature));
                    return;
                }
                final Deque<IPoolEntry> poolEntries = entry.getValue();
                if (poolEntries != null && poolEntries.size() > 0) {
                    while (poolEntries.size() > 0 && creature.isAlive()
                            && creature.getSubAreaSorts().contains(SubAreaSort.RECUPERATION)) {
                        final IPoolEntry poolEntry = poolEntries.pollFirst();
                        if (poolEntry != null) {
                            this.handleFlushChain(poolEntry.getContext(), poolEntry.getCommand());
                        }
                    }
                }
            });
            synchronized (this.parties) {
                final VrijPartij first = this.parties.pollFirst();
                if (first != null) {
                    if (first.check() && this.lewdProduct != null) {
                        this.lewdProduct.onLewd(area, first);
                    }
                }
            }
        }

    }

    @Override
    public synchronized RoundThread instigate(ICreature instigator, Collection<ICreature> others) {
        synchronized (this.roundThread) {
            this.addCreature(instigator);
            if (others != null) {
                for (final ICreature creature : others) {
                    this.addCreature(creature);
                }
            }
            RoundThread curThread = this.getRoundThread();
            if (curThread == null || !curThread.getIsRunning()) {
                this.log(Level.INFO, "Starting rest area thread");
                RestThread thread = new RestThread();
                thread.start();
                this.roundThread.set(thread);
            }
            return this.getRoundThread();
        }
    }

    @Override
    protected EnumMap<AMessageType, CommandHandler> buildCommands() {
        EnumMap<AMessageType, CommandHandler> cmds = new EnumMap<>(
                SubArea.SubAreaCommandHandler.subAreaCommandHandlers);
        cmds.putAll(RestingCommandHandler.restingCommandHandlers);
        cmds.put(AMessageType.STATS, new RestingStatsHandler());
        return cmds;
    }

    @Override
    public void onAreaEntry(ICreature creature) {
        // they need to voluntarily join the rest
    }

    @Override
    protected boolean basicAddCreature(ICreature creature) {
        synchronized (this.actionPools) {
            if (creature == null || this.hasCreature(creature)
                    || !this.getSubAreaSort().canBeAdded(creature.getSubAreaSorts())) {
                return false;
            }
            if (this.actionPools.putIfAbsent(creature, new LinkedBlockingDeque<>(MAX_POOLED_ACTIONS)) == null) {
                creature.addSubArea(this.getSubAreaSort());
                creature.setSuccessor(this);
                synchronized (this.roundThread) {
                    if (this.hasRunningThread(String.format("basicAddCreature(%s)", creature.getName()))) {
                        RoundThread thread = this.roundThread.get();
                        if (thread != null) {
                            synchronized (thread) {
                                thread.register(creature);
                            }
                        }
                    } else {
                        this.instigate(creature, Set.of());
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    protected boolean basicRemoveCreature(ICreature creature) {
        if (creature == null) {
            return false;
        }
        synchronized (this.actionPools) {
            this.actionPools.remove(creature);
            RoundThread thread = this.getRoundThread();
            if (thread != null && thread.isAlive()) {
                synchronized (thread) {
                    thread.arriveAndDeregister(creature);
                }
            }
            return true;
        }
    }

    public interface RestingCommandHandler extends SubAreaCommandHandler {

        final static EnumMap<AMessageType, CommandHandler> restingCommandHandlers = new EnumMap<>(
                Map.of(AMessageType.GO, new RestingGoHandler(),
                        AMessageType.REST, new RestingRestHandler()));

        @Override
        default boolean isEnabled(CommandContext ctx) {
            if (ctx == null) {
                return false;
            }
            ICreature creature = ctx.getCreature();
            if (creature == null || !creature.isAlive()) {
                return false;
            }
            final EnumSet<SubAreaSort> cSubs = ctx.getCreature().getSubAreaSorts();
            if (cSubs == null) {
                return false;
            }
            for (final SubAreaSort sort : cSubs) {
                if (!ctx.hasSubAreaSort(sort)) {
                    return false;
                }
            }
            final SubArea first = this.firstSubArea(ctx);
            return first.getSubAreaSort() == SubAreaSort.RECUPERATION;
        }

    }

    private class RestingStatsHandler implements RestingCommandHandler {
        private final static String helpString = "\"stats\" Retrieves the statistics about the current battle.";

        @Override
        public AMessageType getHandleType() {
            return AMessageType.STATS;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(RestingStatsHandler.helpString);
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            synchronized (RestArea.this.parties) {
                final VrijPartij first = RestArea.this.parties.peekFirst();
                LewdEvent.Builder builder = LewdEvent.getBuilder().setNotBroadcast()
                        .setSubType(LewdOutMessageType.STATUS);
                if (first != null) {
                    builder.setParty(first.getParty()).setBabyNames(first.getNames()).setCreature(first.getInitiator());
                }
                ctx.receive(builder);
            }
            return ctx.handled();
        }

        @Override
        public CommandChainHandler getChainHandler(CommandContext ctx) {
            return RestArea.this;
        }
    }

    protected interface PooledRestingCommandHandler extends PooledCommandHandler {

        default SubArea firstSubArea(CommandContext ctx) {
            for (final SubArea subArea : ctx.getSubAreas()) {
                if (subArea != null) {
                    return subArea;
                }
            }
            return null;
        }

        @Override
        public default boolean isEnabled(CommandContext ctx) {
            if (ctx == null) {
                return false;
            }
            ICreature creature = ctx.getCreature();
            if (creature == null || !creature.isAlive()) {
                return false;
            }
            final EnumSet<SubAreaSort> cSubs = ctx.getCreature().getSubAreaSorts();
            if (cSubs == null || cSubs.isEmpty()) {
                return false;
            }
            for (final SubAreaSort sort : cSubs) {
                if (!ctx.hasSubAreaSort(sort)) {
                    return false;
                }
            }
            final SubArea first = this.firstSubArea(ctx);
            return first.getSubAreaSort() == SubAreaSort.RECUPERATION;
        }

        @Override
        default boolean isPoolingEnabled(CommandContext ctx) {
            final SubArea first = this.firstSubArea(ctx);
            if (first == null || first.getSubAreaSort() != SubAreaSort.RECUPERATION) {
                return false;
            }
            return this.isEnabled(ctx) && first.hasRunningThread(this.getClass().getName() + "::isPoolingEnabled(ctx)");
        }

        @Override
        default PooledMessageChainHandler<?> getPooledChainHandler(CommandContext ctx) {
            return ctx.getSubAreaForSort(SubAreaSort.RECUPERATION);
        }

        @Override
        default boolean onEmpool(CommandContext ctx, boolean empoolResult) {
            RoundThread thread = ctx.getSubAreaForSort(SubAreaSort.RECUPERATION).getRoundThread();
            if (thread != null) {
                synchronized (thread) {
                    if (empoolResult) {
                        thread.arrive(ctx.getCreature());
                    }
                }
            }
            return empoolResult;
        }
    }

    private class RestingLewdHandler implements PooledRestingCommandHandler {
        private final static String helpString = "\"lewd [creature]\" lewd another person in the bed";

        @Override
        public AMessageType getHandleType() {
            return AMessageType.LEWD;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(helpString);
        }

        @Override
        public boolean isEnabled(CommandContext ctx) {
            return PooledRestingCommandHandler.super.isEnabled(ctx) && !LewdStyle.PRUDE.equals(RestArea.this.lewd)
                    && ctx.getCreature().getEquipped(EquipmentSlots.ARMOR) == null;
        }

        @Override
        public boolean isPoolingEnabled(CommandContext ctx) {
            return PooledRestingCommandHandler.super.isPoolingEnabled(ctx)
                    && LewdStyle.LEISURELY.equals(RestArea.this.lewd);
        }

        private Reply handlePartnered(CommandContext ctx, LewdInMessage lewdInMessage, Set<String> partners) {
            LewdEvent.Builder lewdOutMessage = LewdEvent.getBuilder();

            TreeSet<ICreature> invites = new TreeSet<>();

            for (final String inviteName : partners) {
                Optional<ICreature> result = RestArea.this.getCreatures().stream()
                        .filter(creature -> creature != null
                                && creature.getEquipmentSlots().get(EquipmentSlots.ARMOR) == null
                                && creature.checkName(inviteName))
                        .findFirst();
                if (result == null || result.isEmpty()) {
                    lewdOutMessage.setNotBroadcast().setSubType(LewdOutMessageType.NOT_READY);
                    ctx.receive(lewdOutMessage);
                    return ctx.handled();
                }
                invites.add(result.get());
            }
            VrijPartij party = new VrijPartij(ctx.getCreature(), invites);
            party.addNames(lewdInMessage.getNames());
            synchronized (RestArea.this.parties) {
                final ArrayDeque<VrijPartij> parties = RestArea.this.parties;
                final VrijPartij first = parties.peekFirst();
                if (first != null && first.equals(party)) {
                    first.accept(ctx.getCreature());
                    if (LewdStyle.QUICKIE.equals(RestArea.this.lewd) && first.check()) {
                        parties.pollFirst(); // take it off the queue
                        if (RestArea.this.lewdProduct != null) {
                            RestArea.this.lewdProduct.onLewd(RestArea.this.area, first);
                        }
                    }
                } else if (!RestArea.this.parties.contains(party)) {
                    parties.addLast(party);
                    if (LewdStyle.QUICKIE.equals(RestArea.this.lewd)) {
                        party.propose();
                    }
                } else {
                    // if it is contained in the list, say it's already proposed
                }
            }
            return ctx.handled();
        }

        @Override
        public Reply flushHandle(CommandContext ctx, Command cmd) {
            if (cmd != null && AMessageType.LEWD.equals(cmd.getType())) {
                final LewdInMessage lewdInMessage = new LewdInMessage(cmd);
                Set<String> partners = lewdInMessage.getPartners();
                if (partners != null && partners.size() > 0) {
                    return this.handlePartnered(ctx, lewdInMessage, partners);
                }
                synchronized (RestArea.this.parties) {
                    final ArrayDeque<VrijPartij> parties = RestArea.this.parties;
                    final VrijPartij first = parties.peekFirst();
                    if (first != null) {
                        first.accept(ctx.getCreature());
                        if (LewdStyle.QUICKIE.equals(RestArea.this.lewd) && first.check()) {
                            parties.pollFirst(); // take it off the queue
                            if (RestArea.this.lewdProduct != null) {
                                RestArea.this.lewdProduct.onLewd(RestArea.this.area, first);
                            }
                        }
                    } else {
                        ctx.receive(LewdEvent.getBuilder().setCreature(ctx.getCreature()).setNotBroadcast()
                                .setSubType(LewdOutMessageType.SOLO_UNSUPPORTED));
                    }
                }
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler(CommandContext ctx) {
            return RestArea.this;
        }
    }

    private class RestingPassHandler implements PooledRestingCommandHandler {
        private final static String helpString = "\"pass\" to decline the current lewdness";

        @Override
        public AMessageType getHandleType() {
            return AMessageType.LEWD;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(RestingPassHandler.helpString);
        }

        @Override
        public boolean isEnabled(CommandContext ctx) {
            return PooledRestingCommandHandler.super.isEnabled(ctx) && RestArea.this.parties.size() > 0;
        }

        @Override
        public CommandChainHandler getChainHandler(CommandContext ctx) {
            return RestArea.this;
        }

        @Override
        public Reply flushHandle(CommandContext ctx, Command cmd) {
            if (cmd == null || !AMessageType.PASS.equals(cmd.getType())) {
                return ctx.failhandle();
            }
            synchronized (RestArea.this.parties) {
                final VrijPartij party = RestArea.this.parties.peek();
                if (party != null) {
                    party.pass(ctx.getCreature());
                }
            }
            return ctx.handled();
        }

    }
}
