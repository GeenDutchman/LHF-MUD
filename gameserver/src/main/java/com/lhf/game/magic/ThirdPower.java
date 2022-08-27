package com.lhf.game.magic;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.lhf.game.EffectResistance;
import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.magic.CreatureAOESpellEntry.AutoTargeted;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.CastMessage;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.CastingMessage;
import com.lhf.messages.out.CreatureAffectedMessage;
import com.lhf.messages.out.MissMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SpellFizzleMessage;
import com.lhf.messages.out.SpellFizzleMessage.SpellFizzleType;

public class ThirdPower implements MessageHandler {
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
    private MessageHandler successor;
    private EnumMap<CommandMessage, String> cmds;
    private Spellbook spellbook;

    public ThirdPower(MessageHandler successor, Spellbook spellbook) {
        this.successor = successor;
        this.cmds = this.generateCommands();
        if (spellbook != null) {
            this.spellbook = new Spellbook();
            this.spellbook.loadFromFile();
        } else {
            this.spellbook = spellbook;
        }
    }

    private EnumMap<CommandMessage, String> generateCommands() {
        EnumMap<CommandMessage, String> toGenerate = new EnumMap<>(CommandMessage.class);
        StringJoiner sj = new StringJoiner(" ");
        sj.add("\"cast [invocation]\"").add("Casts the spell that has the matching invocation.").add("\n");
        sj.add("\"cast [invocation] at [target]\"").add("Some spells need you to name a target.").add("\n");
        sj.add("\"cast [invocation] use [level]\"").add(
                "Sometimes you want to put more power into your spell, so put a higher level number for the level.")
                .add("\n");
        toGenerate.put(CommandMessage.CAST, sj.toString());
        return toGenerate;
    }

    @Override
    public EnumMap<CommandMessage, String> gatherHelp(CommandContext ctx) {
        EnumMap<CommandMessage, String> retrieved = MessageHandler.super.gatherHelp(ctx);
        if (ctx.getCreature() == null || !(ctx.getCreature().getVocation() instanceof CubeHolder)) {
            retrieved.remove(CommandMessage.CAST);
        }
        return retrieved;
    }

    public SortedSet<SpellEntry> filterByExactLevel(int level) {
        Supplier<TreeSet<SpellEntry>> sortSupplier = () -> new TreeSet<SpellEntry>();
        return this.spellbook.getEntries().stream().filter(entry -> entry.getLevel() == level)
                .collect(Collectors.toCollection(sortSupplier));
    }

    public SortedSet<SpellEntry> filterByVocationName(VocationName vocationName) {
        Supplier<TreeSet<SpellEntry>> sortSupplier = () -> new TreeSet<SpellEntry>();
        return this.spellbook.getEntries().stream().filter(
                entry -> entry.getAllowedVocations().size() == 0 ||
                        (vocationName != null && entry.getAllowedVocations().contains(vocationName)) ||
                        VocationName.DUNGEON_MASTER.equals(vocationName))
                .collect(Collectors.toCollection(sortSupplier));
    }

    public SortedSet<SpellEntry> filterByVocationAndLevels(VocationName vocationName, Collection<Integer> levels) {
        Supplier<TreeSet<SpellEntry>> sortSupplier = () -> new TreeSet<SpellEntry>();
        return this.spellbook.getEntries().stream().filter(
                entry -> entry.getAllowedVocations().size() == 0 ||
                        (vocationName != null && entry.getAllowedVocations().contains(vocationName)) ||
                        VocationName.DUNGEON_MASTER.equals(vocationName))
                .filter(entry -> levels != null && levels.contains(entry.getLevel()))
                .collect(Collectors.toCollection(sortSupplier));
    }

    public Optional<SpellEntry> filterByExactName(String name) {
        return this.spellbook.getEntries().stream().filter(entry -> entry.getName().equals(name)).findFirst();
    }

    public Optional<SpellEntry> filterByExactInvocation(String invocation) {
        return this.spellbook.getEntries().stream().filter(entry -> entry.getInvocation().equals(invocation))
                .findFirst();
    }

    private boolean affectCreatures(CommandContext ctx, ISpell<CreatureEffect> spell, Collection<Creature> targets) {
        Creature caster = ctx.getCreature();
        BattleManager battleManager = ctx.getBattleManager();
        if (spell.isOffensive() && battleManager != null && !battleManager.isBattleOngoing()) {
            battleManager.startBattle(caster, targets);
        }

        for (Creature target : targets) {
            if (spell.isOffensive() && battleManager != null) {
                battleManager.checkAndHandleTurnRenegade(caster, target);
                if (!battleManager.isCreatureInBattle(target)) {
                    battleManager.addCreatureToBattle(target);
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
                    CreatureAffectedMessage cam = target.applyEffect(effect);
                    this.channelizeMessage(ctx, cam, spell.isOffensive(), caster, target);
                } else {
                    MissMessage missMessage = new MissMessage(caster, target, casterResult, targetResult);
                    this.channelizeMessage(ctx, missMessage, spell.isOffensive());
                }
            }
        }
        return true;
    }

    private boolean handleCast(CommandContext ctx, Command msg) {
        CastMessage casting = (CastMessage) msg;
        Creature caster = ctx.getCreature();
        Optional<SpellEntry> foundByInvocation = this.filterByExactInvocation(casting.getInvocation());
        if (foundByInvocation.isEmpty()) {
            ctx.sendMsg(new SpellFizzleMessage(SpellFizzleType.MISPRONOUNCE, caster, true));
            if (ctx.getRoom() != null) {
                ctx.getRoom().sendMessageToAll(new SpellFizzleMessage(SpellFizzleType.MISPRONOUNCE, caster, false));
            }
            return true;
        }
        SpellEntry entry = foundByInvocation.get();
        BattleManager battleManager = ctx.getBattleManager();
        if (battleManager != null && battleManager.isBattleOngoing()) {
            if (!battleManager.checkTurn(caster)) {
                return true; // even if not caster's turn, we handled it
            }
        }
        if (entry instanceof CreatureTargetingSpellEntry) {
            if (ctx.getRoom() == null) {
                ctx.sendMsg(new SpellFizzleMessage(SpellFizzleType.OTHER, caster, true));
                return true;
            }
            CreatureTargetingSpell spell = new CreatureTargetingSpell((CreatureTargetingSpellEntry) entry, caster);

            List<Creature> possTargets = new ArrayList<>();
            for (String targetName : casting.getTargets()) {
                List<Creature> found = ctx.getRoom().getCreaturesInRoom(targetName);
                if (found.size() > 1 || found.size() == 0) {
                    ctx.sendMsg(new BadTargetSelectedMessage(
                            found.size() > 1 ? BadTargetOption.UNCLEAR : BadTargetOption.NOTARGET, targetName, found));
                    return true;
                }
                possTargets.add(found.get(0));
            }

            CastingMessage castingMessage = entry.Cast(caster, casting.getLevel(), possTargets);
            this.channelizeMessage(ctx, castingMessage, spell.isOffensive(), caster);

            return this.affectCreatures(ctx, spell, possTargets);
        } else if (entry instanceof CreatureAOESpellEntry) {
            if (ctx.getRoom() == null) {
                ctx.sendMsg(new SpellFizzleMessage(SpellFizzleType.OTHER, caster, true));
                return true;
            }

            CreatureAOESpellEntry aoeEntry = (CreatureAOESpellEntry) entry;
            int castLevel = casting.getLevel() != null ? casting.getLevel() : entry.getLevel();
            AutoTargeted upcasted = AutoTargeted.upCast(aoeEntry.getAutoSafe(), castLevel - entry.getLevel(),
                    entry.isOffensive());
            CreatureAOESpell spell = new CreatureAOESpell(aoeEntry, caster, upcasted);

            Set<Creature> targets = new HashSet<>();
            for (Creature possTarget : ctx.getRoom().getCreaturesInRoom()) {
                if (possTarget.equals(caster)) {
                    if (upcasted.isCasterTargeted()) {
                        targets.add(caster);
                    }
                    continue;
                }
                if (upcasted.areNPCsTargeted() && CreatureFaction.NPC.equals(possTarget.getFaction())) {
                    targets.add(possTarget);
                } else if (upcasted.areAlliesTargeted() && !caster.getFaction().competing(possTarget.getFaction())) {
                    targets.add(possTarget);
                } else if (upcasted.areEnemiesTargeted() && caster.getFaction().competing(possTarget.getFaction())
                        && !CreatureFaction.RENEGADE.equals(possTarget.getFaction())) {
                    targets.add(possTarget);
                } else if (upcasted.areRenegadesTargeted()
                        && CreatureFaction.RENEGADE.equals(possTarget.getFaction())) {
                    targets.add(possTarget);
                }
            }

            CastingMessage castingMessage = entry.Cast(caster, casting.getLevel(), new ArrayList<>(targets));
            this.channelizeMessage(ctx, castingMessage, spell.isOffensive(), caster);

            return this.affectCreatures(ctx, spell, targets);
        } // TODO: other cases
        if (battleManager != null && battleManager.isCreatureInBattle(caster)) {
            battleManager.endTurn(caster);
        }
        return true;
    }

    private void channelizeMessage(CommandContext ctx, OutMessage message, boolean includeBattle,
            ClientMessenger... directs) {
        BattleManager bm = ctx.getBattleManager();
        if (includeBattle && bm != null && bm.isBattleOngoing()) {
            bm.sendMessageToAllParticipants(message);
        } else if (ctx.getRoom() != null) {
            ctx.getRoom().sendMessageToAll(message);
        } else if (directs != null) {
            for (ClientMessenger direct : directs) {
                direct.sendMsg(message);
            }
        }
    }

    @Override
    public void setSuccessor(MessageHandler successor) {
        this.successor = successor;
    }

    @Override
    public MessageHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return ctx;
    }

    @Override
    public Boolean handleMessage(CommandContext ctx, Command msg) {
        ctx = this.addSelfToContext(ctx);
        if (msg.getType() == CommandMessage.CAST) {
            Creature attempter = ctx.getCreature();
            if (attempter.getVocation() == null || !(attempter.getVocation() instanceof CubeHolder)) {
                ctx.sendMsg(new SpellFizzleMessage(SpellFizzleType.NOT_CASTER, attempter, true));
                if (ctx.getRoom() != null) {
                    ctx.getRoom().sendMessageToAllExcept(
                            new SpellFizzleMessage(SpellFizzleType.NOT_CASTER, attempter, false),
                            attempter.getName());
                }
            } else {
                this.handleCast(ctx, msg);
            }
            return true;
        }
        return MessageHandler.super.handleMessage(ctx, msg);
    }

    @Override
    public Map<CommandMessage, String> getCommands() {
        return Collections.unmodifiableMap(this.cmds);
    }

}
