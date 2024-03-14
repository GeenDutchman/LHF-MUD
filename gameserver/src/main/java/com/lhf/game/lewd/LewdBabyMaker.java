package com.lhf.game.lewd;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.function.Consumer;

import com.lhf.game.ItemContainer;
import com.lhf.game.creature.CreatureBuildInfo;
import com.lhf.game.creature.CreatureBuildInfoPartitionSetVisitor;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureFactory;
import com.lhf.game.creature.CreaturePartitionSetVisitor;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo;
import com.lhf.game.creature.NameGenerator;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.Player.PlayerBuildInfo;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.vocation.VocationFactory;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Equipable;
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
                for (CreatureBuildInfo pairing : party.getTemplateBuildInfos()) {
                    String name = null;
                    if (pairing != null) {
                        name = pairing.getName();
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
                partitionSetVisitor.forEach(party.getFullBuildInfos());
                this.buildCreatures(room, partitionSetVisitor.getINonPlayerCharacterBuildInfos(), null);
            }

            private void directedOverwrite(PlayerBuildInfo player, CreatureBuildInfo template) {
                if (player == null || template == null) {
                    return;
                }
                final VocationName name = template.getVocation();
                if (name != null) {
                    player.setVocation(VocationFactory.getVocation(name));
                }
                final Inventory inventory = template.getInventory();
                if (inventory != null && !inventory.isEmpty()) {
                    ItemContainer.transfer(inventory, player.getInventory(), null, false);
                }
                final EnumMap<EquipmentSlots, Equipable> slots = template.getEquipmentSlots();
                if (slots != null && !slots.isEmpty()) {
                    for (final Entry<EquipmentSlots, Equipable> entry : slots.entrySet()) {
                        player.addEquipment(entry.getKey(), entry.getValue(), false);
                    }
                }
                final NavigableSet<CreatureEffect> effects = template.getCreatureEffects();
                final Set<CreatureEffect> alreadyEffects = player.getCreatureEffects();
                if (effects != null && !effects.isEmpty()) {
                    for (final CreatureEffect effect : effects) {
                        if (effect == null) {
                            continue;
                        }
                        CreatureEffect composed = new CreatureEffect(effect.getSource(), null, effect.getGeneratedBy());
                        if (alreadyEffects.contains(composed)) {
                            continue;
                        }
                        player.applyEffect(composed);
                    }
                }

            }

            @Override
            public void visit(DMRoom room) {
                if (room == null) {
                    return;
                }

                CreatureFactory factory = new CreatureFactory();
                CreatureBuildInfoPartitionSetVisitor builderPartitions = new CreatureBuildInfoPartitionSetVisitor();
                builderPartitions.forEach(party.getFullBuildInfos());
                this.buildCreatures(room, builderPartitions.getINonPlayerCharacterBuildInfos(), factory);

                CreaturePartitionSetVisitor creaturePartitions = new CreaturePartitionSetVisitor();
                creaturePartitions.forEach(party.getParticipants());
                if (creaturePartitions.getDungeonMasters().size() != party.size()) {
                    this.addCorpses(room);
                    return;
                }

                for (CreatureBuildInfo pairing : party.getTemplateBuildInfos()) {
                    String name = null;
                    if (pairing != null) {
                        name = pairing.getName();
                    }
                    final User user = room.removeUser(name);
                    if (user == null) {
                        Corpse body = new Corpse(
                                name == null || name.length() <= 0 ? NameGenerator.Generate(null)
                                        : name);
                        room.addItem(body);
                        continue;
                    }
                    final PlayerBuildInfo buildInfo = LewdBabyMaker.this.getPlayerBuildInfoCopy(user);
                    this.directedOverwrite(buildInfo, pairing);
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
