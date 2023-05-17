package com.lhf.game.magic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.EnumSet;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.magic.concrete.Ensouling;
import com.lhf.game.magic.concrete.ShockBolt;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.concrete.ThunderStrike;
import com.lhf.game.map.DMRoomEffectSource;
import com.lhf.game.map.DungeonEffectSource;
import com.lhf.game.map.RoomEffectSource;

public class Spellbook {
    private NavigableSet<SpellEntry> entries;
    private String path;
    private final String[] path_to_spellbook = { ".", "concrete" };
    private Logger logger;

    public enum Filters {
        VOCATION_NAME, SPELL_NAME, INVOCATION, LEVELS, OFFENSE, NONOFFENSE, SCORE;

        public static Filters getFilters(String value) {
            value = value.trim().replace(" ", "_");
            for (Filters vfilter : values()) {
                if (vfilter.toString().equalsIgnoreCase(value)) {
                    return vfilter;
                }
            }
            return null;
        }
    }

    public Spellbook() {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.logger.log(Level.CONFIG, "Loading initial small spellset");
        this.entries = new TreeSet<>();
        SpellEntry shockBolt = new ShockBolt();
        this.entries.add(shockBolt);
        SpellEntry thaumaturgy = new Thaumaturgy();
        this.entries.add(thaumaturgy);
        SpellEntry thunderStrike = new ThunderStrike();
        this.entries.add(thunderStrike);
        SpellEntry ensouling = new Ensouling();
        this.entries.add(ensouling);
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
                .of(SpellEntry.class, "className", true)
                .registerSubtype(CreatureTargetingSpellEntry.class, CreatureTargetingSpellEntry.class.getName())
                .registerSubtype(CreatureAOESpellEntry.class, CreatureAOESpellEntry.class.getName())
                .registerSubtype(RoomTargetingSpellEntry.class, RoomTargetingSpellEntry.class.getName())
                .registerSubtype(DMRoomTargetingSpellEntry.class, DMRoomTargetingSpellEntry.class.getName())
                .registerSubtype(DungeonTargetingSpellEntry.class, DungeonTargetingSpellEntry.class.getName())
                .registerSubtype(ShockBolt.class, ShockBolt.class.getName())
                .registerSubtype(ThunderStrike.class, ThunderStrike.class.getName())
                .registerSubtype(Thaumaturgy.class, Thaumaturgy.class.getName())
                .registerSubtype(Ensouling.class, Ensouling.class.getName())
                .recognizeSubtypes();
        RuntimeTypeAdapterFactory<EntityEffectSource> effectSourceAdapter = RuntimeTypeAdapterFactory
                .of(EntityEffectSource.class, "className", true)
                .registerSubtype(CreatureEffectSource.class, CreatureEffectSource.class.getName())
                .registerSubtype(RoomEffectSource.class, RoomEffectSource.class.getName())
                .registerSubtype(DMRoomEffectSource.class, DMRoomEffectSource.class.getName())
                .registerSubtype(DungeonEffectSource.class, DungeonEffectSource.class.getName())
                .recognizeSubtypes();
        GsonBuilder gb = new GsonBuilder().registerTypeAdapterFactory(spellEntryAdapter)
                .registerTypeAdapterFactory(effectSourceAdapter).setPrettyPrinting();
        return gb.create();
    }

    public boolean saveToFile() throws IOException {
        return this.saveToFile(true);
    }

    @Deprecated(forRemoval = false)
    private boolean saveToFile(boolean loadFirst) throws IOException {
        this.logger.entering(this.getClass().getName(), "saveToFile()", path_to_spellbook);
        if (loadFirst && !this.loadFromFile()) {
            throw new IOException("Cannot preload spellbook!");
        }
        Gson gson = this.getAdaptedGson();
        this.logger.log(Level.INFO, "Writing to " + this.path);
        try (FileWriter fileWriter = new FileWriter(this.path + "spellbook.json")) {
            String asJson = gson.toJson(this.entries);
            this.logger.log(Level.FINER, asJson);
            fileWriter.write(asJson);
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean loadFromFile() {
        Gson gson = this.getAdaptedGson();
        this.logger.log(Level.CONFIG, "Reading from " + this.path + "spellbook.json");
        Integer preSize = this.entries.size();
        try (JsonReader jReader = new JsonReader(new FileReader(this.path + "spellbook.json"))) {
            Type collectionType = new TypeToken<TreeSet<SpellEntry>>() {
            }.getType();
            NavigableSet<SpellEntry> retrieved = gson.fromJson(jReader, collectionType);
            this.logger.log(Level.CONFIG, retrieved.toString());
            this.entries.addAll(retrieved);
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
            return false;
        }
        this.logger.log(Level.INFO, String.format("Spellbook size changed by %d\n", this.entries.size() - preSize));
        return true;
    }

    public NavigableSet<SpellEntry> getEntries() {
        return Collections.unmodifiableNavigableSet(entries);
    }

    public boolean addEntry(SpellEntry entry) {
        return this.entries.add(entry);
    }

    public NavigableSet<SpellEntry> filter(EnumSet<Filters> filters,
            VocationName vocationName, String spellName, String invocation, EnumSet<SpellLevel> levels) {
        Supplier<TreeSet<SpellEntry>> sortSupplier = () -> filters.contains(Filters.SCORE)
                ? new TreeSet<SpellEntry>((entry1, entry2) -> entry2.aiScore() - entry1.aiScore())
                : new TreeSet<SpellEntry>();
        return this.getEntries().stream()
                .filter(entry -> entry != null)
                .filter(entry -> {
                    if (!filters.contains(Filters.VOCATION_NAME)) {
                        return true;
                    }
                    if (vocationName == null || !vocationName.isCubeHolder()) {
                        return false;
                    }
                    return entry.getAllowedVocations().size() == 0 ||
                            entry.getAllowedVocations().contains(vocationName) ||
                            VocationName.DUNGEON_MASTER.equals(vocationName);
                })
                .filter(entry -> !filters.contains(Filters.SPELL_NAME) || entry.getName().equals(spellName))
                .filter(entry -> !filters.contains(Filters.INVOCATION) || entry.getInvocation().equals(invocation))
                .filter(entry -> !filters.contains(Filters.LEVELS) ||
                        (levels != null && (levels.size() == 0 || levels.contains(entry.getLevel()))))
                .filter(entry -> !filters.contains(Filters.OFFENSE) || entry.isOffensive())
                .filter(entry -> !filters.contains(Filters.NONOFFENSE) || !entry.isOffensive())
                .collect(Collectors.toCollection(sortSupplier));
    }

    public NavigableSet<SpellEntry> filterByExactLevel(SpellLevel level) {
        return this.filter(EnumSet.of(Filters.LEVELS), null, null, null, EnumSet.of(level));
    }

    public NavigableSet<SpellEntry> filterByVocationName(VocationName vocationName) {
        return this.filter(EnumSet.of(Filters.VOCATION_NAME), vocationName, null, null, null);
    }

    public NavigableSet<SpellEntry> filterByVocationAndLevels(VocationName vocationName, EnumSet<SpellLevel> levels) {
        return this.filter(EnumSet.of(Filters.VOCATION_NAME, Filters.LEVELS), vocationName, null, null, levels);
    }

    public NavigableSet<SpellEntry> filterByExactName(String name) {
        return this.filter(EnumSet.of(Filters.SPELL_NAME), null, name, null, null);
    }

    public NavigableSet<SpellEntry> filterByExactInvocation(String invocation) {
        return this.filter(EnumSet.of(Filters.INVOCATION), null, null, invocation, null);
    }
}
