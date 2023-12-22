package com.lhf.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.logging.Level;

import com.lhf.messages.out.GameEvent;

public interface ClientMessengerHub extends ClientMessenger {
    public Collection<ClientMessenger> getClientMessengers();

    public default boolean announceDirect(GameEvent outMessage, Collection<? extends ClientMessenger> recipients) {
        if (outMessage == null || recipients == null) {
            return false;
        }

        Set<ClientMessenger> sentSet = new TreeSet<>(ClientMessenger.getComparator());

        recipients.stream()
                .filter(messenger -> messenger != null)
                .forEachOrdered(messenger -> {
                    if (sentSet.add(messenger)) {
                        ClientMessenger.acceptEvent(messenger, outMessage);
                    }
                });
        return true;
    }

    public default boolean announceDirect(GameEvent outMessage, ClientMessenger... recipients) {
        return this.announceDirect(outMessage, Arrays.asList(recipients));
    }

    public default boolean announce(GameEvent outMessage, Set<? extends ClientMessenger> deafened) {
        Collection<ClientMessenger> subscribedC = this.getClientMessengers();
        List<ClientMessenger> filteredList = subscribedC.stream()
                .filter(messenger -> messenger != null && messenger instanceof ClientMessenger)
                .filter(messenger -> deafened == null || !deafened.contains(messenger)).toList();
        return this.announceDirect(outMessage, filteredList);
    }

    public default boolean announce(GameEvent.Builder<?> builder, Set<? extends ClientMessenger> deafened) {
        return this.announce(builder.Build(), deafened);
    }

    public default boolean announce(GameEvent outMessage, ClientMessenger... deafened) {
        Set<ClientMessenger> deafCollective = new TreeSet<>(ClientMessenger.getComparator());
        deafCollective.addAll(Arrays.asList(deafened));
        return this.announce(outMessage, deafCollective);
    }

    public default boolean announce(GameEvent.Builder<?> builder, ClientMessenger... deafened) {
        return this.announce(builder.Build(), deafened);
    }

    public default boolean announce(GameEvent outMessage) {
        return this.announceDirect(outMessage, this.getClientMessengers());
    }

    public default boolean announce(GameEvent.Builder<?> builder) {
        return this.announceDirect(builder.Build(), this.getClientMessengers());
    }

    @Override
    default Consumer<GameEvent> getAcceptHook() {
        return (event) -> {
            if (event == null) {
                return;
            }
            this.log(Level.FINEST,
                    () -> String.format("Received message %s, defaulting to announce it", event.getUuid()));
            this.announceDirect(event, this.getClientMessengers());
        };
    }

}
