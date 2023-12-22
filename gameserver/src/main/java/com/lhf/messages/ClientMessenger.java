package com.lhf.messages;

import java.util.Comparator;

import com.lhf.Taggable;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.ClientID;

public interface ClientMessenger extends Taggable {
    public void receive(OutMessage msg);

    public default void receive(OutMessage.Builder<?> builder) {
        this.receive(builder.Build());
    }

    public ClientID getClientID();

    public class ClientMessengerComparator implements Comparator<ClientMessenger> {

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
