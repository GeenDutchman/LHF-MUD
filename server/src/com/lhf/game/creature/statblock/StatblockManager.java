package com.lhf.game.creature.statblock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
    private BufferedWriter writer;
    private BufferedReader reader;
    private String[] path_to_monsterStatblocks = { ".", "server", "src", "com", "lhf", "game", "creature",
            "monsterStatblocks" };
    private StringBuilder path = new StringBuilder();

    public StatblockManager() {
        for (String part : path_to_monsterStatblocks) {
            this.path.append(part).append(File.separator);
        }
        System.out.println("Using directory " + Paths.get(".").toAbsolutePath().normalize().toString());
        System.out.println(this.path.toString());
    }

    public void statblockToFile(Statblock statblock) {
        try {
            System.out.println("Using write file: " + path.toString() + statblock.getName());
            writer = new BufferedWriter(new FileWriter(path.toString() + statblock.getName()));
            writer.write(statblock.toString());
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing file");
            e.printStackTrace();
        }
    }

    public String statblockFromfile(String name) {
        char[] file_contents = new char[2048];
        try {
            System.out.println("Using read file: " + path.toString() + name);
            reader = new BufferedReader(new FileReader(path.toString() + name));
            reader.read(file_contents, 0, 2048);
            reader.close();
        } catch (IOException e) {
            System.err.println("Error loading file");
            e.printStackTrace();
        }

        return new String(file_contents);
    }

    public StatblockV2 statblockV2FromMonsterFile(String name) throws FileNotFoundException {
        GsonBuilder gBuilder = new GsonBuilder().setPrettyPrinting();
        gBuilder.registerTypeAdapter(Takeable.class, new TakeableDeserializer<>());
        gBuilder.registerTypeAdapter(Item.class, new ItemDeserializer<>());
        Gson gson = gBuilder.create();
        JsonReader jReader = new JsonReader(new FileReader(this.path.toString() + name + ".json"));
        StatblockV2 statblock = gson.fromJson(jReader, StatblockV2.class);
        return statblock;
    }

    public Boolean statblockV2ToMonsterFile(StatblockV2 statblock) {
        GsonBuilder gBuilder = new GsonBuilder().setPrettyPrinting();
        Gson gson = gBuilder.create();
        try (JsonWriter jWriter = new JsonWriter(
                new FileWriter(this.path.toString() + statblock.getCreatureRace() + ".json"))) {
            gson.toJson(statblock, StatblockV2.class, jWriter);
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
