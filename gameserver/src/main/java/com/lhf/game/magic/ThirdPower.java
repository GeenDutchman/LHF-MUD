package com.lhf.game.magic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.Taggable;
import com.lhf.game.EffectResistance;
import com.lhf.game.battle.BattleManager.PooledBattleManagerCommandHandler;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.VocationFactory;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.magic.CreatureAOESpellEntry.AutoTargeted;
import com.lhf.game.magic.Spellbook.Filters;
import com.lhf.game.map.Area;
import com.lhf.game.map.DMRoom;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.Room;
import com.lhf.game.map.RoomEffect;
import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.BadTargetSelectedEvent.BadTargetOption;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.SpellCastingEvent;
import com.lhf.messages.events.SpellEntryRequestedEvent;
import com.lhf.messages.events.SpellFizzledEvent;
import com.lhf.messages.events.SpellFizzledEvent.SpellFizzleType;
import com.lhf.messages.events.TargetDefendedEvent;
import com.lhf.messages.in.CastMessage;
import com.lhf.messages.in.SpellbookMessage;

public class ThirdPower implements CommandChainHandler {
    // buff debuff
    // damage heal
    // summon banish

    /*
     * Some spells target creatures
     * Some spells target items
     * Some spells target rooms
     * Some spells target the dungeon
     * 
     * 
     */
    private transient CommandChainHandler successor;
    private EnumMap<CommandMessage, CommandHandler> cmds;
    private Spellbook spellbook;
    private transient final Logger logger;
    public final GameEventProcessorID gameEventProcessorID;

    public ThirdPower(CommandChainHandler successor, Spellbook spellbook) {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.gameEventProcessorID = new GameEventProcessorID();
        this.successor = successor;
        this.cmds = this.generateCommands();
        if (spellbook == null) {
            this.spellbook = new Spellbook();
            this.spellbook.loadFromFile();
        } else {
            this.spellbook = spellbook;
        }
    }

    private EnumMap<CommandMessage, CommandHandler> generateCommands() {
        EnumMap<CommandMessage, CommandHandler> toGenerate = new EnumMap<>(CommandMessage.class);
        toGenerate.put(CommandMessage.CAST, new CastHandler());
        toGenerate.put(CommandMessage.SPELLBOOK, new SpellbookHandler());
        return toGenerate;
    }

    public interface ThirdPowerCommandHandler extends Dungeon.DungeonCommandHandler {
        final static Predicate<CommandContext> prerequisites = ctx -> {
            if (!(ctx.getDungeon() != null || ctx.getRoom() instanceof DMRoom)) {
                return false;
            }
            ICreature attempter = ctx.getCreature();
            if (attempter == null) {
                return false;
            }
            return attempter.getVocation() != null && attempter.getVocation() instanceof CubeHolder;
        };
        final static Predicate<CommandContext> defaultThirdPowerPredicate = ThirdPowerCommandHandler.defaultRoomPredicate
                .and(ThirdPowerCommandHandler.prerequisites);
    }

    protected class CastHandler implements PooledBattleManagerCommandHandler {
        private final static String helpString = new StringJoiner(" ")
                .add("\"cast [invocation]\"").add("Casts the spell that has the matching invocation.").add("\n")
                .add("\"cast [invocation] at [target]\"").add("Some spells need you to name a target.").add("\n")
                .add("\"cast [invocation] use [level]\"")
                .add("Sometimes you want to put more power into your spell, so put a higher level number for the level.")
                .add("\n").toString();
        private final static Logger logger = Logger.getLogger(CastHandler.class.getName());

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.CAST;
        }

        @Override
        public Predicate<CommandContext> getPoolingPredicate() {
            return CastHandler.defaultTurnPredicate.and(ThirdPowerCommandHandler.prerequisites);
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(CastHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return ThirdPowerCommandHandler.defaultThirdPowerPredicate;
        }

        private CommandContext.Reply affectCreatures(CommandContext ctx, ISpell<CreatureEffect> spell,
                Collection<ICreature> targets) {
            final ICreature caster = ctx.getCreature();
            final SubArea battleManager = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
            final CubeHolder vocation = caster.getVocation() instanceof CubeHolder ? (CubeHolder) caster.getVocation()
                    : null;

            for (ICreature target : targets) {
                if (spell.isOffensive() && battleManager != null) {
                    CreatureFaction.checkAndHandleTurnRenegade(caster, target, battleManager);
                    if (!battleManager.hasCreature(target)) {
                        battleManager.addCreature(target);
                    }
                }

                for (CreatureEffect effect : spell) {
                    EffectResistance resistance = effect.getResistance();
                    MultiRollResult casterResult = null;
                    MultiRollResult targetResult = null;
                    if (resistance != null) {
                        casterResult = resistance.actorEffort(caster,
                                vocation != null ? vocation.getCastingBonus(effect) : 0);
                        targetResult = resistance.targetEffort(target, 0);
                    }

                    if (resistance == null || targetResult == null
                            || (casterResult != null && (casterResult.getTotal() > targetResult.getTotal()))) {
                        GameEvent cam = target.applyEffect(effect);
                        ThirdPower.this.channelizeMessage(ctx, cam, spell.isOffensive(), caster, target);
                    } else {
                        TargetDefendedEvent missMessage = TargetDefendedEvent.getBuilder().setAttacker(caster)
                                .setTarget(target)
                                .setOffense(casterResult).setDefense(targetResult).Build();

                        ThirdPower.this.channelizeMessage(ctx, missMessage, spell.isOffensive());
                    }
                }
            }
            return ctx.handled();
        }

        private CommandContext.Reply handleCastCreatureTargeting(CommandContext ctx, CastMessage casting,
                CreatureTargetingSpellEntry entry) {
            if (casting == null || entry == null) {
                CastHandler.logger.log(Level.SEVERE, "Cannot target creatures with null message or entry");
                return ctx.failhandle();
            }
            ICreature caster = ctx.getCreature();
            Room localRoom = ctx.getRoom();
            if (caster == null || localRoom == null) {
                ctx.receive(SpellFizzledEvent.getBuilder().setAttempter(caster)
                        .setNotBroadcast().setSubType(SpellFizzleType.OTHER).setNotBroadcast().Build());
                return ctx.handled();
            }
            final CreatureTargetingSpell spell = new CreatureTargetingSpell(entry, caster);

            List<ICreature> possTargets = new ArrayList<>();
            for (String targetName : casting.getTargets()) {
                List<ICreature> found = new ArrayList<>(localRoom.getCreaturesLike(targetName));
                if (found.size() > 1 || found.size() == 0) {
                    CastHandler.logger.log(Level.WARNING,
                            () -> String.format("Searching for '%s' got '%s', cannot continue selecting targets",
                                    targetName, found));
                    ctx.receive(BadTargetSelectedEvent.getBuilder()
                            .setBde(found.size() > 1 ? BadTargetOption.UNCLEAR : BadTargetOption.NOTARGET)
                            .setBadTarget(targetName).setPossibleTargets(found).Build());
                    return ctx.handled();
                }
                CastHandler.logger.log(Level.FINER, () -> String.format("Target '%s' found and added",
                        targetName));
            }

            SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
            if (bm != null && !bm.hasRunningThread("handleCastCreatureTargeting()") && spell.isOffensive()) {
                CastHandler.logger.log(Level.INFO,
                        () -> String.format("Starting battle with offensive spell %s", spell));
                bm.instigate(caster, possTargets);
                this.onEmpool(ctx, bm.empool(ctx, casting));
                return ctx.handled();
            }

            CastHandler.logger.log(Level.FINE, "Casting creature targeting spell");
            ResourceCost level = casting.getLevel() != null
                    && entry.getLevel().compareTo(ResourceCost.fromInt(casting.getLevel())) <= 0
                            ? ResourceCost.fromInt(casting.getLevel())
                            : entry.getLevel();
            SpellCastingEvent castingMessage = entry.Cast(caster, level, possTargets);
            ThirdPower.this.channelizeMessage(ctx, castingMessage, spell.isOffensive(), caster);

            return this.affectCreatures(ctx, spell, possTargets);
        }

        private CommandContext.Reply handleCastCreatureAOETargeting(CommandContext ctx, CastMessage casting,
                CreatureAOESpellEntry entry) {
            if (casting == null || entry == null) {
                CastHandler.logger.log(Level.SEVERE, "Cannot target creatures AOE with null message or entry");
                return ctx.failhandle();
            }
            final ICreature caster = ctx.getCreature();
            final Room localRoom = ctx.getRoom();
            if (caster == null || localRoom == null) {
                ctx.receive(SpellFizzledEvent.getBuilder().setAttempter(caster)
                        .setNotBroadcast().setSubType(SpellFizzleType.OTHER).setNotBroadcast().Build());
                return ctx.handled();
            }

            int castLevel = casting.getLevel() != null ? casting.getLevel() : entry.getLevel().toInt();
            AutoTargeted upcasted = AutoTargeted.upCast(entry.getAutoSafe(),
                    castLevel - entry.getLevel().toInt(),
                    entry.isOffensive());
            CreatureAOESpell spell = new CreatureAOESpell(entry, caster, upcasted);

            Set<ICreature> targets = new HashSet<>();
            for (ICreature possTarget : localRoom.getCreatures()) {
                if (possTarget.equals(caster)) {
                    if (upcasted.isCasterTargeted()) {
                        targets.add(caster);
                    }
                    continue;
                }
                if (upcasted.areNPCsTargeted() && CreatureFaction.NPC.equals(possTarget.getFaction())) {
                    targets.add(possTarget);
                } else if (upcasted.areAlliesTargeted()
                        && !caster.getFaction().competing(possTarget.getFaction())) {
                    targets.add(possTarget);
                } else if (upcasted.areEnemiesTargeted() && caster.getFaction().competing(possTarget.getFaction())
                        && !CreatureFaction.RENEGADE.equals(possTarget.getFaction())) {
                    targets.add(possTarget);
                } else if (upcasted.areRenegadesTargeted()
                        && CreatureFaction.RENEGADE.equals(possTarget.getFaction())) {
                    targets.add(possTarget);
                }
            }

            SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
            if (bm != null && !bm.hasRunningThread("handleCastCreatureAOETargeting()") && spell.isOffensive()) {
                CastHandler.logger.log(Level.INFO,
                        () -> String.format("Starting battle with offensive AOE spell %s", spell));
                bm.instigate(caster, targets);
                this.onEmpool(ctx, bm.empool(ctx, casting));
                return ctx.handled();
            }

            CastHandler.logger.log(Level.FINE, "Casting AOE creature targeting spell");
            ResourceCost level = casting.getLevel() != null
                    && entry.getLevel().compareTo(ResourceCost.fromInt(casting.getLevel())) <= 0
                            ? ResourceCost.fromInt(casting.getLevel())
                            : entry.getLevel();
            SpellCastingEvent castingMessage = entry.Cast(caster, level, new ArrayList<>(targets));
            ThirdPower.this.channelizeMessage(ctx, castingMessage, spell.isOffensive(), caster);

            return this.affectCreatures(ctx, spell, targets);
        }

        private CommandContext.Reply affectRoom(CommandContext ctx, ISpell<? extends RoomEffect> spell) {
            final ICreature caster = ctx.getCreature();

            CastHandler.logger.log(Level.FINE, "Applying individual effects");
            for (RoomEffect effect : spell) {
                EffectResistance resistance = effect.getResistance();
                MultiRollResult casterResult = null;
                MultiRollResult targetResult = null;
                if (resistance != null) {
                    casterResult = resistance.actorEffort(caster, 0);
                    targetResult = resistance.targetEffort(0);
                }

                if (resistance == null || targetResult == null
                        || (casterResult != null && (casterResult.getTotal() > targetResult.getTotal()))) {
                    GameEvent ram = ctx.getRoom().applyEffect(effect);
                    ThirdPower.this.channelizeMessage(ctx, ram, spell.isOffensive(), caster);
                } else {
                    SpellFizzledEvent fizzleMessage = SpellFizzledEvent.getBuilder().setAttempter(caster)
                            .setSubType(SpellFizzleType.OTHER).setOffense(casterResult).setDefense(targetResult)
                            .Build();
                    ThirdPower.this.channelizeMessage(ctx, fizzleMessage, spell.isOffensive());
                }
            }
            return ctx.handled();
        }

        private CommandContext.Reply handleCastDMRoomTargeting(CommandContext ctx, CastMessage casting,
                DMRoomTargetingSpellEntry entry) {
            if (casting == null || entry == null) {
                CastHandler.logger.log(Level.SEVERE, "Cannot target DM room with null message or entry");
                return ctx.failhandle();
            }

            final ICreature caster = ctx.getCreature();
            final Room localRoom = ctx.getRoom();
            if (caster == null || localRoom == null || !(localRoom instanceof DMRoom)) {
                ctx.receive(SpellFizzledEvent.getBuilder().setAttempter(caster)
                        .setNotBroadcast().setSubType(SpellFizzleType.OTHER).setNotBroadcast().Build());
                return ctx.failhandle();
            }
            final DMRoom dmRoom = (DMRoom) localRoom;

            CastHandler.logger.log(Level.INFO,
                    () -> String.format("Caster '%s' is affecting a DMRoom with spell '%s'", caster.getName(),
                            entry.getName()));

            DMRoomTargetingSpell spell = new DMRoomTargetingSpell(entry, caster);

            List<Taggable> taggedTargets = new ArrayList<>();

            if (spell != null && spell.getTypedEntry().isEnsoulsUsers()) {
                String target = casting.getByPreposition("at");
                if (target == null) {
                    CastHandler.logger.log(Level.WARNING, "No target found!");
                    ctx.receive(BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.NOTARGET)
                            .setBadTarget(target).Build());
                    return ctx.handled();
                }
                final Taggable foundUser = dmRoom.getUser(target);
                if (foundUser == null) {
                    CastHandler.logger.log(Level.WARNING,
                            () -> String.format("User '%s' is not in the DMRoom", target));
                    ctx.receive(
                            SpellFizzledEvent.getBuilder().setSubType(SpellFizzleType.OTHER).setAttempter(caster)
                                    .setNotBroadcast().Build());
                    return ctx.handled();
                }
                taggedTargets.add(foundUser);
                CastHandler.logger.log(Level.FINE,
                        () -> String.format("Caster '%s' is targeting '%s' in the DMRoom", caster.getName(),
                                target));
                String vocationName = casting.getByPreposition("as");
                Vocation vocation = VocationFactory.getVocation(vocationName);
                if (vocation != null || vocationName != null) {
                    spell.addUsernameToEnsoul(target, vocation);
                } else {
                    CastHandler.logger.log(Level.WARNING,
                            () -> String.format("Cannot ensoul %s without vocation!", foundUser));
                    ctx.receive(
                            SpellFizzledEvent.getBuilder().setSubType(SpellFizzleType.OTHER).setAttempter(caster)
                                    .setNotBroadcast().Build());
                    return ctx.handled();
                }
            }

            // TODO: summons and banish

            CastHandler.logger.log(Level.FINE, "Casting DMRoom targeting spell");
            ResourceCost level = casting.getLevel() != null
                    && entry.getLevel().compareTo(ResourceCost.fromInt(casting.getLevel())) <= 0
                            ? ResourceCost.fromInt(casting.getLevel())
                            : entry.getLevel();
            SpellCastingEvent castingMessage = entry.Cast(caster, level, taggedTargets);
            ThirdPower.this.channelizeMessage(ctx, castingMessage, spell.isOffensive());

            return this.affectRoom(ctx, spell);
        }

        private CommandContext.Reply handleCastRoomTargeting(CommandContext ctx, CastMessage casting,
                RoomTargetingSpellEntry entry) {
            if (casting == null || entry == null) {
                CastHandler.logger.log(Level.SEVERE, "Cannot target Room with null message or entry");
                return ctx.handled();
            }
            final ICreature caster = ctx.getCreature();
            final Room localRoom = ctx.getRoom();
            if (caster == null || localRoom == null) {
                ctx.receive(SpellFizzledEvent.getBuilder().setAttempter(caster)
                        .setNotBroadcast().setSubType(SpellFizzleType.OTHER).setNotBroadcast().Build());
                return ctx.handled();
            }

            RoomTargetingSpell spell = new RoomTargetingSpell(entry, caster);

            // TODO: summons and banish

            CastHandler.logger.log(Level.FINE, "Casting Room targeting spell");
            ResourceCost level = casting.getLevel() != null
                    && entry.getLevel().compareTo(ResourceCost.fromInt(casting.getLevel())) <= 0
                            ? ResourceCost.fromInt(casting.getLevel())
                            : entry.getLevel();
            SpellCastingEvent castingMessage = entry.Cast(caster, level, null);
            ThirdPower.this.channelizeMessage(ctx, castingMessage, spell.isOffensive());

            return this.affectRoom(ctx, spell);
        }

        private SpellEntry getEntry(CommandContext ctx, CastMessage casting) {
            final ICreature caster = ctx.getCreature();
            final Vocation casterVocation = caster.getVocation();

            NavigableSet<SpellEntry> foundByInvocation = ThirdPower.this.spellbook.filter(
                    EnumSet.of(Spellbook.Filters.INVOCATION, Spellbook.Filters.VOCATION_NAME),
                    casterVocation != null ? casterVocation.getVocationName() : null, null, casting.getInvocation(),
                    null);
            if (foundByInvocation.isEmpty()) {
                CastHandler.logger.log(Level.INFO,
                        () -> String.format("Invocation by '%s' -> '%s' not found", caster.getName(),
                                casting.getInvocation()));

                return null;
            }
            SpellEntry entry = foundByInvocation.first();
            CastHandler.logger.log(Level.INFO,
                    () -> String.format("Invocation by '%s' found -> %s", caster.getName(), entry));
            return entry;
        }

        private CommandContext.Reply handleCast(CommandContext ctx, CastMessage casting) {
            ICreature caster = ctx.getCreature();

            SpellEntry entry = this.getEntry(ctx, casting);
            if (entry == null) {
                SpellFizzledEvent.Builder spellFizzleMessage = SpellFizzledEvent.getBuilder().setAttempter(caster)
                        .setNotBroadcast();
                ctx.receive(spellFizzleMessage.setSubType(SpellFizzleType.MISPRONOUNCE).setNotBroadcast().Build());
                if (ctx.getRoom() != null) {
                    ctx.getRoom().announce(spellFizzleMessage.setBroacast().Build());
                }
                return ctx.handled();
            }
            CastHandler.logger.log(Level.INFO,
                    () -> String.format("Invocation by '%s' -> '%s' found: '%s'", caster.getName(),
                            casting.getInvocation(), entry.getName()));

            if (entry instanceof CreatureTargetingSpellEntry creatureTargetingSpellEntry) {
                return this.handleCastCreatureTargeting(ctx, casting, creatureTargetingSpellEntry);
            } else if (entry instanceof CreatureAOESpellEntry creatureAOESpellEntry) {
                return this.handleCastCreatureAOETargeting(ctx, casting, creatureAOESpellEntry);
            } else if (entry instanceof DMRoomTargetingSpellEntry dmRoomSpellEntry) {
                return this.handleCastDMRoomTargeting(ctx, casting, dmRoomSpellEntry);
            } else if (entry instanceof RoomTargetingSpellEntry roomSpellEntry) {
                return this.handleCastRoomTargeting(ctx, casting, roomSpellEntry);
            } // other cases

            return ctx.failhandle();
        }

        @Override
        public Reply flushHandle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.CAST && cmd instanceof CastMessage castmessage) {
                if (ctx.getCreature() == null) {
                    ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                            .setHelps(ctx.getHelps()).setCommand(castmessage).Build());
                    return ctx.handled();
                }
                ICreature attempter = ctx.getCreature();
                if (attempter.getVocation() == null || !(attempter.getVocation() instanceof CubeHolder)) {
                    SpellFizzledEvent.Builder spellFizzle = SpellFizzledEvent.getBuilder()
                            .setSubType(SpellFizzleType.NOT_CASTER).setAttempter(attempter).setNotBroadcast();
                    ctx.receive(spellFizzle.Build());
                    if (ctx.getRoom() != null) {
                        ctx.getRoom().announce(spellFizzle.setBroacast().Build());
                    }
                    return ctx.handled();
                } else if (!attempter.isInBattle() && ctx.getSubAreaForSort(SubAreaSort.BATTLE) != null
                        && ctx.getSubAreaForSort(SubAreaSort.BATTLE)
                                .hasRunningThread("ThirdPower.CastHandler.flushHandle()")) {
                    SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
                    bm.addCreature(attempter);
                    return bm.handleChain(ctx, castmessage);
                } else {
                    return this.handleCast(ctx, castmessage);
                }
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return ThirdPower.this;
        }

    }

    protected class SpellbookHandler implements ThirdPowerCommandHandler {
        private final static String helpString = new StringJoiner(" ")
                .add("\"spellbook\"")
                .add("Lets you see what spells are available to you, taking into account how much power you still have.")
                .add("\n")
                .add("\"spellbook [spellname]\"")
                .add("Looks up a specific spell by name, as long as your vocation allows it.").add("\n").toString();

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.SPELLBOOK;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(SpellbookHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return SpellbookHandler.defaultThirdPowerPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            SpellbookMessage spellbookMessage = (SpellbookMessage) cmd;
            ICreature caster = ctx.getCreature();
            if (caster.getVocation() == null || !(caster.getVocation() instanceof CubeHolder)) {
                SpellEntryRequestedEvent.Builder notCaster = SpellEntryRequestedEvent.getBuilder().setNotCubeHolder()
                        .setNotBroadcast();
                ctx.receive(notCaster.Build());
                return ctx.handled();
            }
            EnumSet<Filters> filters = EnumSet.of(Filters.VOCATION_NAME, Filters.LEVELS);
            if (spellbookMessage.getSpellName() != null) {
                filters.add(Filters.SPELL_NAME);
            }
            for (String withFilters : spellbookMessage.getWithFilters()) {
                Filters found = Filters.getFilters(withFilters);
                if (found != null) {
                    filters.add(found);
                }
            }
            NavigableSet<SpellEntry> entries = ThirdPower.this.spellbook.filter(filters,
                    caster.getVocation().getVocationName(),
                    spellbookMessage.getSpellName(), null,
                    ((CubeHolder) caster.getVocation()).availableMagnitudes());
            ctx.receive(SpellEntryRequestedEvent.getBuilder().setEntries(entries).Build());
            return ctx.handled();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return ThirdPower.this;
        }

    }

    private void channelizeMessage(CommandContext ctx, GameEvent message, boolean includeBattle,
            GameEventProcessor... directs) {
        if (message == null) {
            return;
        }
        SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
        if (includeBattle && bm != null && bm.hasRunningThread("ThirdPower.channelizeMessage()")) {
            SubArea.eventAccepter.accept(bm, message);
        } else if (ctx.getRoom() != null) {
            Area.eventAccepter.accept(ctx.getRoom(), message);
        } else if (directs != null) {
            for (GameEventProcessor direct : directs) {
                GameEventProcessor.eventAccepter.accept(direct, message);
            }
        }
    }

    @Override
    public void setSuccessor(CommandChainHandler successor) {
        this.successor = successor;
    }

    @Override
    public CommandChainHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return ctx;
    }

    @Override
    public GameEventProcessorID getEventProcessorID() {
        return this.gameEventProcessorID;
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        return Collections.unmodifiableMap(this.cmds);
    }

    @Override
    public synchronized void log(Level logLevel, String logMessage) {
        this.logger.log(logLevel, logMessage);

    }

    @Override
    public synchronized void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.logger.log(logLevel, logMessageSupplier);
    }

    @Override
    public Collection<GameEventProcessor> getGameEventProcessors() {
        this.log(Level.FINE, "No client messengers here!");
        return Set.of();
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + "Third Power" + this.getEndTag();
    }

    @Override
    public String getEndTag() {
        return "</ThirdPower>";
    }

    @Override
    public String getStartTag() {
        return "<ThirdPower>";
    }

}
