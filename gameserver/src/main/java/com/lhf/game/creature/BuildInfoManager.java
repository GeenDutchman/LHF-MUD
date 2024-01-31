package com.lhf.game.creature;

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
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lhf.game.serialization.GsonBuilderFactory;

public class BuildInfoManager {
    private Logger logger;
    private String[] path_to_monsterStatblocks = { ".", "monsterStatblocks" };
    private String path;

    public BuildInfoManager() {
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

    public Boolean statblockToFile(GsonBuilderFactory gsonBuilderFactory, ICreatureBuildInfo statblock) {
        if (gsonBuilderFactory == null) {
            gsonBuilderFactory = GsonBuilderFactory.start();
        }
        Gson gson = gsonBuilderFactory.prettyPrinting().items().creatureInfoBuilders().build();
        try (JsonWriter jWriter = gson.newJsonWriter(
                new FileWriter(this.path.toString() + statblock.getCreatureRace() + ".json"))) {
            gson.toJson(statblock, ICreatureBuildInfo.class, jWriter);
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
            return false;
        }
        String rightWritePath = this.path.replaceAll("target(.)classes", "src$1main$1resources");
        this.logger.log(Level.INFO, "Also writing to: " + rightWritePath);
        try (JsonWriter jWriter = gson.newJsonWriter(
                new FileWriter(rightWritePath.toString() + statblock.getCreatureRace() + ".json"))) {
            gson.toJson(statblock, ICreatureBuildInfo.class, jWriter);
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public ICreatureBuildInfo statblockFromfile(GsonBuilderFactory gsonBuilderFactory, String name)
            throws FileNotFoundException {
        if (gsonBuilderFactory == null) {
            gsonBuilderFactory = GsonBuilderFactory.start();
        }
        Gson gson = gsonBuilderFactory.prettyPrinting().items().creatureInfoBuilders().build();
        String statblockFile = this.path.toString() + name + ".json";
        this.logger.log(Level.INFO, "Opening file: " + statblockFile);
        JsonReader jReader = new JsonReader(new FileReader(statblockFile));
        ICreatureBuildInfo statblock = gson.fromJson(jReader, ICreatureBuildInfo.class);
        return statblock;
    }

}
