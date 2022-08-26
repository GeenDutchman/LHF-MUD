package com.lhf.game.magic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.magic.concrete.ShockBolt;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.concrete.ThunderStrike;
import com.lhf.game.map.DungeonEffectSource;
import com.lhf.game.map.RoomEffectSource;

public class Spellbook {
    private SortedSet<SpellEntry> entries;
    private String path;
    private final String[] path_to_spellbook = { ".", "concrete" };

    public Spellbook() {
        this.entries = new TreeSet<>();
        SpellEntry shockBolt = new ShockBolt();
        this.entries.add(shockBolt);
        SpellEntry thaumaturgy = new Thaumaturgy();
        this.entries.add(thaumaturgy);
        SpellEntry thunderStrike = new ThunderStrike();
        this.entries.add(thunderStrike);
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
                .registerSubtype(DungeonTargetingSpellEntry.class, DungeonTargetingSpellEntry.class.getName())
                .registerSubtype(ShockBolt.class, ShockBolt.class.getName())
                .registerSubtype(ThunderStrike.class, ThunderStrike.class.getName())
                .registerSubtype(Thaumaturgy.class, Thaumaturgy.class.getName())
                .recognizeSubtypes();
        RuntimeTypeAdapterFactory<EntityEffectSource> effectSourceAdapter = RuntimeTypeAdapterFactory
                .of(EntityEffectSource.class, "className", true)
                .registerSubtype(CreatureEffectSource.class, CreatureEffectSource.class.getName())
                .registerSubtype(RoomEffectSource.class, RoomEffectSource.class.getName())
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
        if (loadFirst && !this.loadFromFile()) {
            throw new IOException("Cannot preload spellbook!");
        }
        Gson gson = this.getAdaptedGson();
        System.out.println("Writing to " + this.path);
        try (FileWriter fileWriter = new FileWriter(this.path + "spellbook.json")) {
            String asJson = gson.toJson(this.entries, SpellEntry.class);
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

    public SortedSet<SpellEntry> getEntries() {
        return Collections.unmodifiableSortedSet(entries);
    }

    public boolean addEntry(SpellEntry entry) {
        return this.entries.add(entry);
    }
}
