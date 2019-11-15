package com.lhf.game.creature;

import java.io.*;
import java.nio.file.Path;

public class StatblockLoader_Unloader {
    public StatblockLoader_Unloader(){
    }
    private BufferedWriter writer;
    private BufferedReader reader;
    private String path_to_monsterStatblocks =".\\src\\com\\lhf\\game\\creature\\monsterStatblocks\\";

    public void statblockToFile(Statblock statblock){
        try {
            writer = new BufferedWriter(new FileWriter(path_to_monsterStatblocks+statblock.getName()));
            writer.write(statblock.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String statblockFromfile(String name){
        char[]file_contents = new char[2048];
        try {
            reader = new BufferedReader(new FileReader(path_to_monsterStatblocks+name));
            reader.read(file_contents,0,2048);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String(file_contents);
    }
}
