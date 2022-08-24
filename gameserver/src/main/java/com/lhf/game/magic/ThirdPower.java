package com.lhf.game.magic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.magic.CreatureAOESpellEntry.AutoSafe;
import com.lhf.game.magic.concrete.ShockBolt;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.concrete.ThunderStrike;
import com.lhf.game.magic.strategies.CasterVsCreatureStrategy;
import com.lhf.game.map.DungeonEffectSource;
import com.lhf.game.map.RoomEffectSource;
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
    private String path;
    private SortedSet<SpellEntry> entries;
    private MessageHandler successor;
    private HashMap<CommandMessage, String> cmds;
    private final String[] path_to_spellbook = { ".", "concrete" };

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
        this.setupPath();
    }

    private void setupPath() {
        StringBuilder makePath = new StringBuilder();
        for (String part : path_to_spellbook) {
            makePath.append(part).append(File.separator);
        }
        this.path = getClass().getResource(makePath.toString()).getPath().replaceAll("target(.)classes",
                "src$1main$1resources");
    }

    private Gson getAdaptedGson() {
        RuntimeTypeAdapterFactory<SpellEntry> spellEntryAdapter = RuntimeTypeAdapterFactory
                .of(SpellEntry.class, "className")
                .registerSubtype(CreatureTargetingSpellEntry.class, CreatureTargetingSpellEntry.class.getName())
                .registerSubtype(CreatureAOESpellEntry.class, CreatureAOESpellEntry.class.getName())
                .registerSubtype(RoomTargetingSpellEntry.class, RoomTargetingSpellEntry.class.getName())
                .registerSubtype(DungeonTargetingSpellEntry.class, DungeonTargetingSpellEntry.class.getName())
                .registerSubtype(ShockBolt.class, ShockBolt.class.getName())
                .registerSubtype(ThunderStrike.class, ThunderStrike.class.getName())
                .registerSubtype(Thaumaturgy.class, Thaumaturgy.class.getName());
        RuntimeTypeAdapterFactory<EntityEffectSource> effectSourceAdapter = RuntimeTypeAdapterFactory
                .of(EntityEffectSource.class, "className")
                .registerSubtype(CreatureEffectSource.class, CreatureEffectSource.class.getName())
                .registerSubtype(RoomEffectSource.class, RoomEffectSource.class.getName())
                .registerSubtype(DungeonEffectSource.class, DungeonEffectSource.class.getName());
        GsonBuilder gb = new GsonBuilder().registerTypeAdapterFactory(spellEntryAdapter)
                .registerTypeAdapterFactory(effectSourceAdapter).setPrettyPrinting();
        return gb.create();
    }

    public boolean saveToFile() throws IOException {
        return this.saveToFile(true);
    }

    @Deprecated(forRemoval = false)
    private boolean saveToFile(boolean loadFirst) throws IOException {
        if (loadFirst && !this.loadFromFile()) {
            throw new IOException("Cannot preload spellbook!");
        }
        Gson gson = this.getAdaptedGson();
        System.out.println("Writing to " + this.path);
        try (FileWriter fileWriter = new FileWriter(this.path + "spellbook.json")) {
            String asJson = gson.toJson(this.entries);
            System.out.println(asJson);
            fileWriter.write(asJson);
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean loadFromFile() {
        Gson gson = this.getAdaptedGson();
        System.out.println("Reading from " + this.path + "spellbook.json");
        Integer preSize = this.entries.size();
        try (JsonReader jReader = new JsonReader(new FileReader(this.path + "spellbook.json"))) {
            Type collectionType = new TypeToken<TreeSet<SpellEntry>>() {
            }.getType();
            SortedSet<SpellEntry> retrieved = gson.fromJson(jReader, collectionType);
            System.out.println(retrieved);
            this.entries.addAll(retrieved);
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.printf("Spellbook size changed by %d\n", this.entries.size() - preSize);
        return true;
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
                entry -> entry.getAllowedVocations().size() == 0 ||
                        (vocationName != null && entry.getAllowedVocations().contains(vocationName)) ||
                        VocationName.DUNGEON_MASTER.equals(vocationName))
                .collect(Collectors.toCollection(sortSupplier));
    }

    public SortedSet<SpellEntry> filterByVocationAndLevels(VocationName vocationName, Collection<Integer> levels) {
        Supplier<TreeSet<SpellEntry>> sortSupplier = () -> new TreeSet<SpellEntry>();
        return this.entries.stream().filter(
                entry -> entry.getAllowedVocations().size() == 0 ||
                        (vocationName != null && entry.getAllowedVocations().contains(vocationName)) ||
                        VocationName.DUNGEON_MASTER.equals(vocationName))
                .filter(entry -> levels != null && levels.contains(entry.getLevel()))
                .collect(Collectors.toCollection(sortSupplier));
    }

    public Optional<SpellEntry> filterByExactName(String name) {
        return this.entries.stream().filter(entry -> entry.getName().equals(name)).findFirst();
    }

    public Optional<SpellEntry> filterByExactInvocation(String invocation) {
        return this.entries.stream().filter(entry -> entry.getInvocation().equals(invocation)).findFirst();
    }

    private boolean affectCreatures(CommandContext ctx, ISpell<CreatureEffect> spell, CasterVsCreatureStrategy defense,
            Collection<Creature> targets) {
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
                if (defense != null) {
                    MultiRollResult casterResult = defense.getCasterEffort();
                    MultiRollResult targetResult = defense.getTargetEffort(target);
                    if (casterResult.getTotal() <= targetResult.getTotal()) {
                        battleManager.sendMessageToAllParticipants(
                                new MissMessage(caster, target, casterResult, targetResult));
                        continue;
                    }
                }
            }
            for (CreatureEffect effect : spell) {
                CreatureAffectedMessage cam = target.applyEffect(effect);
                this.channelizeMessage(ctx, cam, spell.isOffensive(), caster, target);
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
            CasterVsCreatureStrategy defense = null;
            if (spell.isOffensive()) {
                defense = spell.getStrategy();
            }
            CastingMessage castingMessage = entry.Cast(caster, casting.getLevel(), possTargets);
            this.channelizeMessage(ctx, castingMessage, spell.isOffensive(), caster);

            return this.affectCreatures(ctx, spell, defense, possTargets);
        } else if (entry instanceof CreatureAOESpellEntry) {
            if (ctx.getRoom() == null) {
                ctx.sendMsg(new SpellFizzleMessage(SpellFizzleType.OTHER, caster, true));
                return true;
            }

            CreatureAOESpellEntry aoeEntry = (CreatureAOESpellEntry) entry;
            int castLevel = casting.getLevel() != null ? casting.getLevel() : entry.getLevel();
            AutoSafe upcasted = AutoSafe.upCast(aoeEntry.getAutoSafe(), castLevel - entry.getLevel());
            CreatureAOESpell spell = new CreatureAOESpell(aoeEntry, caster, upcasted);

            Set<Creature> targets = new HashSet<>();
            for (Creature possTarget : ctx.getRoom().getCreaturesInRoom()) {
                if (possTarget.equals(caster)) {
                    if (!upcasted.isCasterSafe()) {
                        targets.add(caster);
                    }
                    continue;
                }
                if (!upcasted.areNpcSafe() && CreatureFaction.NPC.equals(possTarget.getFaction())) {
                    targets.add(possTarget);
                } else if (!upcasted.areAlliesSafe() && !caster.getFaction().competing(possTarget.getFaction())) {
                    targets.add(possTarget);
                } else if (!upcasted.areEnemiesSafe() && caster.getFaction().competing(possTarget.getFaction())
                        && !CreatureFaction.RENEGADE.equals(possTarget.getFaction())) {
                    targets.add(possTarget);
                } else if (!upcasted.areRenegadesSafe() && CreatureFaction.RENEGADE.equals(possTarget.getFaction())) {
                    targets.add(possTarget);
                }
            }
            CasterVsCreatureStrategy defense = null;
            if (spell.isOffensive()) {
                defense = spell.getStrategy();
            }
            CastingMessage castingMessage = entry.Cast(caster, casting.getLevel(), new ArrayList<>(targets));
            this.channelizeMessage(ctx, castingMessage, spell.isOffensive(), caster);

            return this.affectCreatures(ctx, spell, defense, targets);
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
