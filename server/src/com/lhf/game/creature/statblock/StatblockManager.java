package com.lhf.game.creature.statblock;

import java.io.*;
import java.nio.file.Paths;

public class StatblockManager {
    private BufferedWriter writer;
    private BufferedReader reader;
    private String[] path_to_monsterStatblocks = { ".", "server", "src", "com", "lhf", "game", "creature",
            "monsterStatblocks" };
    private StringBuilder path = new StringBuilder();

    public StatblockManager() {
        for (String part : path_to_monsterStatblocks) {
            path.append(part).append(File.separator);
        }
        System.out.println("Using directory " + Paths.get(".").toAbsolutePath().normalize().toString());
        System.out.println(path.toString());
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
}
