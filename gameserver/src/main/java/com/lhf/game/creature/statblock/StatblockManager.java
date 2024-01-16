package com.lhf.game.creature.statblock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.EquipableDeserializer;
import com.lhf.game.item.AItem;
import com.lhf.game.item.ItemDeserializer;
import com.lhf.game.item.Takeable;
import com.lhf.game.item.TakeableDeserializer;

public class StatblockManager {
    private Logger logger;
    private String[] path_to_monsterStatblocks = { ".", "monsterStatblocks" };
    private String path;

    public StatblockManager() {
        this.logger = Logger.getLogger(this.getClass().getName());
        StringBuilder makePath = new StringBuilder();
        for (String part : path_to_monsterStatblocks) {
            makePath.append(part).append(File.separator);
        }
        this.logger.log(Level.CONFIG,
                "Current Working Directory: " + Paths.get(".").toAbsolutePath().normalize().toString());
        // See https://stackoverflow.com/a/3844316
        URL statblockDir = getClass().getResource(makePath.toString());
        this.path = statblockDir.getPath();
        this.logger.log(Level.CONFIG, "directory " + this.path);
    }

    public Boolean statblockToFile(Statblock statblock) {
        GsonBuilder gBuilder = new GsonBuilder().setPrettyPrinting();
        Gson gson = gBuilder.create();
        try (JsonWriter jWriter = gson.newJsonWriter(
                new FileWriter(this.path.toString() + statblock.getCreatureRace() + ".json"))) {
            gson.toJson(statblock, Statblock.class, jWriter);
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
            return false;
        }
        String rightWritePath = this.path.replaceAll("target(.)classes", "src$1main$1resources");
        this.logger.log(Level.INFO, "Also writing to: " + rightWritePath);
        try (JsonWriter jWriter = gson.newJsonWriter(
                new FileWriter(rightWritePath.toString() + statblock.getCreatureRace() + ".json"))) {
            gson.toJson(statblock, Statblock.class, jWriter);
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Statblock statblockFromfile(String name) throws FileNotFoundException {
        GsonBuilder gBuilder = new GsonBuilder().setPrettyPrinting();
        gBuilder.registerTypeAdapter(Equipable.class, new EquipableDeserializer<Equipable>());
        gBuilder.registerTypeAdapter(Takeable.class, new TakeableDeserializer<>());
        gBuilder.registerTypeAdapter(AItem.class, new ItemDeserializer<>());
        Gson gson = gBuilder.create();
        String statblockFile = this.path.toString() + name + ".json";
        this.logger.log(Level.INFO, "Opening file: " + statblockFile);
        JsonReader jReader = new JsonReader(new FileReader(statblockFile));
        Statblock statblock = gson.fromJson(jReader, Statblock.class);
        return statblock;
    }

}
