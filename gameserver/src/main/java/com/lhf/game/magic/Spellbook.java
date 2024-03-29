package com.lhf.game.magic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.magic.concrete.ElectricWisp;
import com.lhf.game.magic.concrete.Ensouling;
import com.lhf.game.magic.concrete.ShockBolt;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.concrete.ThunderStrike;
import com.lhf.game.serialization.GsonBuilderFactory;

public class Spellbook {
    private NavigableSet<SpellEntry> entries;
    private String path;
    private final String[] path_to_spellbook = { ".", "concrete" };
    private transient Logger logger;

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
        this.logger.log(Level.CONFIG, "Creating empty spellbook");
        this.entries = new TreeSet<>();
        this.setupPath();
    }

    public Spellbook addConcreteSpells() {
        this.logger.log(Level.CONFIG, "Loading initial small spellset");
        SpellEntry shockBolt = new ShockBolt();
        this.entries.add(shockBolt);
        SpellEntry thaumaturgy = new Thaumaturgy();
        this.entries.add(thaumaturgy);
        SpellEntry thunderStrike = new ThunderStrike();
        this.entries.add(thunderStrike);
        SpellEntry ensouling = new Ensouling();
        this.entries.add(ensouling);
        SpellEntry electricWisp = new ElectricWisp();
        this.entries.add(electricWisp);
        return this;
    }

    private void setupPath() {
        StringBuilder makePath = new StringBuilder();
        for (String part : path_to_spellbook) {
            makePath.append(part).append(File.separator);
        }
        this.logger.log(Level.CONFIG,
                "Current Working Directory: " + Paths.get(".").toAbsolutePath().normalize().toString());
        this.logger.log(Level.CONFIG, "Made path: " + makePath.toString());
        URL spellbookDir = getClass().getResource(makePath.toString());
        this.logger.log(Level.CONFIG, String.format("URL: %s", spellbookDir));
        this.path = spellbookDir.getPath().replaceAll("target(.)classes",
                "src$1main$1resources");
        this.logger.log(Level.CONFIG, "directory " + this.path);
    }

    public boolean saveToFile(GsonBuilderFactory gsonBuilderFactory) throws IOException {
        return this.saveToFile(true, gsonBuilderFactory);
    }

    @Deprecated(forRemoval = false)
    protected boolean saveToFile(boolean loadFirst, GsonBuilderFactory gsonBuilderFactory) throws IOException {
        this.logger.entering(this.getClass().getName(), "saveToFile()", path_to_spellbook);
        if (gsonBuilderFactory == null) {
            gsonBuilderFactory = GsonBuilderFactory.start();
        }
        gsonBuilderFactory.prettyPrinting().spells();
        if (loadFirst && !this.loadFromFile(gsonBuilderFactory)) {
            throw new IOException("Cannot preload spellbook!");
        }
        Gson gson = gsonBuilderFactory.prettyPrinting().spells().build();
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

    public boolean loadFromFile(GsonBuilderFactory gsonBuilderFactory) {
        if (gsonBuilderFactory == null) {
            gsonBuilderFactory = GsonBuilderFactory.start();
        }
        Gson gson = gsonBuilderFactory.prettyPrinting().spells().build();
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
            VocationName vocationName, String spellName, String invocation, EnumSet<ResourceCost> levels) {
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

    public NavigableSet<SpellEntry> filterByExactLevel(ResourceCost level) {
        return this.filter(EnumSet.of(Filters.LEVELS), null, null, null, EnumSet.of(level));
    }

    public NavigableSet<SpellEntry> filterByVocationName(VocationName vocationName) {
        return this.filter(EnumSet.of(Filters.VOCATION_NAME), vocationName, null, null, null);
    }

    public NavigableSet<SpellEntry> filterByVocationAndLevels(VocationName vocationName, EnumSet<ResourceCost> levels) {
        return this.filter(EnumSet.of(Filters.VOCATION_NAME, Filters.LEVELS), vocationName, null, null, levels);
    }

    public NavigableSet<SpellEntry> filterByExactName(String name) {
        return this.filter(EnumSet.of(Filters.SPELL_NAME), null, name, null, null);
    }

    public NavigableSet<SpellEntry> filterByExactInvocation(String invocation) {
        return this.filter(EnumSet.of(Filters.INVOCATION), null, null, invocation, null);
    }
}
