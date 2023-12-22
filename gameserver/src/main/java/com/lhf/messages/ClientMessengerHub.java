package com.lhf.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import com.lhf.messages.out.OutMessage;

public interface ClientMessengerHub extends ClientMessenger {
    public Collection<ClientMessenger> getClientMessengers();

    public default boolean announceDirect(OutMessage outMessage, Collection<? extends ClientMessenger> recipients) {
        if (outMessage == null || recipients == null) {
            return false;
        }

        ClientMessenger.acceptEvent(this, outMessage);

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

    public default boolean announceDirect(OutMessage outMessage, ClientMessenger... recipients) {
        return this.announceDirect(outMessage, Arrays.asList(recipients));
    }

    public default boolean announce(OutMessage outMessage, Set<? extends ClientMessenger> deafened) {
        Collection<ClientMessenger> subscribedC = this.getClientMessengers();
        List<ClientMessenger> filteredList = subscribedC.stream()
                .filter(messenger -> messenger != null && messenger instanceof ClientMessenger)
                .filter(messenger -> deafened == null || !deafened.contains(messenger)).toList();
        return this.announceDirect(outMessage, filteredList);
    }

    public default boolean announce(OutMessage.Builder<?> builder, Set<? extends ClientMessenger> deafened) {
        return this.announce(builder.Build(), deafened);
    }

    public default boolean announce(OutMessage outMessage, ClientMessenger... deafened) {
        Set<ClientMessenger> deafCollective = new TreeSet<>(ClientMessenger.getComparator());
        deafCollective.addAll(Arrays.asList(deafened));
        return this.announce(outMessage, deafCollective);
    }

    public default boolean announce(OutMessage.Builder<?> builder, ClientMessenger... deafened) {
        return this.announce(builder.Build(), deafened);
    }

    public default boolean announce(OutMessage outMessage) {
        return this.announceDirect(outMessage, this.getClientMessengers());
    }

    public default boolean announce(OutMessage.Builder<?> builder) {
        return this.announceDirect(builder.Build(), this.getClientMessengers());
    }

    @Override
    public default void receive(OutMessage msg) {
        if (msg != null && !msg.isFirstRecieve(this.getClientID())) {
            this.log(Level.FINE, "Received message, defaulting to announce it");
            this.announceDirect(msg, this.getClientMessengers());
        }
    }
}
