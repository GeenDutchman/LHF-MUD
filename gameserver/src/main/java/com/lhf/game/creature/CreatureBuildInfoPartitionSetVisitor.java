package com.lhf.game.creature;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lhf.game.creature.DungeonMaster.DungeonMasterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo;
import com.lhf.game.creature.Player.PlayerBuildInfo;

public class CreatureBuildInfoPartitionSetVisitor implements ICreatureBuildInfoVisitor {
    private final NavigableSet<PlayerBuildInfo> playerBuildInfos = new TreeSet<>();
    private final NavigableSet<MonsterBuildInfo> monsterBuildInfos = new TreeSet<>();
    private final NavigableSet<INPCBuildInfo> npcBuildInfos = new TreeSet<>();
    private final NavigableSet<DungeonMasterBuildInfo> dungeonMasterBuildInfos = new TreeSet<>();
    private final NavigableSet<CreatureBuildInfo> creatureBuildInfos = new TreeSet<>();

    @Override
    public void visit(PlayerBuildInfo buildInfo) {
        if (buildInfo == null) {
            return;
        }
        this.playerBuildInfos.add(buildInfo);
    }

    @Override
    public void visit(MonsterBuildInfo buildInfo) {
        if (buildInfo == null) {
            return;
        }
        this.monsterBuildInfos.add(buildInfo);
    }

    @Override
    public void visit(INPCBuildInfo buildInfo) {
        if (buildInfo == null) {
            return;
        }
        this.npcBuildInfos.add(buildInfo);
    }

    @Override
    public void visit(DungeonMasterBuildInfo buildInfo) {
        if (buildInfo == null) {
            return;
        }
        this.dungeonMasterBuildInfos.add(buildInfo);
    }

    @Override
    public void visit(CreatureBuildInfo buildInfo) {
        if (buildInfo == null) {
            return;
        }
        this.creatureBuildInfos.add(buildInfo);
    }

    /**
     * @deprecated Contains raw and unbuildable
     *             {@link com.lhf.game.creature.CreatureBuildInfo CreatureBuildInfo}
     * @return everything collected by this visitor
     */
    @Deprecated(forRemoval = false)
    public NavigableSet<ICreatureBuildInfo> getICreatureBuildInfos() {
        return Collections.unmodifiableNavigableSet(
                Stream.concat(this.getBuildables().stream(), this.getCreatureBuildInfos().stream())
                        .collect(Collectors.toCollection(() -> new TreeSet<>())));
    }

    public NavigableSet<ICreatureBuildInfo> getBuildables() {
        return Collections.unmodifiableNavigableSet(
                Stream.concat(this.getPlayerBuildInfos().stream(), this.getINonPlayerCharacterBuildInfos().stream())
                        .collect(Collectors.toCollection(() -> new TreeSet<>())));
    }

    public NavigableSet<PlayerBuildInfo> getPlayerBuildInfos() {
        return Collections.unmodifiableNavigableSet(playerBuildInfos);
    }

    public NavigableSet<INonPlayerCharacterBuildInfo> getINonPlayerCharacterBuildInfos() {
        return Collections.unmodifiableNavigableSet(
                Stream.concat(this.getMonsterBuildInfos().stream(), this.getDMsAndNPCs().stream())
                        .collect(Collectors.toCollection(() -> new TreeSet<>())));
    }

    public NavigableSet<MonsterBuildInfo> getMonsterBuildInfos() {
        return Collections.unmodifiableNavigableSet(monsterBuildInfos);
    }

    public NavigableSet<INonPlayerCharacterBuildInfo> getDMsAndNPCs() {
        return Collections.unmodifiableNavigableSet(
                Stream.concat(this.getDungeonMasterBuildInfos().stream(), this.getNpcBuildInfos().stream())
                        .collect(Collectors.toCollection(() -> new TreeSet<>())));
    }

    public NavigableSet<INPCBuildInfo> getNpcBuildInfos() {
        return Collections.unmodifiableNavigableSet(npcBuildInfos);
    }

    public NavigableSet<DungeonMasterBuildInfo> getDungeonMasterBuildInfos() {
        return Collections.unmodifiableNavigableSet(dungeonMasterBuildInfos);
    }

    /**
     * @deprecated Contains raw and unbuildable
     *             {@link com.lhf.game.creature.CreatureBuildInfo CreatureBuildInfo}
     * @return partitioned NavigableSet<CreatureBuildInfo>
     */
    @Deprecated(forRemoval = false)
    public NavigableSet<CreatureBuildInfo> getCreatureBuildInfos() {
        return Collections.unmodifiableNavigableSet(creatureBuildInfos);
    }

}
