package com.lhf.game.map;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.lhf.game.creature.Monster;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.enums.HealType;
import com.lhf.game.item.DispenserAction;
import com.lhf.game.item.concrete.Dispenser;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.item.concrete.Note;
import com.lhf.game.item.concrete.Switch;
import com.lhf.game.item.concrete.equipment.CarnivorousArmor;
import com.lhf.game.item.concrete.equipment.ChainMail;
import com.lhf.game.item.concrete.equipment.MantleOfDeath;
import com.lhf.game.item.concrete.equipment.ReaperScythe;
import com.lhf.game.item.concrete.equipment.RustyDagger;
import com.lhf.game.item.concrete.equipment.Shortsword;
import com.lhf.game.item.concrete.equipment.Whimsystick;
import com.lhf.game.item.interfaces.InteractAction;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.InteractOutMessage;
import com.lhf.messages.out.InteractOutMessage.InteractOutMessageType;

public class DungeonBuilder {

    private Room startingRoom;
    private MessageHandler successor;
    private Map<Room, Map<Directions, Room>> mapping;
    private List<Room> orderAdded;

    public static DungeonBuilder newInstance() {
        return new DungeonBuilder();
    }

    private DungeonBuilder() {
        this.mapping = new HashMap<>();
        this.orderAdded = new ArrayList<>();
    }

    public DungeonBuilder addStartingRoom(Room startingRoom) {
        this.startingRoom = startingRoom;
        this.mapping.putIfAbsent(this.startingRoom, new TreeMap<>());
        this.orderAdded.add(startingRoom);
        return this;
    }

    public DungeonBuilder setSuccessor(MessageHandler successor) {
        this.successor = successor;
        return this;
    }

    public DungeonBuilder connectRoom(Room existing, Directions toExistingRoom, Room toAdd) {
        assert this.mapping.containsKey(existing) : existing.getName() + " not yet added";
        Directions toNewRoom = toExistingRoom.opposite();
        this.mapping.putIfAbsent(toAdd, new TreeMap<>());
        Map<Directions, Room> toAddExits = this.mapping.get(toAdd);
        assert !toAddExits.containsKey(toExistingRoom)
                : toAdd.getName() + " already has direction " + toExistingRoom.toString();
        toAddExits.put(toExistingRoom, existing);
        Map<Directions, Room> existingExits = this.mapping.get(existing);
        assert !existingExits.containsKey(toNewRoom)
                : existing.getName() + " already has direction " + toNewRoom.toString();
        existingExits.put(toNewRoom, toAdd);
        this.orderAdded.add(toAdd);
        return this;
    }

    public DungeonBuilder connectRoomOneWay(Room existing, Directions toExistingRoom, Room secretRoom) {
        assert this.mapping.containsKey(existing) : existing.getName() + " not yet added";
        this.mapping.putIfAbsent(secretRoom, new TreeMap<>());
        Map<Directions, Room> secretExits = this.mapping.get(secretRoom);
        assert !secretExits.containsKey(toExistingRoom)
                : secretRoom.getName() + " already has direction " + toExistingRoom.toString();
        secretExits.put(toExistingRoom, existing);
        this.orderAdded.add(secretRoom);
        return this;
    }

    public Dungeon build() {
        Dungeon dungeon = new Dungeon(this.successor);
        dungeon.setStartingRoom(this.startingRoom);
        for (Room room : this.orderAdded) {
            for (Map.Entry<Directions, Room> exits : this.mapping.get(room).entrySet()) {
                if (this.mapping.get(exits.getValue()).containsKey(exits.getKey().opposite())) {
                    dungeon.connectRoom(room, exits.getKey().opposite(), exits.getValue());
                } else {
                    dungeon.connectRoomOneWay(exits.getValue(), exits.getKey().opposite(), room);
                }
            }
        }
        return dungeon;
    }

    public static Dungeon buildStaticDungeon(MessageHandler successor) throws FileNotFoundException {
        DungeonBuilder builder = DungeonBuilder.newInstance();
        builder.setSuccessor(successor);

        ConversationManager convoLoader = new ConversationManager();
        StatblockManager loader = new StatblockManager();
        Statblock goblin = loader.statblockFromfile("goblin");
        Statblock bugbear = loader.statblockFromfile("bugbear");
        Statblock hobgoblin = loader.statblockFromfile("hobgoblin");

        // Entry Room RM1
        Room entryRoom = new Room("Entry Room", "This is the entry room.");
        Note addNote = new Note("interact note", true, "This note is to test the switch action.");

        // Switch test start
        Switch testSwitch = new Switch("test switch", true, false, "This looks like a test switch.");
        // Set items the action is going to use
        testSwitch.setItem("note", addNote);
        testSwitch.setItem("room", entryRoom);
        // Create action as anonymous function
        InteractAction testAction = (creature, triggerObject, args) -> {
            // You can do anything you imagine inside, just with casting overhead (for now)
            // This can be used for the secret room trigger, since a switch can be hidden
            Object o1 = args.get("note");
            if (!(o1 instanceof Note)) {
                Logger.getLogger(triggerObject.getClassName()).warning("Note not found");
                return new InteractOutMessage(triggerObject, InteractOutMessageType.ERROR);
            }
            Note n = (Note) o1;
            Object o2 = args.get("room");
            if (!(o2 instanceof Room)) {
                Logger.getLogger(triggerObject.getClassName()).warning("Room not found");
                return new InteractOutMessage(triggerObject, InteractOutMessageType.ERROR);
            }
            Room r = (Room) o2;
            r.addItem(n);
            return new InteractOutMessage(triggerObject, "Switch activated. A note dropped from the ceiling.");
        };
        // Set Action
        testSwitch.setAction(testAction);
        // Switch test end
        entryRoom.addItem(testSwitch);

        // History Hall RM2
        Room historyHall = new Room("History Hall", "This is the history hall.");
        Note loreNote = new Note("ominous lore", true,
                "You read the page and it says 'This page intentionally left blank.'");
        historyHall.addItem(loreNote);

        RustyDagger dagger = new RustyDagger(true);

        historyHall.addItem(dagger);
        // Test dispenser start - could be used for other items
        Dispenser dispenser = new Dispenser("note dispenser", true, false,
                "It looks like a mailbox with a big lever.  Something probably comes out of that slot.");
        Note generatedNote = new Note("note", true, "This is a autogenerated note.");
        dispenser.setItem("room", historyHall);
        dispenser.setItem("disp", dispenser);
        dispenser.setItem("item", generatedNote);
        dispenser.setItem("message", "A note fell out of the dispenser.");
        InteractAction testAction2 = new DispenserAction();
        dispenser.setAction(testAction2);
        // Test dispenser end
        historyHall.addItem(dispenser);

        // RM3
        Room offeringRoom = new Room("Offering Room", "This is the offering room.");

        // RM4
        Room trappedHall = new Room("Trapped Room", "This is the trapped room.");
        HealPotion h1 = new HealPotion(true);
        trappedHall.addItem(h1);

        // RM5
        Room statueRoom = new Room("Statue Room", "This is the statue room.");

        Note bossNote = new Note("note from boss", true, "The tutorial boss is on vacation right now.");
        statueRoom.addItem(bossNote);

        Room secretRoom = new Room("Secret Room", "This is the secret room!");

        MantleOfDeath mantle = new MantleOfDeath(false);
        ReaperScythe scythe = new ReaperScythe(false);

        HealPotion healPotion = new HealPotion(true);
        secretRoom.addItem(healPotion);
        secretRoom.addItem(mantle);
        secretRoom.addItem(scythe);

        Switch statue = new Switch("golden statue", true, true,
                "The statue has a start to a riddle, but it looks like it hasn't been finished yet.");
        statue.setItem("room1", statueRoom);
        statue.setItem("room2", secretRoom);
        InteractAction statueAction = (player, triggerObject, args) -> {
            Object o1 = args.get("room1");
            if (!(o1 instanceof Room)) {
                Logger.getLogger(triggerObject.getClassName()).warning("Origin Room not found");
                return new InteractOutMessage(triggerObject, InteractOutMessageType.ERROR);
            }
            Room r1 = (Room) o1;

            Object o2 = args.get("room2");
            if (!(o2 instanceof Room)) {
                Logger.getLogger(triggerObject.getClassName()).warning("Destination Room not found");
                return new InteractOutMessage(triggerObject, InteractOutMessageType.ERROR);
            }
            Room r2 = (Room) o2;

            r1.removeCreature(player);
            r2.addCreature(player);
            return new InteractOutMessage(triggerObject,
                    "The statue glows and you black out for a second. You find yourself in another room.");
        };
        statue.setAction(statueAction);
        statueRoom.addItem(statue);

        // RM6 The armory
        Room armory = new Room("Armory", "An armory");
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

        // RM7
        Room passage = new Room("Passageway", "An old dusty passageway");
        // RM8
        Room treasury = new Room("Vault", "A looted vault room");
        HealPotion regular = new HealPotion(true);
        HealPotion greater = new HealPotion(HealType.Greater);
        HealPotion critical = new HealPotion(HealType.Critical);

        treasury.addItem(regular);
        treasury.addItem(greater);
        treasury.addItem(critical);

        // Monsters
        Monster g1 = new Monster("goblin", goblin);
        g1.setConvoTree(convoLoader, "non_verbal_default");
        historyHall.addCreature(g1);

        Monster boss = new Monster("Boss Bear", bugbear);
        statueRoom.addCreature(boss);

        Monster rightHandMan = new Monster("Right", hobgoblin);
        offeringRoom.addCreature(rightHandMan);

        // Set starting room
        builder.addStartingRoom(entryRoom);

        // Path
        builder.connectRoom(entryRoom, Directions.WEST, historyHall);
        builder.connectRoom(historyHall, Directions.WEST, offeringRoom);
        builder.connectRoom(historyHall, Directions.NORTH, armory);
        builder.connectRoom(offeringRoom, Directions.WEST, trappedHall);
        builder.connectRoom(armory, Directions.WEST, passage);
        builder.connectRoom(passage, Directions.SOUTH, treasury);
        builder.connectRoom(treasury, Directions.SOUTH, trappedHall);
        builder.connectRoom(trappedHall, Directions.SOUTH, statueRoom);
        builder.connectRoomOneWay(statueRoom, Directions.WEST, secretRoom);

        return builder.build();
    }

    public static Dungeon buildDynamicDungeon(int seed) {

        return null;
    }
}
