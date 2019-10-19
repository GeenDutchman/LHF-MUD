package com.lhf.game.map;

import com.lhf.game.map.objects.Note;

public class DungeonBuilder {

    public static Dungeon buildStaticDungeon() {
        Dungeon dungeon = new Dungeon();

        //Entry Room
        Room entryRoom = new Room("This is the entry room.");


        //History Hall
        Room historyHall = new Room("This is the history hall.");
        Note loreNote = new Note("ominous lore", true, "You read the page and it says 'This page intentionally left blank.'");
        historyHall.addObject(loreNote);

        //Path
        entryRoom.addExit("forward", historyHall);
        historyHall.addExit("backward", entryRoom);

        //Set starting room
        dungeon.setStartingRoom(entryRoom);

        //Add to Dungeon
        dungeon.addRoom(entryRoom);
        dungeon.addRoom(historyHall);

        return dungeon;
    }

    public static Dungeon buildDynamicDungeon(int seed) {

        return null;
    }
}
