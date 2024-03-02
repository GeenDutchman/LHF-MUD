package com.lhf.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.logging.Level;

import com.lhf.messages.events.GameEvent;

public interface GameEventProcessorHub extends GameEventProcessor {
    public Collection<GameEventProcessor> getGameEventProcessors();

    public default boolean announceDirect(GameEvent gameEvent, Collection<? extends GameEventProcessor> recipients) {
        if (gameEvent == null || recipients == null) {
            return false;
        }

        Set<GameEventProcessor> sentSet = new TreeSet<>(GameEventProcessor.getComparator());

        for (final GameEventProcessor recipient : recipients) {
            if (recipient != null && sentSet.add(recipient)) {
                GameEventProcessor.acceptEvent(recipient, gameEvent);
            }
        }
        return true;
    }

    public default boolean announceDirect(GameEvent gameEvent, GameEventProcessor... recipients) {
        return this.announceDirect(gameEvent, Arrays.asList(recipients));
    }

    public default boolean announce(GameEvent gameEvent, Set<? extends GameEventProcessor> deafened) {
        Collection<GameEventProcessor> subscribedC = this.getGameEventProcessors();
        List<GameEventProcessor> filteredList = subscribedC.stream()
                .filter(messenger -> messenger != null && messenger instanceof GameEventProcessor)
                .filter(messenger -> deafened == null || !deafened.contains(messenger)).toList();
        return this.announceDirect(gameEvent, filteredList);
    }

    public default boolean announce(GameEvent.Builder<?> builder, Set<? extends GameEventProcessor> deafened) {
        return this.announce(builder.Build(), deafened);
    }

    public default boolean announce(GameEvent gameEvent, GameEventProcessor... deafened) {
        Set<GameEventProcessor> deafCollective = new TreeSet<>(GameEventProcessor.getComparator());
        deafCollective.addAll(Arrays.asList(deafened));
        return this.announce(gameEvent, deafCollective);
    }

    public default boolean announce(GameEvent.Builder<?> builder, GameEventProcessor... deafened) {
        return this.announce(builder.Build(), deafened);
    }

    public default boolean announce(GameEvent gameEvent) {
        return this.announceDirect(gameEvent, this.getGameEventProcessors());
    }

    public default boolean announce(GameEvent.Builder<?> builder) {
        return this.announceDirect(builder.Build(), this.getGameEventProcessors());
    }

    @Override
    default Consumer<GameEvent> getAcceptHook() {
        return (event) -> {
            if (event == null) {
                return;
            }
            this.log(Level.FINEST,
                    () -> String.format("Received message %s, defaulting to announce it", event.getUuid()));
            this.announceDirect(event, this.getGameEventProcessors());
        };
    }

}
