package com.lhf.messages;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.Taggable;
import com.lhf.messages.out.GameEvent;
import com.lhf.server.client.ClientID;

public interface ClientMessenger extends Taggable {
    /**
     * Accepts a ClientMessenger and an OutMessage. Utilizes
     * {@link #getAcceptHook()} to find how the implementation wants to handle the
     * event.
     * <p>
     * Does nothing if the ClientMessenger or OutMessage is null. If
     * {@link #getAcceptHook()} returns null that is valid, but note that the event
     * will be listed as being recieved by this ClientMessenger.
     */
    public static final BiConsumer<ClientMessenger, GameEvent> eventAccepter = (messenger, event) -> {
        if (messenger == null || event == null) {
            return;
        }
        Consumer<GameEvent> acceptHook = messenger.getAcceptHook();
        if (event.isFirstRecieve(messenger.getClientID()) && acceptHook != null) {
            acceptHook.accept(event);
        }
    };

    /**
     * Returns a {@link java.util.function.Consumer Consumer<OutMessage>} that
     * should process the event on the behalf of the ClientMessenger.
     * It is valid to return null and say that this ClientMessenger won't process
     * it.
     * <p>
     * The assumption is that the event has already been marked by
     * {@link #getClientID()}.
     * 
     * @return {@link java.util.function.Consumer Consumer<OutMessage>} or null
     */
    public abstract Consumer<GameEvent> getAcceptHook();

    // public void receive(OutMessage msg);

    // public default void receive(OutMessage.Builder<?> builder) {
    // this.receive(builder.Build());
    // }

    public abstract void log(Level logLevel, String logMessage);

    public abstract void log(Level logLevel, Supplier<String> logMessageSupplier);

    public ClientID getClientID();

    /**
     * Accepts an event. Calls {@link #eventAccepter}.
     * 
     * @param messenger
     * @param event
     */
    public static void acceptEvent(ClientMessenger messenger, GameEvent event) {
        ClientMessenger.eventAccepter.accept(messenger, event);
    }

    /**
     * Accepts an event. Calls {@link #eventAccepter}.
     * 
     * @param messenger
     * @param builder
     */
    public static void acceptEvent(ClientMessenger messenger, GameEvent.Builder<?> builder) {
        ClientMessenger.eventAccepter.accept(messenger, builder.Build());
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
