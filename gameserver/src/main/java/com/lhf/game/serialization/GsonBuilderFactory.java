package com.lhf.game.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.conversation.ConversationPattern;
import com.lhf.game.creature.conversation.ConversationPatternSerializer;
import com.lhf.game.item.AItem;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.EquipableDeserializer;
import com.lhf.game.item.IItem;
import com.lhf.game.item.ItemDeserializer;
import com.lhf.game.item.Takeable;
import com.lhf.game.item.TakeableDeserializer;
import com.lhf.game.magic.CreatureAOESpellEntry;
import com.lhf.game.magic.CreatureTargetingSpellEntry;
import com.lhf.game.magic.DMRoomTargetingSpellEntry;
import com.lhf.game.magic.DungeonTargetingSpellEntry;
import com.lhf.game.magic.RoomTargetingSpellEntry;
import com.lhf.game.magic.SpellEntry;
import com.lhf.game.magic.concrete.ElectricWisp;
import com.lhf.game.magic.concrete.Ensouling;
import com.lhf.game.magic.concrete.ShockBolt;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.concrete.ThunderStrike;
import com.lhf.game.map.DMRoomEffectSource;
import com.lhf.game.map.DungeonEffectSource;
import com.lhf.game.map.RoomEffectSource;

public class GsonBuilderFactory {
    private final GsonBuilder gsonBuilder;

    public GsonBuilderFactory() {
        this.gsonBuilder = new GsonBuilder();
    }

    public static GsonBuilderFactory start() {
        return new GsonBuilderFactory();
    }

    public GsonBuilderFactory prettyPrinting() {
        this.gsonBuilder.setPrettyPrinting();
        return this;
    }

    public GsonBuilderFactory conversation() {
        this.gsonBuilder.registerTypeAdapter(ConversationPattern.class, new ConversationPatternSerializer());
        return this;
    }

    public GsonBuilderFactory effects() {
        RuntimeTypeAdapterFactory<EntityEffectSource> effectSourceAdapter = RuntimeTypeAdapterFactory
                .of(EntityEffectSource.class, "className", true)
                .registerSubtype(CreatureEffectSource.class, CreatureEffectSource.class.getName())
                .registerSubtype(RoomEffectSource.class, RoomEffectSource.class.getName())
                .registerSubtype(DMRoomEffectSource.class, DMRoomEffectSource.class.getName())
                .registerSubtype(DungeonEffectSource.class, DungeonEffectSource.class.getName())
                .recognizeSubtypes();
        this.gsonBuilder.registerTypeAdapterFactory(effectSourceAdapter);
        return this;
    }

    public GsonBuilderFactory items() {
        this.effects();
        this.gsonBuilder.registerTypeAdapter(Equipable.class, new EquipableDeserializer<Equipable>());
        this.gsonBuilder.registerTypeAdapter(Takeable.class, new TakeableDeserializer<>());
        this.gsonBuilder.registerTypeAdapter(AItem.class, new ItemDeserializer<>());
        return this;
    }

    public GsonBuilderFactory cachedReferences() {
        DataTypeAdapterFactory dtaf = new DataTypeAdapterFactory.Builder()
                .add(IItem.class, new CachedIItemTypeAdapter())
                .add(ICreature.class, new CachedICreatureTypeAdapter())
                .build();
        this.gsonBuilder.registerTypeAdapterFactory(dtaf);
        return this;
    }

    public GsonBuilderFactory spells() {
        this.items();
        this.conversation();
        RuntimeTypeAdapterFactory<SpellEntry> spellEntryAdapter = RuntimeTypeAdapterFactory
                .of(SpellEntry.class, "className", true)
                .registerSubtype(CreatureTargetingSpellEntry.class, CreatureTargetingSpellEntry.class.getName())
                .registerSubtype(CreatureAOESpellEntry.class, CreatureAOESpellEntry.class.getName())
                .registerSubtype(RoomTargetingSpellEntry.class, RoomTargetingSpellEntry.class.getName())
                .registerSubtype(DMRoomTargetingSpellEntry.class, DMRoomTargetingSpellEntry.class.getName())
                .registerSubtype(DungeonTargetingSpellEntry.class, DungeonTargetingSpellEntry.class.getName())
                .registerSubtype(ShockBolt.class, ShockBolt.class.getName())
                .registerSubtype(ThunderStrike.class, ThunderStrike.class.getName())
                .registerSubtype(Thaumaturgy.class, Thaumaturgy.class.getName())
                .registerSubtype(Ensouling.class, Ensouling.class.getName())
                .registerSubtype(ElectricWisp.class, ElectricWisp.class.getName())
                .recognizeSubtypes();
        this.gsonBuilder.registerTypeAdapterFactory(spellEntryAdapter);
        return this;
    }

    public GsonBuilder rawBuilder() {
        return this.gsonBuilder;
    }

    public Gson build() {
        return this.gsonBuilder.create();
    }
}
