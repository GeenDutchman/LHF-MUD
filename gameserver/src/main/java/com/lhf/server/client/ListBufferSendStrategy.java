package com.lhf.server.client;

import java.util.ArrayList;
import java.util.List;

import com.lhf.messages.GameEventType;
import com.lhf.messages.out.GameEvent;

public class ListBufferSendStrategy extends StringBufferSendStrategy {
    private List<GameEvent> lBuffer;

    public ListBufferSendStrategy() {
        this.lBuffer = new ArrayList<>();
    }

    @Override
    public void clear() {
        super.clear();
        this.lBuffer.clear();
    }

    @Override
    public void send(GameEvent toSend) {
        super.send(toSend);
        this.lBuffer.add(toSend);
    }

    public int size() {
        return this.lBuffer.size();
    }

    public GameEvent get(int index) {
        return this.lBuffer.get(index);
    }

    public GameEvent getMostRecent(GameEventType type) {
        for (int i = this.lBuffer.size() - 1; i >= 0; i--) {
            GameEvent found = this.lBuffer.get(i);
            if (found.getEventType().equals(type)) {
                return found;
            }
        }
        return null;
    }

}
