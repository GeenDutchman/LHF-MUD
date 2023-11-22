package com.lhf.server.client;

import java.util.ArrayList;
import java.util.List;

import com.lhf.game.events.messages.OutMessageType;
import com.lhf.game.events.messages.out.OutMessage;

public class ListBufferSendStrategy extends StringBufferSendStrategy {
    private List<OutMessage> lBuffer;

    public ListBufferSendStrategy() {
        this.lBuffer = new ArrayList<>();
    }

    @Override
    public void clear() {
        super.clear();
        this.lBuffer.clear();
    }

    @Override
    public void send(OutMessage toSend) {
        super.send(toSend);
        this.lBuffer.add(toSend);
    }

    public int size() {
        return this.lBuffer.size();
    }

    public OutMessage get(int index) {
        return this.lBuffer.get(index);
    }

    public OutMessage getMostRecent(OutMessageType type) {
        for (int i = this.lBuffer.size() - 1; i >= 0; i--) {
            OutMessage found = this.lBuffer.get(i);
            if (found.getOutType().equals(type)) {
                return found;
            }
        }
        return null;
    }

}
