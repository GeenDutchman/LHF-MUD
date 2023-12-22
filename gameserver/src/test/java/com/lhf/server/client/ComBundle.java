package com.lhf.server.client;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

import com.lhf.messages.events.GameEvent;

public abstract class ComBundle {
    public SendStrategy sssb;
    @Captor
    public ArgumentCaptor<GameEvent> outCaptor;

    public ComBundle() {
        this.sssb = Mockito.mock(SendStrategy.class);
        Mockito.lenient().doAnswer(invocation -> {
            Object object = invocation.getArgument(0);
            System.out.print(object.getClass().getName() + ' ');
            System.out.print(Mockito.mockingDetails(this.sssb).getInvocations().size());
            this.print(object.toString(), false);
            return null;
        }).when(this.sssb).send(Mockito.any(GameEvent.class));
    }

    protected synchronized void print(String buffer, boolean sending) {
        System.out.println("***********************" + this.getName() + "**********************");
        for (String part : buffer.split("\n")) {
            System.out.print(sending ? ">>> " : "<<< ");
            System.out.println(part);
        }
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    }

    protected String getName() {
        return String.valueOf(this.hashCode());
    }
}
