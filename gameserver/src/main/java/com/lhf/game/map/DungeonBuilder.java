package com.lhf.game.map;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.creature.Monster;
import com.lhf.game.creature.NameGenerator;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.enums.HealType;
import com.lhf.game.item.DispenserAction;
import com.lhf.game.item.concrete.Chest;
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
import com.lhf.game.map.Land.AreaDirectionalLinks;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.out.ItemInteractionEvent;
import com.lhf.messages.out.ItemInteractionEvent.InteractOutMessageType;

public class DungeonBuilder implements Land.LandBuilder {
    private class RoomAndDirs implements Land.AreaDirectionalLinks {
        public final Area room;
        public Map<Directions, Doorway> exits;

        RoomAndDirs(Area room) {
            this.room = room;
            this.exits = new TreeMap<>();
        }

        @Override
        public Area getArea() {
            return this.room;
        }

        @Override
        public Map<Directions, Doorway> getExits() {
            return this.exits;
        }
    }

    private Logger logger;
    private Map<UUID, Land.AreaDirectionalLinks> mapping;
    private Room startingRoom = null;
    private MessageChainHandler successor = null;
    private List<Room> orderAdded;

    public static DungeonBuilder newInstance() {
        return new DungeonBuilder();
    }

    private DungeonBuilder() {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.mapping = new HashMap<>();
        this.orderAdded = new ArrayList<>();
    }

    public DungeonBuilder addStartingRoom(Room startingRoom) {
        this.startingRoom = startingRoom;
        this.mapping.putIfAbsent(this.startingRoom.getUuid(), new RoomAndDirs(startingRoom));
        this.orderAdded.add(startingRoom);
        return this;
    }

    public DungeonBuilder setSuccessor(MessageChainHandler successor) {
        this.successor = successor;
        return this;
    }

    public DungeonBuilder connectRoom(DoorwayType type, Room toAdd, Directions toExistingRoom, Room existing) {
        assert this.mapping.containsKey(existing.getUuid()) : existing.getName() + " not yet added";
        Directions toNewRoom = toExistingRoom.opposite();
        this.mapping.putIfAbsent(toAdd.getUuid(), new RoomAndDirs(toAdd));
        Map<Directions, Doorway> toAddExits = this.mapping.get(toAdd.getUuid()).getExits();
        assert !toAddExits.containsKey(toExistingRoom)
                : toAdd.getName() + " already has direction " + toExistingRoom.toString();
        Doorway doorway = DoorwayFactory.createDoorway(type, toAdd, toExistingRoom, existing);
        toAddExits.put(toExistingRoom, doorway);
        Map<Directions, Doorway> existingExits = this.mapping.get(existing.getUuid()).getExits();
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
        Map<Directions, Doorway> secretExits = this.mapping.get(secretRoom.getUuid()).getExits();
        assert !secretExits.containsKey(toExistingRoom)
                : secretRoom.getName() + " already has direction " + toExistingRoom.toString();
        Doorway oneWayDoor = DoorwayFactory.createDoorway(DoorwayType.ONE_WAY, secretRoom, toExistingRoom, existing);
        secretExits.put(toExistingRoom, oneWayDoor);
        this.orderAdded.add(secretRoom);
        return this;
    }

    @Override
    public Dungeon build() {
        this.logger.entering(this.getClass().getName(), "build()");
        Dungeon dungeon = new Dungeon(this);
        return dungeon;
    }

    public static Dungeon buildStaticDungeon(MessageChainHandler successor, AIRunner aiRunner)
            throws FileNotFoundException {
        DungeonBuilder builder = DungeonBuilder.newInstance();
        if (aiRunner == null) {
            builder.logger.log(Level.SEVERE, "AIRunner NOT provided!");
        }

        builder.setSuccessor(successor);

        ConversationManager convoLoader = new ConversationManager();
        StatblockManager loader = new StatblockManager();
        Statblock goblin = loader.statblockFromfile("goblin");
        Statblock bugbear = loader.statblockFromfile("bugbear");
        Statblock hobgoblin = loader.statblockFromfile("hobgoblin");

        // Entry Room RM1
        Room.RoomBuilder entryRoomBuilder = Room.RoomBuilder.getInstance();
        entryRoomBuilder.setName("Entry Room").setDescription("This is the entry room.");

        Note addNote = new Note("interact note", true, "This note is to test the switch action.");

        // Switch test start
        Switch testSwitch = new Switch("test switch", true, false, "This looks like a test switch.");
        // Set items the action is going to use
        testSwitch.setItem("note", addNote);
        // Create action as anonymous function
        InteractAction testAction = (creature, triggerObject, args) -> {
            // You can do anything you imagine inside, just with casting overhead (for now)
            // This can be used for the secret room trigger, since a switch can be hidden
            ItemInteractionEvent.Builder interactOutMessage = ItemInteractionEvent.getBuilder()
                    .setTaggable(triggerObject);
            Object o1 = args.get("note");
            if (!(o1 instanceof Note)) {
                Logger.getLogger(triggerObject.getClassName()).warning("Note not found");
                return interactOutMessage.setSubType(InteractOutMessageType.ERROR).Build();
            }
            Note n = (Note) o1;
            Object o2 = args.get("room");
            if (!(o2 instanceof Room)) {
                Logger.getLogger(triggerObject.getClassName()).warning("Room not found");
                return interactOutMessage.setSubType(InteractOutMessageType.ERROR).Build();
            }
            Room r = (Room) o2;
            r.addItem(n);
            return interactOutMessage.setSubType(InteractOutMessageType.PERFORMED)
                    .setDescription("Switch activated. A note dropped from the ceiling.")
                    .setPerformed().Build();
        };
        // Set Action
        testSwitch.setAction(testAction);
        // Switch test end
        entryRoomBuilder.addItem(testSwitch);
        Room entryRoom = entryRoomBuilder.build();
        testSwitch.setItem("room", entryRoom); // TODO: find a better way for context aware items

        // History Hall RM2
        Room.RoomBuilder historyHallBuilder = Room.RoomBuilder.getInstance();
        historyHallBuilder.setName("History Hall").setDescription("This is the history hall.");
        Note loreNote = new Note("ominous lore", true,
                "You read the page and it says 'This page intentionally left blank.'");
        historyHallBuilder.addItem(loreNote);

        RustyDagger dagger = new RustyDagger(true);

        historyHallBuilder.addItem(dagger);
        // Test dispenser start - could be used for other items
        Dispenser dispenser = new Dispenser("note dispenser", true, false,
                "It looks like a mailbox with a big lever.  Something probably comes out of that slot.");
        Note generatedNote = new Note("note", true, "This is a autogenerated note.");
        dispenser.setItem("disp", dispenser);
        dispenser.setItem("item", generatedNote);
        dispenser.setItem("message", "A note fell out of the dispenser.");
        InteractAction testAction2 = new DispenserAction();
        dispenser.setAction(testAction2);
        // Test dispenser end
        historyHallBuilder.addItem(dispenser);
        Room historyHall = historyHallBuilder.build();
        dispenser.setItem("room", historyHall);

        // RM3
        Room.RoomBuilder offeringRoomBuilder = Room.RoomBuilder.getInstance().setName("Offering Room")
                .setDescription("This is the offering room.");
        Room offeringRoom = offeringRoomBuilder.build();

        // RM4
        Room.RoomBuilder trappedHallBuilder = Room.RoomBuilder.getInstance().setName("Trapped Room")
                .setDescription("This is the trapped room.");
        HealPotion h1 = new HealPotion(true);
        trappedHallBuilder.addItem(h1);
        Room trappedHall = trappedHallBuilder.build();

        Room.RoomBuilder secretRoomBuilder = Room.RoomBuilder.getInstance().setName("Secret Room")
                .setDescription("This is the secret room!");

        MantleOfDeath mantle = new MantleOfDeath(false);
        ReaperScythe scythe = new ReaperScythe(false);

        HealPotion healPotion = new HealPotion(true);
        secretRoomBuilder.addItem(healPotion);
        secretRoomBuilder.addItem(mantle);
        secretRoomBuilder.addItem(scythe);
        Room secretRoom = secretRoomBuilder.build();

        // RM5
        Room.RoomBuilder statueRoomBuilder = Room.RoomBuilder.getInstance().setName("Statue Room")
                .setDescription("This is the statue room.");
        Note bossNote = new Note("note from boss", true, "The tutorial boss is on vacation right now.");
        statueRoomBuilder.addItem(bossNote);

        Switch statue = new Switch("golden statue", true, true,
                "The statue has a start to a riddle, but it looks like it hasn't been finished yet.");
        InteractAction statueAction = (player, triggerObject, args) -> {
            ItemInteractionEvent.Builder interactOutMessage = ItemInteractionEvent.getBuilder()
                    .setTaggable(triggerObject);
            Object o1 = args.get("room1");
            if (!(o1 instanceof Room)) {
                Logger.getLogger(triggerObject.getClassName()).warning("Origin Room not found");
                return interactOutMessage.setSubType(InteractOutMessageType.ERROR).Build();
            }
            Room r1 = (Room) o1;

            Object o2 = args.get("room2");
            if (!(o2 instanceof Room)) {
                Logger.getLogger(triggerObject.getClassName()).warning("Destination Room not found");
                return interactOutMessage.setSubType(InteractOutMessageType.ERROR).Build();
            }
            Room r2 = (Room) o2;

            r1.removeCreature(player);
            r2.addCreature(player);
            return interactOutMessage
                    .setSubType(InteractOutMessageType.PERFORMED)
                    .setDescription(
                            "The statue glows and you black out for a second. You find yourself in another room.")
                    .Build();
        };
        statue.setAction(statueAction);
        statueRoomBuilder.addItem(statue);

        Room statueRoom = statueRoomBuilder.build();
        statue.setItem("room1", statueRoom);
        statue.setItem("room2", secretRoom);

        // RM6 The armory
        Room.RoomBuilder armoryBuilder = Room.RoomBuilder.getInstance().setName("Armory").setDescription("An armory");
        CarnivorousArmor mimic = new CarnivorousArmor(true);
        ChainMail mail = new ChainMail(true);
        Whimsystick stick = new Whimsystick(true);
        Shortsword shortsword = new Shortsword(true);
        HealPotion potion = new HealPotion(true);
        armoryBuilder.addItem(mimic);
        armoryBuilder.addItem(mail);
        armoryBuilder.addItem(stick);
        armoryBuilder.addItem(shortsword);
        armoryBuilder.addItem(potion);
        Room armory = armoryBuilder.build();

        // RM7
        Room passage = Room.RoomBuilder.getInstance().setName("Passageway")
                .setDescription("An old, curvy and dusty passageway").build();
        // RM8
        Room.RoomBuilder treasuryBuilder = Room.RoomBuilder.getInstance().setName("Vault")
                .setDescription("A looted vault room.");
        HealPotion regular = new HealPotion(true);
        HealPotion greater = new HealPotion(HealType.Greater);
        HealPotion critical = new HealPotion(HealType.Critical);

        treasuryBuilder.addItem(regular);
        treasuryBuilder.addItem(greater);
        treasuryBuilder.addItem(critical);
        for (Chest.ChestDescriptor descriptor : Chest.ChestDescriptor.values()) { // it's "looted", so...
            treasuryBuilder.addItem(new Chest(descriptor, true, false, true));
        }
        Room treasury = treasuryBuilder.build();

        // Monsters
        Monster.MonsterBuilder g1 = Monster.getMonsterBuilder(aiRunner).setName(NameGenerator.Generate("goblin"))
                .setStatblock(goblin);
        g1.setConversationTree(convoLoader, "non_verbal_default");
        historyHall.addCreature(g1.build());

        Monster.MonsterBuilder boss = Monster.getMonsterBuilder(aiRunner).setName("Boss Bear").setStatblock(bugbear);
        statueRoom.addCreature(boss.build());

        Monster.MonsterBuilder rightHandMan = Monster.getMonsterBuilder(aiRunner)
                .setName(NameGenerator.Generate("Right")).setStatblock(hobgoblin);
        offeringRoom.addCreature(rightHandMan.build());

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

    public static Dungeon buildDynamicDungeon(int seed, AIRunner aiRunner) {

        return null;
    }

    @Override
    public Area getStartingArea() {
        return this.startingRoom;
    }

    @Override
    public Map<UUID, AreaDirectionalLinks> getAtlas() {
        return this.mapping;
    }

    @Override
    public MessageChainHandler getSuccessor() {
        return this.successor;
    }
}
