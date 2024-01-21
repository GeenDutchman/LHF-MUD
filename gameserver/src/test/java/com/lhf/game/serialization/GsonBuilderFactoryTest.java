package com.lhf.game.serialization;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.lhf.game.creature.Monster;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.item.Takeable;
import com.lhf.game.item.concrete.equipment.CarnivorousArmor;
import com.lhf.game.item.concrete.equipment.Longsword;
import com.lhf.game.map.DMRoom;
import com.lhf.game.map.DMRoom.DMRoomBuilder;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.Dungeon.DungeonBuilder;
import com.lhf.game.map.Room;
import com.lhf.game.map.Room.RoomBuilder;

public class GsonBuilderFactoryTest {
    @Test
    void testBuild() throws FileNotFoundException {
        Inventory georgeInventory = new Inventory();
        georgeInventory.addItem(new Longsword());
        georgeInventory.addItem(new Takeable("Sharkbait"));
        Statblock georgeblock = new Statblock.StatblockBuilder().setInventory(georgeInventory).build();
        Monster.MonsterBuilder builder = Monster.getMonsterBuilder().setName("George").setStatblock(georgeblock);

        RoomBuilder roomBuilder = Room.RoomBuilder.getInstance().addItem(new CarnivorousArmor()).addNPCBuilder(builder);

        DungeonBuilder dungeon = Dungeon.DungeonBuilder.newInstance().addStartingRoom(roomBuilder);

        DMRoomBuilder dmRoom = DMRoom.DMRoomBuilder.buildDefault(null, null, null).addLandBuilder(dungeon);

        GsonBuilderFactory gbf = new GsonBuilderFactory().conversation().items().prettyPrinting();

        Gson gson = gbf.build();

        System.out.println(gson.toJson(dmRoom));

    }
}
