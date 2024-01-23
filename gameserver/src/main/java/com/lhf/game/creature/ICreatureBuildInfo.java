package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.server.interfaces.NotNull;

public interface ICreatureBuildInfo extends Serializable {
    public final static class CreatureBuilderID implements Comparable<ICreatureBuildInfo.CreatureBuilderID> {
        private final UUID id;

        public CreatureBuilderID() {
            this.id = UUID.randomUUID();
        }

        protected CreatureBuilderID(@NotNull UUID id) {
            this.id = id;
        }

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
        public String toString() {
            return this.id.toString();
        }

        @Override
        public int compareTo(ICreatureBuildInfo.CreatureBuilderID arg0) {
            return this.id.compareTo(arg0.id);
        }

        public static class IDTypeAdapter extends TypeAdapter<CreatureBuilderID> {

            @Override
            public void write(JsonWriter out, CreatureBuilderID value) throws IOException {
                out.value(value.getId().toString());
            }

            @Override
            public CreatureBuilderID read(JsonReader in) throws IOException {
                final String asStr = in.nextString();
                return new CreatureBuilderID(UUID.fromString(asStr));
            }

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

    public Statblock loadStatblock(StatblockManager statblockManager) throws FileNotFoundException;

    public Statblock loadBlankStatblock();

    public Corpse getCorpse();

    public void acceptBuildInfoVisitor(ICreatureBuildInfoVisitor visitor);
}