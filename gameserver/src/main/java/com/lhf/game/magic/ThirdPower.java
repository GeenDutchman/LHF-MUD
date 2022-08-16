package com.lhf.game.magic;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.magic.concrete.ShockBolt;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.concrete.ThunderStrike;
import com.lhf.game.magic.strategies.CasterVsCreatureStrategy;
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
    private SortedSet<SpellEntry> entries;
    private MessageHandler successor;
    private HashMap<CommandMessage, String> cmds;

    public ThirdPower(MessageHandler successor) {
        this.successor = successor;
        this.entries = new TreeSet<>();
        SpellEntry shockBolt = new ShockBolt();
        this.entries.add(shockBolt);
        SpellEntry thaumaturgy = new Thaumaturgy();
        this.entries.add(thaumaturgy);
        SpellEntry thunderStrike = new ThunderStrike();
        this.entries.add(thunderStrike);
        this.cmds = this.generateCommands();
    }

    private HashMap<CommandMessage, String> generateCommands() {
        HashMap<CommandMessage, String> toGenerate = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("\"cast [invocation]\"").add("Casts the spell that has the matching invocation.").add("\n");
        sj.add("\"cast [invocation] at [target]\"").add("Some spells need you to name a target.").add("\n");
        sj.add("\"cast [invocation] use [level]\"").add(
                "Sometimes you want to put more power into your spell, so put a higher level number for the level.")
                .add("\n");
        toGenerate.put(CommandMessage.CAST, sj.toString()); // TODO: make this help not even show up for non-casters
        return toGenerate;
    }

    public SortedSet<SpellEntry> filterByExactLevel(int level) {
        Supplier<TreeSet<SpellEntry>> sortSupplier = () -> new TreeSet<SpellEntry>();
        return this.entries.stream().filter(entry -> entry.getLevel() == level)
                .collect(Collectors.toCollection(sortSupplier));
    }

    public SortedSet<SpellEntry> filterByVocationName(VocationName vocationName) {
        Supplier<TreeSet<SpellEntry>> sortSupplier = () -> new TreeSet<SpellEntry>();
        return this.entries.stream().filter(
                entry -> entry.getAllowedVocations().size() == 0 || entry.getAllowedVocations().contains(vocationName)
                        || VocationName.DUNGEON_MASTER.equals(vocationName))
                .collect(Collectors.toCollection(sortSupplier));
    }

    public SortedSet<SpellEntry> filterByVocationAndLevels(VocationName vocationName, Collection<Integer> levels) {
        Supplier<TreeSet<SpellEntry>> sortSupplier = () -> new TreeSet<SpellEntry>();
        return this.entries.stream().filter(
                entry -> entry.getAllowedVocations().size() == 0 || entry.getAllowedVocations().contains(vocationName)
                        || VocationName.DUNGEON_MASTER.equals(vocationName))
                .filter(entry -> levels != null && levels.contains(entry.getLevel()))
                .collect(Collectors.toCollection(sortSupplier));
    }

    public Optional<SpellEntry> filterByExactName(String name) {
        return this.entries.stream().filter(entry -> entry.getName().equals(name)).findFirst();
    }

    public Optional<SpellEntry> filterByExactInvocation(String invocation) {
        return this.entries.stream().filter(entry -> entry.getInvocation().equals(invocation)).findFirst();
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
            CreatureTargetingSpell spell = new CreatureTargetingSpell((CreatureTargetingSpellEntry) entry);
            spell.setCaster(caster);
            // TODO: duration should be a thing
            if (ctx.getRoom() == null) {
                ctx.sendMsg(new SpellFizzleMessage(SpellFizzleType.OTHER, caster, true));
                return true;
            }
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
            if (spell.isOffensive() && battleManager != null && !battleManager.isBattleOngoing()) {
                battleManager.startBattle(caster, possTargets);
            }
            this.channelizeMessage(ctx, castingMessage, spell.isOffensive(), caster);
            Optional<CasterVsCreatureStrategy> defense = Optional.empty();
            if (spell.isOffensive()) {
                defense = spell.getStrategy();
            }
            for (Creature target : possTargets) {
                if (spell.isOffensive() && battleManager != null) {
                    battleManager.checkAndHandleTurnRenegade(caster, target);
                    if (!battleManager.isCreatureInBattle(target)) {
                        battleManager.addCreatureToBattle(target);
                        battleManager.callReinforcements(caster, target);
                    }
                    if (defense.isPresent()) {
                        CasterVsCreatureStrategy strat = defense.get();
                        MultiRollResult casterResult = strat.getCasterEffort();
                        MultiRollResult targetResult = strat.getTargetEffort(target);
                        if (casterResult.getTotal() <= targetResult.getTotal()) {
                            battleManager.sendMessageToAllParticipants(
                                    new MissMessage(caster, target, casterResult, targetResult));
                            continue;
                        }
                    }
                }
                CreatureAffectedMessage cam = target.applyAffects(spell);
                this.channelizeMessage(ctx, cam, spell.isOffensive(), caster, target);
            }

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
    public Boolean handleMessage(CommandContext ctx, Command msg) {
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
        return this.cmds;
    }

}
