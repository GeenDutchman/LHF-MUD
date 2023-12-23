package com.lhf.messages;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.Taggable;
import com.lhf.messages.events.GameEvent;

public interface GameEventProcessor extends Taggable {
    /**
     * Accepts a GameEventProcessor and an GameEvent. Utilizes
     * {@link #getAcceptHook()} to find how the implementation wants to handle the
     * event.
     * <p>
     * Does nothing if the GameEventProcessor or GameEvent is null. If
     * {@link #getAcceptHook()} returns null that is valid, but note that the event
     * will be listed as being recieved by this GameEventProcessor.
     */
    public static final BiConsumer<GameEventProcessor, GameEvent> eventAccepter = (messenger, event) -> {
        if (messenger == null || event == null) {
            return;
        }
        Consumer<GameEvent> acceptHook = messenger.getAcceptHook();
        if (event.isFirstRecieve(messenger.getClientID()) && acceptHook != null) {
            acceptHook.accept(event);
        }
    };

    /**
     * Returns a {@link java.util.function.Consumer Consumer<GameEvent>} that
     * should process the event on the behalf of the GameEventProcessor.
     * It is valid to return null and say that this GameEventProcessor won't process
     * it.
     * <p>
     * The assumption is that the event has already been marked by
     * {@link #getClientID()}.
     * 
     * @return {@link java.util.function.Consumer Consumer<GameEvent>} or null
     */
    public abstract Consumer<GameEvent> getAcceptHook();

    public abstract void log(Level logLevel, String logMessage);

    public abstract void log(Level logLevel, Supplier<String> logMessageSupplier);

    public ClientID getClientID();

    /**
     * Accepts an event. Calls {@link #eventAccepter}.
     * 
     * @param messenger
     * @param event
     */
    public static void acceptEvent(GameEventProcessor messenger, GameEvent event) {
        GameEventProcessor.eventAccepter.accept(messenger, event);
    }

    /**
     * Accepts an event. Calls {@link #eventAccepter}.
     * 
     * @param messenger
     * @param builder
     */
    public static void acceptEvent(GameEventProcessor messenger, GameEvent.Builder<?> builder) {
        GameEventProcessor.eventAccepter.accept(messenger, builder.Build());
    }

    public static class ClientMessengerComparator implements Comparator<GameEventProcessor> {

        @Override
        public int compare(GameEventProcessor arg0, GameEventProcessor arg1) {
            if (arg0 == null || arg1 == null) {
                throw new NullPointerException();
            }
            return arg0.getClientID().compareTo(arg1.getClientID());
        }

    }

    public static Comparator<GameEventProcessor> getComparator() {
        return new ClientMessengerComparator();
    }

}
