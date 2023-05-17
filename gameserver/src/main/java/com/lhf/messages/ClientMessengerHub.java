package com.lhf.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.lhf.messages.out.OutMessage;

public interface ClientMessengerHub {
    public Collection<ClientMessenger> getClientMessengers();

    public default boolean announceDirect(OutMessage outMessage, Collection<ClientMessenger> recipients) {
        if (outMessage == null || recipients == null) {
            return false;
        }
        Set<ClientMessenger> sentSet = new TreeSet<>(ClientMessenger.getComparator());

        recipients.stream()
                .filter(messenger -> messenger != null && messenger instanceof ClientMessenger)
                .forEachOrdered(messenger -> {
                    if (sentSet.add(messenger)) {
                        messenger.sendMsg(outMessage);
                    }
                });
        return true;
    }

    public default boolean announceDirect(OutMessage outMessage, ClientMessenger... recipients) {
        return this.announceDirect(outMessage, Arrays.asList(recipients));
    }

    public default boolean announce(OutMessage outMessage, Set<ClientMessenger> deafened) {
        Collection<ClientMessenger> subscribedC = this.getClientMessengers();
        List<ClientMessenger> filteredList = subscribedC.stream()
                .filter(messenger -> messenger != null && messenger instanceof ClientMessenger)
                .filter(messenger -> deafened == null || !deafened.contains(messenger)).toList();
        return this.announceDirect(outMessage, filteredList);
    }

    public default boolean announce(OutMessage.Builder<?> builder, Set<ClientMessenger> deafened) {
        return this.announce(builder.Build(), deafened);
    }

    public default boolean announce(OutMessage outMessage) {
        return this.announceDirect(outMessage, this.getClientMessengers());
    }

    public default boolean announce(OutMessage.Builder<?> builder) {
        return this.announceDirect(builder.Build(), this.getClientMessengers());
    }
}
