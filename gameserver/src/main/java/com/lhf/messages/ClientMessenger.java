package com.lhf.messages;

import java.util.Comparator;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.Taggable;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.ClientID;

public interface ClientMessenger extends Taggable {
    public void receive(OutMessage msg);

    public default void receive(OutMessage.Builder<?> builder) {
        this.receive(builder.Build());
    }

    public abstract void log(Level logLevel, String logMessage);

    public abstract void log(Level logLevel, Supplier<String> logMessageSupplier);

    public ClientID getClientID();

    /**
     * Accepts an event. Calls {@link #receive(OutMessage)} if the event shows that
     * this is the first time that the event has come to this ClientID
     * 
     * @param messenger
     * @param event
     */
    public static void acceptEvent(ClientMessenger messenger, OutMessage event) {
        if (event != null && messenger != null && event.isFirstRecieve(messenger.getClientID())) {
            messenger.receive(event);
        }
    }

    /**
     * Accepts an event. Calls {@link #receive(OutMessage)} if the event shows that
     * this is the first time that the event has come to this ClientID
     * 
     * @param messenger
     * @param builder
     */
    public static void acceptEvent(ClientMessenger messenger, OutMessage.Builder<?> builder) {
        ClientMessenger.acceptEvent(messenger, builder.Build());
    }

    public static class ClientMessengerComparator implements Comparator<ClientMessenger> {

        @Override
        public int compare(ClientMessenger arg0, ClientMessenger arg1) {
            if (arg0 == null || arg1 == null) {
                throw new NullPointerException();
            }
            return arg0.getClientID().compareTo(arg1.getClientID());
        }

    }

    public static Comparator<ClientMessenger> getComparator() {
        return new ClientMessengerComparator();
    }

}
