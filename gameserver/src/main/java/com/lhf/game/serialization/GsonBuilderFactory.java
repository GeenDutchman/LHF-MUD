package com.lhf.game.serialization;

import java.util.EnumSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.battle.BattleManager.IBattleManagerBuildInfo;
import com.lhf.game.creature.CreatureBuildInfo;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.DungeonMaster.DungeonMasterBuildInfo;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.ICreatureBuildInfo;
import com.lhf.game.creature.IMonster.IMonsterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo;
import com.lhf.game.creature.Player.PlayerBuildInfo;
import com.lhf.game.creature.conversation.ConversationPattern;
import com.lhf.game.creature.conversation.ConversationPatternSerializer;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.handlers.BattleTurnHandler;
import com.lhf.game.creature.intelligence.handlers.HandleCreatureAffected;
import com.lhf.game.creature.intelligence.handlers.LewdAIHandler;
import com.lhf.game.creature.intelligence.handlers.RoomExitHandler;
import com.lhf.game.creature.intelligence.handlers.SilencedHandler;
import com.lhf.game.creature.intelligence.handlers.SpeakOnOtherEntry;
import com.lhf.game.creature.intelligence.handlers.SpokenPromptChunk;
import com.lhf.game.item.IItem;
import com.lhf.game.item.IItemRunTypeAdapterFactoryProducer;
import com.lhf.game.lewd.AfterGlow;
import com.lhf.game.lewd.LewdBabyMaker;
import com.lhf.game.lewd.LewdProduct;
import com.lhf.game.lewd.LewdProductList;
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
import com.lhf.game.map.Area.AreaBuilder;
import com.lhf.game.map.CloseableDoorway;
import com.lhf.game.map.DMRoom.DMRoomBuilder;
import com.lhf.game.map.DMRoomEffectSource;
import com.lhf.game.map.Doorway;
import com.lhf.game.map.Dungeon.DungeonBuilder;
import com.lhf.game.map.DungeonEffectSource;
import com.lhf.game.map.KeyedDoorway;
import com.lhf.game.map.Land.LandBuilder;
import com.lhf.game.map.OneWayDoorway;
import com.lhf.game.map.RestArea.IRestAreaBuildInfo;
import com.lhf.game.map.Room.RoomBuilder;
import com.lhf.game.map.RoomEffectSource;
import com.lhf.game.map.SubArea.ISubAreaBuildInfo;
import com.lhf.game.map.SubArea.SubAreaBuilder;

public class GsonBuilderFactory {
    private enum Loaded {
        EFFECTSOURCE, EFFECTS, ITEMS, CONVERSATION, CREATURE_INFO, HANDLERS, SUBAREA, LEWDPRODUCT, AREAS, DOORWAYS,
        LANDS, CACHED, SPELLS;
    }

    private final GsonBuilder gsonBuilder;
    private final EnumSet<Loaded> loaded;

    public GsonBuilderFactory() {
        this.gsonBuilder = new GsonBuilder();
        this.loaded = EnumSet.noneOf(Loaded.class);
    }

    public static GsonBuilderFactory start() {
        return new GsonBuilderFactory();
    }

    public synchronized GsonBuilderFactory prettyPrinting() {
        this.gsonBuilder.setPrettyPrinting();
        return this;
    }

    public synchronized GsonBuilderFactory conversation() {
        if (!this.loaded.contains(Loaded.CONVERSATION)) {
            this.gsonBuilder.registerTypeAdapter(ConversationPattern.class, new ConversationPatternSerializer());
        }
        return this;
    }

    private synchronized GsonBuilderFactory effectSources() {
        if (!this.loaded.contains(Loaded.EFFECTSOURCE)) {
            this.creatureInfoBuilders();
            RuntimeTypeAdapterFactory<EntityEffectSource> effectSourceAdapter = RuntimeTypeAdapterFactory
                    .of(EntityEffectSource.class, "className", true)
                    .registerSubtype(CreatureEffectSource.class, CreatureEffectSource.class.getName())
                    .registerSubtype(RoomEffectSource.class, RoomEffectSource.class.getName())
                    .registerSubtype(DMRoomEffectSource.class, DMRoomEffectSource.class.getName())
                    .registerSubtype(DungeonEffectSource.class, DungeonEffectSource.class.getName())
                    .recognizeSubtypes();
            this.gsonBuilder.registerTypeAdapterFactory(effectSourceAdapter);
        }
        return this;
    }

    public synchronized GsonBuilderFactory items() {
        if (!this.loaded.contains(Loaded.ITEMS)) {
            this.effectSources();
            this.gsonBuilder.registerTypeAdapterFactory(IItemRunTypeAdapterFactoryProducer.produce());
        }
        return this;
    }

    private synchronized GsonBuilderFactory aiHandlers() {
        if (!this.loaded.contains(Loaded.HANDLERS)) {
            final RuntimeTypeAdapterFactory<AIHandler> aiHandlerAdapterFactory = RuntimeTypeAdapterFactory
                    .of(AIHandler.class, "className", true)
                    .registerSubtype(BattleTurnHandler.class, BattleTurnHandler.class.getName())
                    .registerSubtype(HandleCreatureAffected.class, HandleCreatureAffected.class.getName())
                    .registerSubtype(LewdAIHandler.class, LewdAIHandler.class.getName())
                    .registerSubtype(RoomExitHandler.class, RoomExitHandler.class.getName())
                    .registerSubtype(SpeakOnOtherEntry.class, SpeakOnOtherEntry.class.getName())
                    .registerSubtype(SpokenPromptChunk.class, SpokenPromptChunk.class.getName())
                    .registerSubtype(SilencedHandler.class, SilencedHandler.class.getName())
                    .recognizeSubtypes();
            this.gsonBuilder.registerTypeAdapterFactory(aiHandlerAdapterFactory);
        }
        return this;
    }

    public synchronized GsonBuilderFactory creatureInfoBuilders() {
        if (!this.loaded.contains(Loaded.CREATURE_INFO)) {
            this.conversation();
            this.aiHandlers();
            final RuntimeTypeAdapterFactory<ICreatureBuildInfo> creatureBuilderAdapterFactory = RuntimeTypeAdapterFactory
                    .of(ICreatureBuildInfo.class, "className", true)
                    .registerSubtype(CreatureBuildInfo.class, CreatureBuildInfo.class.getName())
                    .registerSubtype(PlayerBuildInfo.class, PlayerBuildInfo.class.getName())
                    .registerSubtype(INonPlayerCharacterBuildInfo.class, INonPlayerCharacterBuildInfo.class.getName())
                    .registerSubtype(DungeonMasterBuildInfo.class, DungeonMasterBuildInfo.class.getName())
                    .registerSubtype(IMonsterBuildInfo.class, IMonsterBuildInfo.class.getName())
                    .registerSubtype(INPCBuildInfo.class, INPCBuildInfo.class.getName())
                    .recognizeSubtypes();
            this.gsonBuilder.registerTypeAdapterFactory(creatureBuilderAdapterFactory);
        }
        return this;
    }

    private synchronized GsonBuilderFactory lewdProducts() {
        if (!this.loaded.contains(Loaded.LEWDPRODUCT)) {
            final RuntimeTypeAdapterFactory<LewdProduct> lewdProductAdapterFactory = RuntimeTypeAdapterFactory
                    .of(LewdProduct.class, "className", true)
                    .registerSubtype(AfterGlow.class, AfterGlow.class.getName())
                    .registerSubtype(LewdBabyMaker.class, LewdBabyMaker.class.getName())
                    .registerSubtype(LewdProductList.class, LewdProductList.class.getName())
                    .recognizeSubtypes();
            this.gsonBuilder.registerTypeAdapterFactory(lewdProductAdapterFactory);
        }
        return this;
    }

    private synchronized GsonBuilderFactory subAreaInfo() {
        if (!this.loaded.contains(Loaded.SUBAREA)) {
            this.lewdProducts();
            final RuntimeTypeAdapterFactory<ISubAreaBuildInfo> subAreaAdapterFactory = RuntimeTypeAdapterFactory
                    .of(ISubAreaBuildInfo.class, "className", true)
                    .registerSubtype(SubAreaBuilder.class, SubAreaBuilder.class.getName())
                    .registerSubtype(IBattleManagerBuildInfo.class, IBattleManagerBuildInfo.class.getName())
                    .registerSubtype(com.lhf.game.battle.BattleManager.Builder.class,
                            com.lhf.game.battle.BattleManager.Builder.class.getName())
                    .registerSubtype(IRestAreaBuildInfo.class, IRestAreaBuildInfo.class.getName())
                    .registerSubtype(com.lhf.game.map.RestArea.Builder.class,
                            com.lhf.game.map.RestArea.Builder.class.getName())
                    .recognizeSubtypes();
            this.gsonBuilder.registerTypeAdapterFactory(subAreaAdapterFactory);
        }
        return this;
    }

    public synchronized GsonBuilderFactory areas() {
        if (!this.loaded.contains(Loaded.AREAS)) {
            this.creatureInfoBuilders();
            this.subAreaInfo();
            final RuntimeTypeAdapterFactory<AreaBuilder> areaAdapterFactory = RuntimeTypeAdapterFactory
                    .of(AreaBuilder.class, "className", true)
                    .registerSubtype(RoomBuilder.class, RoomBuilder.class.getName())
                    .registerSubtype(DMRoomBuilder.class, DMRoomBuilder.class.getName())
                    .recognizeSubtypes();
            this.gsonBuilder.registerTypeAdapterFactory(areaAdapterFactory);
        }
        return this;
    }

    private synchronized GsonBuilderFactory doors() {
        if (!this.loaded.contains(Loaded.DOORWAYS)) {
            final RuntimeTypeAdapterFactory<Doorway> doorwayAdapterFactory = RuntimeTypeAdapterFactory
                    .of(Doorway.class, "className", true)
                    .registerSubtype(Doorway.class, Doorway.class.getName())
                    .registerSubtype(OneWayDoorway.class, OneWayDoorway.class.getName())
                    .registerSubtype(CloseableDoorway.class, CloseableDoorway.class.getName())
                    .registerSubtype(KeyedDoorway.class, KeyedDoorway.class.getName())
                    .recognizeSubtypes();
            this.gsonBuilder.registerTypeAdapterFactory(doorwayAdapterFactory);
        }
        return this;
    }

    public synchronized GsonBuilderFactory lands() {
        if (!this.loaded.contains(Loaded.LANDS)) {
            this.creatureInfoBuilders();
            this.areas();
            this.doors();
            final RuntimeTypeAdapterFactory<LandBuilder> landAdapterFactory = RuntimeTypeAdapterFactory
                    .of(LandBuilder.class, "className", true)
                    .registerSubtype(DungeonBuilder.class, DungeonBuilder.class.getName())
                    .recognizeSubtypes();
            this.gsonBuilder.registerTypeAdapterFactory(landAdapterFactory);
        }
        return this;
    }

    public synchronized GsonBuilderFactory cachedReferences() {
        if (!this.loaded.contains(Loaded.CACHED)) {
            DataTypeAdapterFactory dtaf = new DataTypeAdapterFactory.Builder()
                    .add(IItem.class, new CachedIItemTypeAdapter())
                    .add(ICreature.class, new CachedICreatureTypeAdapter())
                    .build();
            this.gsonBuilder.registerTypeAdapterFactory(dtaf);
        }
        return this;
    }

    public synchronized GsonBuilderFactory spells() {
        if (!this.loaded.contains(Loaded.SPELLS)) {
            this.items();
            this.creatureInfoBuilders();
            this.effectSources();
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
        }
        return this;
    }

    public GsonBuilder rawBuilder() {
        return this.gsonBuilder;
    }

    public synchronized Gson build() {
        return this.gsonBuilder.create();
    }
}
