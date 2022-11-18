package com.lhf.game.map;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

import com.lhf.game.creature.Monster;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.GroupAIRunner;
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
import com.lhf.game.map.DoorwayFactory.DoorwayType;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.InteractOutMessage;
import com.lhf.messages.out.InteractOutMessage.InteractOutMessageType;

public class DungeonBuilder {
    private class RoomAndDirs {
        public final Room room;
        public Map<Directions, Doorway> exits;

        RoomAndDirs(Room room) {
            this.room = room;
            this.exits = new TreeMap<>();
        }
    }

    private Map<UUID, RoomAndDirs> mapping;
    private Room startingRoom = null;
    private MessageHandler successor;
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
        this.mapping.putIfAbsent(this.startingRoom.getUuid(), new RoomAndDirs(startingRoom));
        this.orderAdded.add(startingRoom);
        return this;
    }

    public DungeonBuilder setSuccessor(MessageHandler successor) {
        this.successor = successor;
        return this;
    }

    public DungeonBuilder connectRoom(DoorwayType type, Room toAdd, Directions toExistingRoom, Room existing) {
        assert this.mapping.containsKey(existing.getUuid()) : existing.getName() + " not yet added";
        Directions toNewRoom = toExistingRoom.opposite();
        this.mapping.putIfAbsent(toAdd.getUuid(), new RoomAndDirs(toAdd));
        Map<Directions, Doorway> toAddExits = this.mapping.get(toAdd.getUuid()).exits;
        assert !toAddExits.containsKey(toExistingRoom)
                : toAdd.getName() + " already has direction " + toExistingRoom.toString();
        Doorway doorway = DoorwayFactory.createDoorway(type, toAdd, toExistingRoom, existing);
        toAddExits.put(toExistingRoom, doorway);
        Map<Directions, Doorway> existingExits = this.mapping.get(existing.getUuid()).exits;
        assert !existingExits.containsKey(toNewRoom)
                : existing.getName() + " already has direction " + toNewRoom.toString();
        existingExits.put(toNewRoom, doorway);
        this.orderAdded.add(toAdd);
        return this;
    }

    public DungeonBuilder connectRoom(Room toAdd, Directions toExistingRoom, Room existing) {
        return this.connectRoom(DoorwayType.STANDARD, toAdd, toExistingRoom, existing);
    }

    public DungeonBuilder connectRoomOneWay(Room secretRoom, Directions toExistingRoom, Room existing) {
        assert this.mapping.containsKey(existing.getUuid()) : existing.getName() + " not yet added";
        this.mapping.putIfAbsent(secretRoom.getUuid(), new RoomAndDirs(secretRoom));
        Map<Directions, Doorway> secretExits = this.mapping.get(secretRoom.getUuid()).exits;
        assert !secretExits.containsKey(toExistingRoom)
                : secretRoom.getName() + " already has direction " + toExistingRoom.toString();
        Doorway oneWayDoor = DoorwayFactory.createDoorway(DoorwayType.ONE_WAY, secretRoom, toExistingRoom, existing);
        secretExits.put(toExistingRoom, oneWayDoor);
        this.orderAdded.add(secretRoom);
        return this;
    }

    public Dungeon build() {
        Dungeon dungeon = new Dungeon(this.successor);
        System.out.printf("Adding starting room %s\r\n", this.startingRoom.getName());
        dungeon.setStartingRoom(this.startingRoom);
        for (Room existing : this.orderAdded) {
            Map<Directions, Doorway> existingExits = this.mapping.get(existing.getUuid()).exits;
            for (Directions exitDirection : existingExits.keySet()) {
                Doorway door = existingExits.get(exitDirection);
                UUID nextRoomUuid = door.getRoomAccross(existing.getUuid());
                Room nextRoom = this.mapping.get(nextRoomUuid).room;
                Map<Directions, Doorway> nextExits = this.mapping.get(nextRoomUuid).exits;
                if (nextExits.containsKey(exitDirection.opposite())) {
                    System.out.printf("%s go %s to room %s\r\n", nextRoom.getName(),
                            exitDirection.opposite().toString(),
                            existing.getName());
                    dungeon.connectRoom(door.getType(), nextRoom, exitDirection.opposite(), existing);
                } else {
                    System.out.printf("hidden %s go %s to room %s, but not back\r\n", existing.getName(),
                            exitDirection.toString(), nextRoom.getName());
                    dungeon.connectRoomExclusiveOneWay(existing, exitDirection, nextRoom);
                }
            }
        }
        return dungeon;
    }

    public static Dungeon buildStaticDungeon(MessageHandler successor, GroupAIRunner aiRunner)
            throws FileNotFoundException {
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
        Room passage = new Room("Passageway", "An old, curvy and dusty passageway");
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
        aiRunner.register(g1);
        g1.setConvoTree(convoLoader, "non_verbal_default");
        historyHall.addCreature(g1);

        Monster boss = new Monster("Boss Bear", bugbear);
        aiRunner.register(boss);
        statueRoom.addCreature(boss);

        Monster rightHandMan = new Monster("Right", hobgoblin);
        aiRunner.register(rightHandMan);
        offeringRoom.addCreature(rightHandMan);

        // Set starting room
        builder.addStartingRoom(entryRoom);

        // Path
        builder.connectRoom(historyHall, Directions.WEST, entryRoom);
        builder.connectRoom(offeringRoom, Directions.WEST, historyHall);
        builder.connectRoom(armory, Directions.SOUTH, historyHall);
        builder.connectRoom(trappedHall, Directions.WEST, offeringRoom);
        builder.connectRoom(passage, Directions.SOUTH, armory);
        builder.connectRoom(treasury, Directions.WEST, passage);
        builder.connectRoom(trappedHall, Directions.NORTH, treasury);
        builder.connectRoom(statueRoom, Directions.NORTH, trappedHall);
        builder.connectRoomOneWay(secretRoom, Directions.WEST, statueRoom);

        return builder.build();
    }

    public static Dungeon buildDynamicDungeon(int seed, GroupAIRunner aiRunner) {

        return null;
    }
}
