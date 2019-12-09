package com.lhf.game.map;

import com.lhf.game.creature.Monster;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.enums.HealType;
import com.lhf.game.item.Note;
import com.lhf.game.item.concrete.*;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.map.objects.roomobject.Dispenser;
import com.lhf.game.map.objects.roomobject.Switch;
import com.lhf.game.map.objects.roomobject.actions.DispenserAction;
import com.lhf.game.map.objects.roomobject.interfaces.InteractAction;
import javafx.beans.property.ReadOnlyBooleanProperty;

public class DungeonBuilder {

    public static Dungeon buildStaticDungeon() {
        Dungeon dungeon = new Dungeon();

        StatblockManager loader = new StatblockManager();
        Statblock goblin = new Statblock(loader.statblockFromfile("goblin"));
        Statblock bugbear = new Statblock(loader.statblockFromfile("bugbear"));
        Statblock hobgoblin = new Statblock(loader.statblockFromfile("hobgoblin"));

        //Entry Room RM1
        Room entryRoom = new Room("This is the entry room.");
        Note addNote = new Note("interact note", true, "This note is to test the switch action.");

        //Switch test start
        Switch testSwitch = new Switch("test switch", true, false, "This looks like a test switch.");
        //Set items the action is going to use
        testSwitch.setItem("note", addNote);
        testSwitch.setItem("room", entryRoom);
        //Create action as anonymous function
        InteractAction testAction = (player, args) -> {
            //You can do anything you imagine inside, just with casting overhead (for now)
            //This can be used for the secret room trigger, since a switch can be hidden
            Object o1 = args.get("note");
            if (!(o1 instanceof Note)) {
                return "Switch error.";
            }
            Note n = (Note)o1;
            Object o2 = args.get("room");
            if (!(o2 instanceof Room)) {
                return "Switch error.";
            }
            Room r = (Room)o2;
            r.addItem(n);
            return "Switch activated. A note dropped from the ceiling.";
        };
        //Set Action
        testSwitch.setAction(testAction);
        //Switch test end
        entryRoom.addObject(testSwitch);




        //History Hall RM2
        Room historyHall = new Room("This is the history hall.");
        Note loreNote = new Note("ominous lore", true, "You read the page and it says 'This page intentionally left blank.'");
        historyHall.addItem(loreNote);

        RustyDagger dagger = new RustyDagger(true);



        historyHall.addItem(dagger);
        //Test dispenser start - could be used for other items
        Dispenser dispenser = new Dispenser("note dispenser", true, false, "It looks like a mailbox with a big lever.  Something probably comes out of that slot.");
        Note generatedNote = new Note("note", true, "This is a autogenerated note.");
        dispenser.setItem("room", historyHall);
        dispenser.setItem("disp", dispenser);
        dispenser.setItem("item", generatedNote);
        dispenser.setItem("message", "A note fell out of the dispenser.");
        InteractAction testAction2 = new DispenserAction();
        dispenser.setAction(testAction2);
        //Test dispenser end
        historyHall.addObject(dispenser);

        //RM3
        Room offeringRoom = new Room("This is the offering room.");

        //RM4
        Room trappedHall = new Room("This is the trapped room.");
        HealPotion h1 = new HealPotion(true);
        trappedHall.addItem(h1);


        //RM5
        Room statueRoom = new Room("This is the statue room.");


        Note bossNote = new Note("note from boss", true, "The tutorial boss is on vacation right now.");
        statueRoom.addItem(bossNote);

        Room secretRoom = new Room("This is the secret room!");

        MantleOfDeath mantle = new MantleOfDeath(false);
        ReaperScythe scythe = new ReaperScythe(false);

        HealPotion healPotion = new HealPotion(true);
        secretRoom.addItem(healPotion);
        secretRoom.addItem(mantle);
        secretRoom.addItem(scythe);

        Switch statue = new Switch("golden statue", true, true, "The statue has a start to a riddle, but it looks like it hasn't been finished yet.");
        statue.setItem("room1", statueRoom);
        statue.setItem("room2", secretRoom);
        InteractAction statueAction = (player, args) -> {
            Object o1 = args.get("room1");
            if (!(o1 instanceof Room)) {
                return "Switch error 1.";
            }
            Room r1 = (Room) o1;

            Object o2 = args.get("room2");
            if (!(o2 instanceof Room)) {
                return "Switch error 1.";
            }
            Room r2 = (Room) o2;

            r1.removePlayer(player);
            r2.addPlayer(player);

            return "The statue glows and you black out for a second. You find yourself in another room.";
        };
        statue.setAction(statueAction);
        statueRoom.addObject(statue);

        //RM6 The armory
        Room armory = new Room("An armory");
        CarnivorousArmor mimic = new CarnivorousArmor(true);
        ChainMail mail = new ChainMail(true);
        Whimsystick stick = new Whimsystick(true);
        Shortsword shortsword = new Shortsword(true);
        HealPotion potion = new HealPotion(true);
        armory.addItem(mimic);
        armory.addItem(mail);
        armory.addItem(stick);
        armory.addItem(shortsword);
        armory.addItem(potion);

        //RM7
        Room passage = new Room("An old dusty passageway");
        //RM8
        Room treasury = new Room("A looted vault room");
        HealPotion regular = new HealPotion(true);
        HealPotion greater = new HealPotion(HealType.Greater);
        HealPotion critical = new HealPotion(HealType.Critical);

        treasury.addItem(regular);
        treasury.addItem(greater);
        treasury.addItem(critical);


        //Monsters
        Monster g1 = new Monster("goblin",goblin);
        historyHall.addCreature(g1);

        Monster boss = new Monster("Boss Bear", bugbear);
        statueRoom.addCreature(boss);

        Monster rightHandMan = new Monster("Right",hobgoblin);
        offeringRoom.addCreature(rightHandMan);



        //Path
        entryRoom.addExit("east", historyHall);

        historyHall.addExit("west", entryRoom);
        historyHall.addExit("east", offeringRoom);
        historyHall.addExit("north", armory);


        offeringRoom.addExit("west", historyHall);
        offeringRoom.addExit("east", trappedHall);

        trappedHall.addExit("west", offeringRoom);
        trappedHall.addExit("south", statueRoom);
        trappedHall.addExit("north",treasury);

        statueRoom.addExit("north", trappedHall);

        secretRoom.addExit("secret door", statueRoom);

        armory.addExit("east",passage);
        armory.addExit("south",historyHall);

        passage.addExit("south",treasury);
        passage.addExit("west", armory);

        treasury.addExit("north",passage);
        treasury.addExit("south",trappedHall);


        //Set starting room
        dungeon.setStartingRoom(entryRoom);

        //Add to Dungeon
        dungeon.addRoom(entryRoom);
        dungeon.addRoom(historyHall);
        dungeon.addRoom(offeringRoom);
        dungeon.addRoom(trappedHall);
        dungeon.addRoom(statueRoom);
        dungeon.addRoom(secretRoom);
        dungeon.addRoom(armory);
        dungeon.addRoom(passage);
        dungeon.addRoom(treasury);

        return dungeon;
    }

    public static Dungeon buildDynamicDungeon(int seed) {

        return null;
    }
}
