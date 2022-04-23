package com.lhf.game.creature.statblock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemDeserializer;
import com.lhf.game.item.TakeableDeserializer;
import com.lhf.game.item.interfaces.Takeable;

public class StatblockManager {
    private String[] path_to_monsterStatblocks = { ".", "gameserver", "src", "main", "java", "com",
            "lhf", "game", "creature", "monsterStatblocks" };
    private StringBuilder path = new StringBuilder();

    public StatblockManager() {
        for (String part : path_to_monsterStatblocks) {
            this.path.append(part).append(File.separator);
        }
        System.out.println("Using directory " + Paths.get(".").toAbsolutePath().normalize().toString());
        System.out.println(this.path.toString());
    }

    public Boolean statblockToFile(Statblock statblock) {
        GsonBuilder gBuilder = new GsonBuilder().setPrettyPrinting();
        Gson gson = gBuilder.create();
        try (JsonWriter jWriter = new JsonWriter(
                new FileWriter(this.path.toString() + statblock.creatureRace + ".json"))) {
            gson.toJson(statblock, Statblock.class, jWriter);
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Statblock statblockFromfile(String name) throws FileNotFoundException {
        GsonBuilder gBuilder = new GsonBuilder().setPrettyPrinting();
        gBuilder.registerTypeAdapter(Takeable.class, new TakeableDeserializer<>());
        gBuilder.registerTypeAdapter(Item.class, new ItemDeserializer<>());
        Gson gson = gBuilder.create();
        JsonReader jReader = new JsonReader(new FileReader(this.path.toString() + name + ".json"));
        Statblock statblock = gson.fromJson(jReader, Statblock.class);
        return statblock;
    }

}
