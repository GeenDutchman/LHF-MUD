package com.lhf.game.item;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.map.RoomEffectSource;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEventTester;

public interface GameEventProcessorCapability extends ItemCapability, GameEventProcessor {
    public Map<GameEventType, Integer> getEventCounts();

    public boolean isActivated();

    public static interface IEffectSets {
        public default Set<CreatureEffectSource> getCreatureEffects() {
            return Set.of();
        }

        // TODO: areaeffects should aggregate creatureeffects
        public default Set<RoomEffectSource> getAreaEffects() {
            return Set.of();
        }

        public static class EffectSets implements IEffectSets {
            private final Set<CreatureEffectSource> creatureEffects;
            private final Set<RoomEffectSource> roomEffects;

            public EffectSets() {
                this.creatureEffects = new LinkedHashSet<>();
                this.roomEffects = new LinkedHashSet<>();
            }

            public EffectSets(Set<CreatureEffectSource> creatureEffects, Set<RoomEffectSource> roomEffects) {
                this.creatureEffects = creatureEffects;
                this.roomEffects = roomEffects;
            }

            public Set<CreatureEffectSource> getCreatureEffects() {
                return Collections.unmodifiableSet(creatureEffects);
            }

            public Set<RoomEffectSource> getRoomEffects() {
                return Collections.unmodifiableSet(roomEffects);
            }

        }

    }

    /**
     * Per GameEventType, have a navigablemap of thresholds to changes to enact
     * 
     * @return
     */
    public Map<GameEventTester, NavigableMap<Integer, IEffectSets>> getTriggeredEffects();

    // map for itemeffects??
}
