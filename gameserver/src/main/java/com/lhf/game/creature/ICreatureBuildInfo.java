package com.lhf.game.creature;

import java.io.IOException;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.server.interfaces.NotNull;

public interface ICreatureBuildInfo extends Serializable {

    public static final int DEFAULT_HP = 12;
    public static final int DEFAULT_AC = 11;
    public static final int DEFAULT_XP_WORTH = 500;

    public static void setDefaultFlavorReactions(
            EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> needDefaults) {
        if (needDefaults == null) {
            needDefaults = new EnumMap<>(DamgeFlavorReaction.class);
        }
        for (DamgeFlavorReaction reaction : DamgeFlavorReaction.values()) {
            needDefaults.computeIfAbsent(reaction, key -> EnumSet.noneOf(DamageFlavor.class));
        }
        needDefaults
                .computeIfAbsent(DamgeFlavorReaction.CURATIVES, key -> EnumSet.of(DamageFlavor.HEALING))
                .add(DamageFlavor.HEALING);
        needDefaults
                .computeIfAbsent(DamgeFlavorReaction.IMMUNITIES, key -> EnumSet.of(DamageFlavor.AGGRO))
                .add(DamageFlavor.AGGRO);
    }

    public static void setDefaultStats(EnumMap<Stats, Integer> needDefaults) {
        if (needDefaults == null) {
            needDefaults = new EnumMap<>(Stats.class);
        }
        needDefaults.put(Stats.MAXHP, ICreatureBuildInfo.DEFAULT_HP);
        needDefaults.put(Stats.CURRENTHP, ICreatureBuildInfo.DEFAULT_HP);
        needDefaults.put(Stats.AC, ICreatureBuildInfo.DEFAULT_AC);
        needDefaults.put(Stats.XPWORTH, ICreatureBuildInfo.DEFAULT_XP_WORTH);
    }

    // Reactions to DamageFlavors
    public enum DamgeFlavorReaction {
        WEAKNESSES,
        RESISTANCES,
        IMMUNITIES,
        CURATIVES
    };

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

    public String getCreatureRace();

    public AttributeBlock getAttributeBlock();

    public EnumMap<Stats, Integer> getStats();

    public EnumSet<EquipmentTypes> getProficiencies();

    public Inventory getInventory();

    public EnumMap<EquipmentSlots, Equipable> getEquipmentSlots();

    public EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> getDamageFlavorReactions();

    public String getName();

    public CreatureFaction getFaction();

    public VocationName getVocation();

    public Integer getVocationLevel();

    public void acceptBuildInfoVisitor(ICreatureBuildInfoVisitor visitor);
}