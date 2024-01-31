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
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lhf.game.creature.DungeonMaster.DungeonMasterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.Player.PlayerBuildInfo;
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
        this.logger.log(Level.CONFIG, "Made path: " + makePath.toString());
        // See https://stackoverflow.com/a/3844316
        URL statblockDir = getClass().getResource(makePath.toString());
        this.logger.log(Level.CONFIG, String.format("URL: %s", statblockDir));
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

    public MonsterBuildInfo monsterBuildInfoFromFile(GsonBuilderFactory gsonBuilderFactory, String name)
            throws JsonIOException, JsonSyntaxException, IOException {
        if (gsonBuilderFactory == null) {
            gsonBuilderFactory = GsonBuilderFactory.start();
        }
        Gson gson = gsonBuilderFactory.prettyPrinting().items().creatureInfoBuilders().build();
        String statblockFile = this.path.toString() + name + ".json";
        this.logger.log(Level.INFO, "Opening file: " + statblockFile);
        try (JsonReader jReader = new JsonReader(new FileReader(statblockFile))) {
            return gson.fromJson(jReader, MonsterBuildInfo.class);
        } catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
            throw e;
        }
    }

    public INPCBuildInfo iNPCBuildInfoFromFile(GsonBuilderFactory gsonBuilderFactory, String name)
            throws JsonIOException, JsonSyntaxException, IOException {
        if (gsonBuilderFactory == null) {
            gsonBuilderFactory = GsonBuilderFactory.start();
        }
        Gson gson = gsonBuilderFactory.prettyPrinting().items().creatureInfoBuilders().build();
        String statblockFile = this.path.toString() + name + ".json";
        this.logger.log(Level.INFO, "Opening file: " + statblockFile);
        try (JsonReader jReader = new JsonReader(new FileReader(statblockFile))) {
            return gson.fromJson(jReader, INPCBuildInfo.class);
        } catch (JsonIOException | JsonSyntaxException | IOException e) {
            throw e;
        }
    }

    public DungeonMasterBuildInfo DMBuildInfoFromFile(GsonBuilderFactory gsonBuilderFactory, String name)
            throws JsonIOException, JsonSyntaxException, IOException {
        if (gsonBuilderFactory == null) {
            gsonBuilderFactory = GsonBuilderFactory.start();
        }
        Gson gson = gsonBuilderFactory.prettyPrinting().items().creatureInfoBuilders().build();
        String statblockFile = this.path.toString() + name + ".json";
        this.logger.log(Level.INFO, "Opening file: " + statblockFile);
        try (JsonReader jReader = new JsonReader(new FileReader(statblockFile))) {
            return gson.fromJson(jReader, DungeonMasterBuildInfo.class);
        } catch (JsonIOException | JsonSyntaxException | IOException e) {
            throw e;
        }
    }

    public PlayerBuildInfo playerBuildInfoFromFile(GsonBuilderFactory gsonBuilderFactory, String name)
            throws JsonIOException, JsonSyntaxException, IOException {
        if (gsonBuilderFactory == null) {
            gsonBuilderFactory = GsonBuilderFactory.start();
        }
        Gson gson = gsonBuilderFactory.prettyPrinting().items().creatureInfoBuilders().build();
        String statblockFile = this.path.toString() + name + ".json";
        this.logger.log(Level.INFO, "Opening file: " + statblockFile);
        try (JsonReader jReader = new JsonReader(new FileReader(statblockFile))) {
            return gson.fromJson(jReader, PlayerBuildInfo.class);
        } catch (JsonIOException | JsonSyntaxException | IOException e) {
            throw e;
        }
    }

    public ICreatureBuildInfo creatureBuildInfoFromFile(GsonBuilderFactory gsonBuilderFactory,
            String name) throws IOException, JsonParseException {
        if (gsonBuilderFactory == null) {
            gsonBuilderFactory = GsonBuilderFactory.start();
        }
        ICreatureBuildInfo buildInfo = null;
        try {
            buildInfo = this.monsterBuildInfoFromFile(gsonBuilderFactory, name);
        } catch (IOException e) {
            throw e;
        } catch (JsonIOException | JsonSyntaxException e) {
            try {
                buildInfo = this.iNPCBuildInfoFromFile(gsonBuilderFactory, name);
            } catch (JsonIOException | JsonSyntaxException | IOException e1) {
                try {
                    buildInfo = this.DMBuildInfoFromFile(gsonBuilderFactory, name);
                } catch (JsonIOException | JsonSyntaxException | IOException e2) {
                    try {
                        buildInfo = this.playerBuildInfoFromFile(gsonBuilderFactory, name);
                    } catch (JsonIOException | JsonSyntaxException | IOException e3) {
                        throw new JsonParseException(
                                String.format("Cannot deserialize file '%s' as a monster, npc, dm, or player", name),
                                e3);
                    }
                }
            }
        }

        return buildInfo;
    }

}
