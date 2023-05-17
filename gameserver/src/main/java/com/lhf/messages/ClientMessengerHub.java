package com.lhf.messages;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.lhf.messages.out.OutMessage;

public interface ClientMessengerHub {
    public Collection<ClientMessenger> getClientMessengers();

    public default boolean announce(OutMessage outMessage, Set<ClientMessenger> deafened) {
        Collection<ClientMessenger> subscribedC = this.getClientMessengers();
        if (outMessage == null || subscribedC == null) {
            return false;
        }
        Set<ClientMessenger> sentSet = new TreeSet<>(ClientMessenger.getComparator());

        subscribedC.stream()
                .filter(messenger -> messenger != null)
                .filter(messenger -> deafened == null || !deafened.contains(messenger))
                .forEachOrdered(messenger -> {
                    if (sentSet.add(messenger)) {
                        messenger.sendMsg(outMessage);
                    }
                });
        return true;
    }

    public default boolean announce(OutMessage.Builder<?> builder, Set<ClientMessenger> deafened) {
        return this.announce(builder.Build(), deafened);
    }

    public default boolean announce(OutMessage outMessage) {
        return this.announce(outMessage, null);
    }

    public default boolean announce(OutMessage.Builder<?> builder) {
        return this.announce(builder.Build(), null);
    }
}
