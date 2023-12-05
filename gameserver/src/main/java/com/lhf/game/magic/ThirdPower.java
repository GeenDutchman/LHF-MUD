package com.lhf.game.magic;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.Taggable;
import com.lhf.game.EffectResistance;
import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.VocationFactory;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.magic.CreatureAOESpellEntry.AutoTargeted;
import com.lhf.game.magic.Spellbook.Filters;
import com.lhf.game.map.DMRoom;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.RoomEffect;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.in.CastMessage;
import com.lhf.messages.in.SpellbookMessage;
import com.lhf.messages.out.BadMessage;
import com.lhf.messages.out.BadMessage.BadMessageType;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.CastingMessage;
import com.lhf.messages.out.MissMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SpellEntryMessage;
import com.lhf.messages.out.SpellFizzleMessage;
import com.lhf.messages.out.SpellFizzleMessage.SpellFizzleType;

public class ThirdPower implements MessageChainHandler {
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
    private transient MessageChainHandler successor;
    private EnumMap<CommandMessage, CommandHandler> cmds;
    private Spellbook spellbook;
    private transient final Logger logger;

    public ThirdPower(MessageChainHandler successor, Spellbook spellbook) {
        this.logger = Logger.getLogger(this.getClass().getName());
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
        final static Predicate<CommandContext> defaultThirdPowerPredicate = ThirdPowerCommandHandler.defaultDungeonPredicate
                .and(ctx -> {
                    Creature attempter = ctx.getCreature();
                    if (attempter.getVocation() == null || !(attempter.getVocation() instanceof CubeHolder)) {
                        return false;
                    }
                    return true;
                });
    }

    protected class CastHandler implements ThirdPowerCommandHandler {
        private final static String helpString = new StringJoiner(" ")
                .add("\"cast [invocation]\"").add("Casts the spell that has the matching invocation.").add("\n")
                .add("\"cast [invocation] at [target]\"").add("Some spells need you to name a target.").add("\n")
                .add("\"cast [invocation] use [level]\"").add(
                        "Sometimes you want to put more power into your spell, so put a higher level number for the level.")
                .add("\n").toString();
        private final static Logger logger = Logger.getLogger(CastHandler.class.getName());

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.CAST;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(CastHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return ThirdPowerCommandHandler.defaultDungeonPredicate;
        }

        private boolean affectCreatures(CommandContext ctx, ISpell<CreatureEffect> spell,
                Collection<Creature> targets) {
            Creature caster = ctx.getCreature();
            BattleManager battleManager = ctx.getBattleManager();
            if (spell.isOffensive() && battleManager != null && !battleManager.isBattleOngoing()) {
                battleManager.startBattle(caster, targets);
            }

            for (Creature target : targets) {
                if (spell.isOffensive() && battleManager != null) {
                    battleManager.checkAndHandleTurnRenegade(caster, target);
                    if (!battleManager.hasCreature(target)) {
                        battleManager.addCreature(target);
                        battleManager.callReinforcements(caster, target);
                    }
                }

                for (CreatureEffect effect : spell) {
                    EffectResistance resistance = effect.getResistance();
                    MultiRollResult casterResult = null;
                    MultiRollResult targetResult = null;
                    if (resistance != null) {
                        casterResult = resistance.actorEffort(caster, 0);
                        targetResult = resistance.targetEffort(target, 0);
                    }

                    if (resistance == null || targetResult == null
                            || (casterResult != null && (casterResult.getTotal() > targetResult.getTotal()))) {
                        OutMessage cam = target.applyEffect(effect);
                        ThirdPower.this.channelizeMessage(ctx, cam, spell.isOffensive(), caster, target);
                    } else {
                        MissMessage missMessage = MissMessage.getBuilder().setAttacker(caster).setTarget(target)
                                .setOffense(casterResult).setDefense(targetResult).Build();

                        ThirdPower.this.channelizeMessage(ctx, missMessage, spell.isOffensive());
                    }
                }
            }
            return true;
        }

        private boolean affectRoom(CommandContext ctx, ISpell<? extends RoomEffect> spell) {
            Creature caster = ctx.getCreature();
            BattleManager battleManager = ctx.getBattleManager();

            if (spell.isOffensive() && battleManager != null && !battleManager.isBattleOngoing()) {
                CastHandler.logger.log(Level.INFO, "This spell was offensive and started a battle");
                battleManager.startBattle(caster, null);
            }

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
                    OutMessage ram = ctx.getRoom().applyEffect(effect);
                    ThirdPower.this.channelizeMessage(ctx, ram, spell.isOffensive(), caster);
                } else {
                    MissMessage missMessage = MissMessage.getBuilder().setAttacker(caster)
                            .setOffense(casterResult).setDefense(targetResult).Build();
                    ThirdPower.this.channelizeMessage(ctx, missMessage, spell.isOffensive());
                }
            }
            return true;
        }

        private boolean handleCast(CommandContext ctx, Command msg) {
            CastMessage casting = (CastMessage) msg;
            Creature caster = ctx.getCreature();
            SpellFizzleMessage.Builder spellFizzleMessage = SpellFizzleMessage.getBuilder().setAttempter(caster)
                    .setNotBroadcast();

            Vocation casterVocation = caster.getVocation();
            CastHandler.logger.log(Level.INFO,
                    () -> String.format("Handling cast of '%s' by '%s' who is a '%s'", casting.getInvocation(),
                            caster.getName(), casterVocation));
            NavigableSet<SpellEntry> foundByInvocation = ThirdPower.this.spellbook.filter(
                    EnumSet.of(Spellbook.Filters.INVOCATION, Spellbook.Filters.VOCATION_NAME),
                    casterVocation != null ? casterVocation.getVocationName() : null, null, casting.getInvocation(),
                    null);
            if (foundByInvocation.isEmpty()) {
                CastHandler.logger.log(Level.INFO,
                        () -> String.format("Invocation by '%s' -> '%s' not found", caster.getName(),
                                casting.getInvocation()));
                ctx.sendMsg(spellFizzleMessage.setSubType(SpellFizzleType.MISPRONOUNCE).setNotBroadcast().Build());
                if (ctx.getRoom() != null) {
                    ctx.getRoom().announce(spellFizzleMessage.setBroacast().Build());
                }
                return true;
            }
            SpellEntry entry = foundByInvocation.first();
            CastHandler.logger.log(Level.INFO,
                    () -> String.format("Invocation by '%s' -> '%s' found: '%s'", caster.getName(),
                            casting.getInvocation(), entry.getName()));
            BattleManager battleManager = ctx.getBattleManager();
            if (battleManager != null && battleManager.isBattleOngoing()) {
                if (!battleManager.checkTurn(caster)) {
                    return true; // even if not caster's turn, we handled it
                }
            }
            if (entry instanceof CreatureTargetingSpellEntry) {
                if (ctx.getRoom() == null) {
                    ctx.sendMsg(spellFizzleMessage.setSubType(SpellFizzleType.OTHER).setNotBroadcast().Build());
                    return true;
                }
                CreatureTargetingSpell spell = new CreatureTargetingSpell((CreatureTargetingSpellEntry) entry, caster);

                List<Creature> possTargets = new ArrayList<>();
                for (String targetName : casting.getTargets()) {
                    List<Creature> found = new ArrayList<>(ctx.getRoom().getCreaturesLike(targetName));
                    if (found.size() > 1 || found.size() == 0) {
                        CastHandler.logger.log(Level.FINE,
                                () -> String.format("Searching for '%s' got '%s'", targetName, found));
                        ctx.sendMsg(BadTargetSelectedMessage.getBuilder()
                                .setBde(found.size() > 1 ? BadTargetOption.UNCLEAR : BadTargetOption.NOTARGET)
                                .setBadTarget(targetName).setPossibleTargets(found).Build());
                        return true;
                    }
                    CastHandler.logger.log(Level.FINE, () -> String.format("Target '%s' found and added",
                            targetName));
                }

                CastHandler.logger.log(Level.FINE, "Casting creature targeting spell");
                ResourceCost level = casting.getLevel() != null
                        && entry.getLevel().compareTo(ResourceCost.fromInt(casting.getLevel())) <= 0
                                ? ResourceCost.fromInt(casting.getLevel())
                                : entry.getLevel();
                CastingMessage castingMessage = entry.Cast(caster, level, possTargets);
                ThirdPower.this.channelizeMessage(ctx, castingMessage, spell.isOffensive(), caster);

                return this.affectCreatures(ctx, spell, possTargets);
            } else if (entry instanceof CreatureAOESpellEntry) {
                if (ctx.getRoom() == null) {
                    ctx.sendMsg(spellFizzleMessage.setSubType(SpellFizzleType.OTHER).setNotBroadcast().Build());
                    return true;
                }

                CreatureAOESpellEntry aoeEntry = (CreatureAOESpellEntry) entry;
                int castLevel = casting.getLevel() != null ? casting.getLevel() : entry.getLevel().toInt();
                AutoTargeted upcasted = AutoTargeted.upCast(aoeEntry.getAutoSafe(),
                        castLevel - entry.getLevel().toInt(),
                        entry.isOffensive());
                CreatureAOESpell spell = new CreatureAOESpell(aoeEntry, caster, upcasted);

                Set<Creature> targets = new HashSet<>();
                for (Creature possTarget : ctx.getRoom().getCreatures()) {
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

                CastHandler.logger.log(Level.FINE, "Casting AOE creature targeting spell");
                ResourceCost level = casting.getLevel() != null
                        && entry.getLevel().compareTo(ResourceCost.fromInt(casting.getLevel())) <= 0
                                ? ResourceCost.fromInt(casting.getLevel())
                                : entry.getLevel();
                CastingMessage castingMessage = entry.Cast(caster, level, new ArrayList<>(targets));
                ThirdPower.this.channelizeMessage(ctx, castingMessage, spell.isOffensive(), caster);

                return this.affectCreatures(ctx, spell, targets);
            } else if (entry instanceof DMRoomTargetingSpellEntry) {
                if (ctx.getRoom() == null || !(ctx.getRoom() instanceof DMRoom)) {
                    ctx.sendMsg(SpellFizzleMessage.getBuilder().setSubType(SpellFizzleType.OTHER).setAttempter(caster)
                            .setNotBroadcast().Build());
                    return true;
                }
                CastHandler.logger.log(Level.INFO,
                        () -> String.format("Caster '%s' is affecting a DMRoom with spell '%s'", caster.getName(),
                                entry.getName()));

                DMRoom dmRoom = (DMRoom) ctx.getRoom();

                DMRoomTargetingSpell spell = new DMRoomTargetingSpell((DMRoomTargetingSpellEntry) entry, caster);

                List<Taggable> taggedTargets = new ArrayList<>();

                if (spell != null && spell.getTypedEntry().isEnsoulsUsers()) {
                    String target = casting.getByPreposition("at");
                    if (target == null) {
                        CastHandler.logger.log(Level.FINE, "No target found!");
                        ctx.sendMsg(BadTargetSelectedMessage.getBuilder().setBde(BadTargetOption.NOTARGET)
                                .setBadTarget(target).Build());
                        return true;
                    }
                    Taggable foundUser = dmRoom.getUser(target);
                    if (foundUser == null) {
                        CastHandler.logger.log(Level.FINE,
                                () -> String.format("User '%s' is not in the DMRoom", target));
                        ctx.sendMsg(
                                SpellFizzleMessage.getBuilder().setSubType(SpellFizzleType.OTHER).setAttempter(caster)
                                        .setNotBroadcast().Build());
                        return true;
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
                        ctx.sendMsg(
                                SpellFizzleMessage.getBuilder().setSubType(SpellFizzleType.OTHER).setAttempter(caster)
                                        .setNotBroadcast().Build());
                        return true;
                    }
                }

                // TODO: summons and banish

                CastHandler.logger.log(Level.FINE, "Casting DMRoom targeting spell");
                ResourceCost level = casting.getLevel() != null
                        && entry.getLevel().compareTo(ResourceCost.fromInt(casting.getLevel())) <= 0
                                ? ResourceCost.fromInt(casting.getLevel())
                                : entry.getLevel();
                CastingMessage castingMessage = entry.Cast(caster, level, taggedTargets);
                ThirdPower.this.channelizeMessage(ctx, castingMessage, spell.isOffensive());

                return this.affectRoom(ctx, spell);
            } else if (entry instanceof RoomTargetingSpellEntry) {
                if (ctx.getRoom() == null) {
                    ctx.sendMsg(SpellFizzleMessage.getBuilder().setSubType(SpellFizzleType.OTHER).setAttempter(caster)
                            .setNotBroadcast().Build());
                    return true;
                }

                RoomTargetingSpell spell = new RoomTargetingSpell((RoomTargetingSpellEntry) entry, caster);

                // TODO: summons and banish

                CastHandler.logger.log(Level.FINE, "Casting Room targeting spell");
                ResourceCost level = casting.getLevel() != null
                        && entry.getLevel().compareTo(ResourceCost.fromInt(casting.getLevel())) <= 0
                                ? ResourceCost.fromInt(casting.getLevel())
                                : entry.getLevel();
                CastingMessage castingMessage = entry.Cast(caster, level, null);
                ThirdPower.this.channelizeMessage(ctx, castingMessage, spell.isOffensive());

                return this.affectRoom(ctx, spell);
            } // TODO: other cases
            if (battleManager != null && battleManager.hasCreature(caster)) {
                battleManager.endTurn(caster);
            }
            return true;
        }

        @Override
        public Reply handle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.CAST) {
                if (ctx.getCreature() == null) {
                    ctx.sendMsg(BadMessage.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                            .setHelps(ctx.getHelps()).setCommand(cmd).Build());
                    return ctx.handled();
                }
                Creature attempter = ctx.getCreature();
                if (attempter.getVocation() == null || !(attempter.getVocation() instanceof CubeHolder)) {
                    SpellFizzleMessage.Builder spellFizzle = SpellFizzleMessage.getBuilder()
                            .setSubType(SpellFizzleType.NOT_CASTER).setAttempter(attempter).setNotBroadcast();
                    ctx.sendMsg(spellFizzle.Build());
                    if (ctx.getRoom() != null) {
                        ctx.getRoom().announce(spellFizzle.setBroacast().Build());
                    }
                } else {
                    this.handleCast(ctx, cmd);
                }
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public MessageChainHandler getChainHandler() {
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
        public Reply handle(CommandContext ctx, Command cmd) {
            SpellbookMessage spellbookMessage = (SpellbookMessage) cmd;
            Creature caster = ctx.getCreature();
            if (caster.getVocation() == null || !(caster.getVocation() instanceof CubeHolder)) {
                SpellEntryMessage.Builder notCaster = SpellEntryMessage.getBuilder().setNotCubeHolder()
                        .setNotBroadcast();
                ctx.sendMsg(notCaster.Build());
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
            ctx.sendMsg(SpellEntryMessage.getBuilder().setEntries(entries).Build());
            return ctx.handled();
        }

        @Override
        public MessageChainHandler getChainHandler() {
            return ThirdPower.this;
        }

    }

    private void channelizeMessage(CommandContext ctx, OutMessage message, boolean includeBattle,
            ClientMessenger... directs) {
        if (message == null) {
            return;
        }
        BattleManager bm = ctx.getBattleManager();
        if (includeBattle && bm != null && bm.isBattleOngoing()) {
            bm.announce(message);
        } else if (ctx.getRoom() != null) {
            ctx.getRoom().announce(message);
        } else if (directs != null) {
            for (ClientMessenger direct : directs) {
                direct.sendMsg(message);
            }
        }
    }

    @Override
    public void setSuccessor(MessageChainHandler successor) {
        this.successor = successor;
    }

    @Override
    public MessageChainHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return ctx;
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

}
