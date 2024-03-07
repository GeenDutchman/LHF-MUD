package com.lhf.game.lewd;

import java.util.function.Consumer;

import com.lhf.game.creature.CreatureFactory;
import com.lhf.game.creature.CreaturePartitionSetVisitor;
import com.lhf.game.creature.NameGenerator;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.Player.PlayerBuildInfo;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.map.Area;
import com.lhf.game.map.AreaVisitor;
import com.lhf.game.map.DMRoom;
import com.lhf.game.map.Room;
import com.lhf.server.client.user.User;

public class LewdBabyMaker extends LewdProduct {

    private final PlayerBuildInfo playerBuildInfo;

    public LewdBabyMaker(PlayerBuildInfo playerBuildInfo) {
        this.playerBuildInfo = playerBuildInfo;
    }

    private PlayerBuildInfo getPlayerBuildInfoCopy(User user) {
        return this.playerBuildInfo != null ? new PlayerBuildInfo(this.playerBuildInfo).setUser(user)
                : Player.getPlayerBuilder(user);
    }

    @Override
    public Consumer<Area> onLewdAreaChanges(VrijPartij party) {
        if (party == null) {
            return null;
        }
        return new AreaVisitor() {

            private void addCorpses(Area area) {
                for (String name : party.getNames()) {
                    if (name.length() <= 0) {
                        name = NameGenerator.Generate(null);
                    }
                    Corpse body = new Corpse(name);
                    area.addItem(body);
                }
            }

            @Override
            public void visit(Room room) {
                if (room == null) {
                    return;
                }
                this.addCorpses(room);
            }

            @Override
            public void visit(DMRoom room) {
                if (room == null) {
                    return;
                }
                CreaturePartitionSetVisitor partitions = new CreaturePartitionSetVisitor();
                partitions.forEach(party.getParticipants());
                if (partitions.getDungeonMasters().size() != party.size()) {
                    this.addCorpses(room);
                    return;
                }
                CreatureFactory factory = new CreatureFactory();
                for (String name : party.getNames()) {
                    final User user = room.removeUser(name);
                    if (user == null) {
                        Corpse body = new Corpse(
                                name == null || name.length() <= 0 ? NameGenerator.Generate(null) : name);
                        room.addItem(body);
                        continue;
                    }
                    final PlayerBuildInfo buildInfo = LewdBabyMaker.this.getPlayerBuildInfoCopy(user);
                    final Player player = factory.buildPlayer(buildInfo);
                    room.addNewPlayer(player);
                }
            }

        };
    }

}
