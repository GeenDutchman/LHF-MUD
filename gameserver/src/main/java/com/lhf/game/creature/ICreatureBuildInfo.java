package com.lhf.game.creature;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.concrete.Corpse;

public interface ICreatureBuildInfo extends Serializable {
    public final static class CreatureBuilderID implements Comparable<ICreatureBuildInfo.CreatureBuilderID> {
        private final UUID id = UUID.randomUUID();

        public UUID getId() {
            return id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof ICreatureBuildInfo.CreatureBuilderID))
                return false;
            ICreatureBuildInfo.CreatureBuilderID other = (ICreatureBuildInfo.CreatureBuilderID) obj;
            return Objects.equals(id, other.id);
        }

        @Override
        public int compareTo(ICreatureBuildInfo.CreatureBuilderID arg0) {
            return this.id.compareTo(arg0.id);
        }

    }

    public String getClassName();

    public ICreatureBuildInfo.CreatureBuilderID getCreatureBuilderID();

    public String getName();

    public CreatureFaction getFaction();

    public VocationName getVocation();

    public Integer getVocationLevel();

    public String getStatblockName();

    public Statblock getStatblock();

    public Corpse getCorpse();

    public void acceptBuildInfoVisitor(ICreatureBuildInfoVisitor visitor);
}