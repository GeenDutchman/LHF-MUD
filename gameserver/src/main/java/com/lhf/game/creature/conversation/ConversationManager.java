package com.lhf.game.creature.conversation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ConversationManager {
    private Logger logger;
    private String[] path_to_conversations = { "." };
    private String path;

    public ConversationManager() {
        this.logger = Logger.getLogger(this.getClass().toString());
        StringBuilder makePath = new StringBuilder();
        for (String part : path_to_conversations) {
            makePath.append(part).append(File.separator);
        }
        this.logger.config("Current Working Directory: " + Paths.get(".").toAbsolutePath().normalize().toString());
        // See https://stackoverflow.com/a/3844316
        URL convoDir = getClass().getResource(makePath.toString());
        this.path = convoDir.getPath();
        this.logger.config("directory " + this.path);
    }

    public Boolean convoTreeToFile(ConversationTree tree) {
        GsonBuilder gb = new GsonBuilder().setPrettyPrinting();
        gb.registerTypeAdapter(ConversationPattern.class, new ConversationPatternSerializer());
        Gson gson = gb.create();
        String rightWritePath = this.path.replaceAll("target(.)classes", "src$1main$1resources");
        this.logger.info("Writing to: " + rightWritePath);
        try (JsonWriter jWriter = gson.newJsonWriter(
                new FileWriter(rightWritePath.toString() + tree.getTreeName() + ".json"))) {
            gson.toJson(tree, ConversationTree.class, jWriter);
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public ConversationTree convoTreeFromFile(String name) throws FileNotFoundException {
        GsonBuilder gb = new GsonBuilder().setPrettyPrinting();
        gb.registerTypeAdapter(ConversationPattern.class, new ConversationPatternSerializer());
        Gson gson = gb.create();
        String convoFile = this.path.toString() + name + ".json";
        this.logger.info("Opening file: " + convoFile);
        JsonReader jReader = new JsonReader(new FileReader(convoFile));
        ConversationTree tree = gson.fromJson(jReader, ConversationTree.class);
        tree.initBookmarks();
        return tree;
    }
}
