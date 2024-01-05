package com.lhf.game.map;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.lewd.LewdProduct;
import com.lhf.game.lewd.VrijPartij;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.PooledMessageChainHandler;
import com.lhf.messages.events.BadGoEvent;
import com.lhf.messages.events.BadGoEvent.BadGoType;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.LewdEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.LewdEvent.LewdOutMessageType;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.LewdInMessage;

public class RestArea extends SubArea {
    private final boolean lewd;
    private final LewdProduct lewdProduct;
    private final ArrayDeque<VrijPartij> parties;

    public static class Builder extends SubAreaBuilder<RestArea, Builder> {
        private boolean lewd;
        private LewdProduct lewdProduct;

        public static Builder getInstance() {
            return new Builder();
        }

        public Builder() {
            this.lewd = false;
        }

        public boolean isLewd() {
            return lewd;
        }

        public Builder setLewd(boolean lewd) {
            this.lewd = lewd;
            return this.getThis();
        }

        public LewdProduct getLewdProduct() {
            return lewdProduct;
        }

        public Builder setLewdProduct(LewdProduct lewdProduct) {
            this.lewdProduct = lewdProduct;
            if (lewdProduct != null) {
                this.lewd = true;
            }
            return this.getThis();
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public SubAreaSort getSubAreaSort() {
            return SubAreaSort.RECUPERATION;
        }

        @Override
        public RestArea build(Area area) {
            return new RestArea(this, area);
        }

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

    protected RestArea(Builder builder, Area area) {
        super(builder, area);
        this.lewd = builder.isLewd();
        this.lewdProduct = builder.getLewdProduct();
        this.parties = new ArrayDeque<>();
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
        if (this.lewd) {
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
    protected EnumMap<CommandMessage, CommandHandler> buildCommands() {
        EnumMap<CommandMessage, CommandHandler> cmds = new EnumMap<>(CommandMessage.class);
        cmds.put(CommandMessage.REST, new RestHandler());
        cmds.put(CommandMessage.PASS, new PassHandler());
        cmds.put(CommandMessage.GO, new GoHandler());
        cmds.put(CommandMessage.STATS, new StatsHandler());
        cmds.put(CommandMessage.SEE, new SubAreaSeeHandler());
        cmds.put(CommandMessage.SAY, new SubAreaSayHandler());
        cmds.put(CommandMessage.EXIT, new SubAreaExitHandler());
        return cmds;
    }

    @Override
    public void onAreaEntry(ICreature creature) {
        // they need to voluntarily join the rest
    }

    @Override
    protected synchronized boolean basicAddCreature(ICreature creature) {
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
                        RoundThread thread = this.getRoundThread();
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
    protected synchronized boolean basicRemoveCreature(ICreature creature) {
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
        static final Predicate<CommandContext> defaultRestPredicate = RestingCommandHandler.defaultSubAreaPredicate
                .and(ctx -> ctx.getCreature().getSubAreaSorts().contains(SubAreaSort.RECUPERATION));
    }

    protected class RestHandler implements RestingCommandHandler {
        private final static String helpString = "\"REST\" puts yourself in state of REST, use \"GO UP\" to get out of it";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.REST;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(RestHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return RestHandler.defaultCreaturePredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd == null || !CommandMessage.REST.equals(cmd.getType())) {
                return ctx.failhandle();
            }
            RestArea.this.addCreature(ctx.getCreature());
            return ctx.handled();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return RestArea.this;
        }

    }

    /**
     * Reduces options to just "Go UP"
     */
    protected class GoHandler implements RestingCommandHandler {
        private static final String helpString = "Use the command <command>GO UP</command> to get out of bed. ";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.GO;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(GoHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return GoHandler.defaultRestPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.GO && cmd instanceof GoMessage goMessage) {
                if (Directions.UP.equals(goMessage.getDirection())) {
                    RestArea.this.removeCreature(ctx.getCreature());
                    return ctx.handled();
                } else {
                    ctx.receive(
                            BadGoEvent.getBuilder().setSubType(BadGoType.DNE).setAttempted(goMessage.getDirection())
                                    .setAvailable(EnumSet.of(Directions.UP)).Build());
                    return ctx.handled();
                }
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return RestArea.this;
        }

    }

    private class StatsHandler implements RestingCommandHandler {
        private final static String helpString = "\"stats\" Retrieves the statistics about the current battle.";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.STATS;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(StatsHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return StatsHandler.defaultRestPredicate;
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
        public CommandChainHandler getChainHandler() {
            return RestArea.this;
        }
    }

    protected interface PooledRestingCommandHandler extends PooledCommandHandler {
        static final Predicate<CommandContext> defaultPooledPredicate = RestingCommandHandler.defaultRestPredicate
                .and(ctx -> ctx.getSubAreaForSort(SubAreaSort.RECUPERATION).hasRunningThread("defaultPooledPredicate"));

        @Override
        default Predicate<CommandContext> getPoolingPredicate() {
            return PooledRestingCommandHandler.defaultPooledPredicate;
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

    private class LewdHandler implements PooledRestingCommandHandler {
        private final static String helpString = "\"lewd [creature]\" lewd another person in the bed";
        private final static Predicate<CommandContext> lewdPredicate = LewdHandler.defaultPooledPredicate
                .and(ctx -> ctx.getCreature().getEquipped(EquipmentSlots.ARMOR) == null);

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.LEWD;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return LewdHandler.lewdPredicate.and(ctx -> RestArea.this.lewd);
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
                } else if (!RestArea.this.parties.contains(party)) {
                    parties.addLast(party);
                } else {
                    // if it is contained in the list, say it's already proposed
                }
            }
            return ctx.handled();
        }

        @Override
        public Reply flushHandle(CommandContext ctx, Command cmd) {
            if (cmd != null && CommandMessage.LEWD.equals(cmd.getType())
                    && cmd instanceof LewdInMessage lewdInMessage) {

                Set<String> partners = lewdInMessage.getPartners();
                if (partners != null && partners.size() > 0) {
                    return this.handlePartnered(ctx, lewdInMessage, partners);
                }
                synchronized (RestArea.this.parties) {
                    final ArrayDeque<VrijPartij> parties = RestArea.this.parties;
                    final VrijPartij first = parties.peekFirst();
                    if (first != null) {
                        first.accept(ctx.getCreature());
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
        public CommandChainHandler getChainHandler() {
            return RestArea.this;
        }
    }

    private class PassHandler implements PooledRestingCommandHandler {
        private final Predicate<CommandContext> enabledPredicate = LewdHandler.lewdPredicate
                .and(ctx -> RestArea.this.parties.size() > 0);
        private final static String helpString = "\"pass\" to decline the current lewdness";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.LEWD;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(PassHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return this.enabledPredicate;
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return RestArea.this;
        }

        @Override
        public Reply flushHandle(CommandContext ctx, Command cmd) {
            if (cmd == null || !CommandMessage.PASS.equals(cmd.getType())) {
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
