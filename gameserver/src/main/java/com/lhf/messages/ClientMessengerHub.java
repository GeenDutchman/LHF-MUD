package com.lhf.messages;

import java.util.Set;

import com.lhf.messages.out.OutMessage;

public interface ClientMessengerHub {
    public Set<ClientMessenger> getClientMessengers();

    public default boolean announce(OutMessage outMessage, Set<ClientMessenger> deafened) {
        Set<ClientMessenger> subscribed = this.getClientMessengers();
        if (outMessage == null || subscribed == null) {
            return false;
        }
        subscribed.stream()
                .filter(messenger -> messenger != null)
                .filter(messenger -> deafened == null || !deafened.contains(messenger))
                .forEachOrdered(messenger -> messenger.sendMsg(outMessage));
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
