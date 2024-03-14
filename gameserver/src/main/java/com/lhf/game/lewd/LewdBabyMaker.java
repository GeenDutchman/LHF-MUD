package com.lhf.game.lewd;

import java.util.Collection;
import java.util.function.Consumer;

import com.lhf.game.creature.CreatureBuildInfoPartitionSetVisitor;
import com.lhf.game.creature.CreatureFactory;
import com.lhf.game.creature.CreaturePartitionSetVisitor;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo;
import com.lhf.game.creature.NameGenerator;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.Player.PlayerBuildInfo;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.map.Area;
import com.lhf.game.map.AreaVisitor;
import com.lhf.game.map.DMRoom;
import com.lhf.game.map.Room;
import com.lhf.messages.in.LewdInMessage.NameVocationPair;
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
                for (NameVocationPair pairing : party.getNames()) {
                    String name = null;
                    if (pairing != null) {
                        name = pairing.name;
                    }
                    if (name == null || name.length() <= 0) {
                        name = NameGenerator.Generate(null);
                    }
                    Corpse body = new Corpse(name);
                    area.addItem(body);
                }
            }

            private void buildCreatures(final Area area, final Collection<INonPlayerCharacterBuildInfo> toBuild,
                    CreatureFactory factory) {
                if (area == null || toBuild == null) {
                    return;
                }
                if (factory == null) {
                    factory = new CreatureFactory();
                }
                factory.forEach(toBuild);
                for (final INonPlayerCharacter npc : factory.getBuiltCreatures().getINpcs()) {
                    area.addCreature(npc);
                }
            }

            @Override
            public void visit(Room room) {
                if (room == null) {
                    return;
                }
                this.addCorpses(room);
                CreatureBuildInfoPartitionSetVisitor partitionSetVisitor = new CreatureBuildInfoPartitionSetVisitor();
                partitionSetVisitor.forEach(party.getBuildInfos());
                this.buildCreatures(room, partitionSetVisitor.getINonPlayerCharacterBuildInfos(), null);
            }

            @Override
            public void visit(DMRoom room) {
                if (room == null) {
                    return;
                }

                CreatureFactory factory = new CreatureFactory();
                CreatureBuildInfoPartitionSetVisitor builderPartitions = new CreatureBuildInfoPartitionSetVisitor();
                builderPartitions.forEach(party.getBuildInfos());
                this.buildCreatures(room, builderPartitions.getINonPlayerCharacterBuildInfos(), factory);

                CreaturePartitionSetVisitor creaturePartitions = new CreaturePartitionSetVisitor();
                creaturePartitions.forEach(party.getParticipants());
                if (creaturePartitions.getDungeonMasters().size() != party.size()) {
                    this.addCorpses(room);
                    return;
                }

                for (NameVocationPair pairing : party.getNames()) {
                    final User user = room.removeUser(pairing.name);
                    if (user == null) {
                        Corpse body = new Corpse(
                                pairing.name == null || pairing.name.length() <= 0 ? NameGenerator.Generate(null)
                                        : pairing.name);
                        room.addItem(body);
                        continue;
                    }
                    final PlayerBuildInfo buildInfo = LewdBabyMaker.this.getPlayerBuildInfoCopy(user);
                    final Player player = factory.buildPlayer(buildInfo);
                    room.addNewPlayer(player);
                }

                for (Player.PlayerBuildInfo buildInfo : builderPartitions.getPlayerBuildInfos()) {
                    final User user = room.removeUser(buildInfo.getRawName());
                    if (user == null) {
                        String name = buildInfo.getName();
                        Corpse body = new Corpse(
                                name == null || name.length() <= 0 ? NameGenerator.Generate(null) : name);
                        room.addItem(body);
                        continue;
                    }
                    final Player player = factory.buildPlayer(buildInfo.setUser(user));
                    room.addNewPlayer(player);
                }
            }

        };
    }

}
