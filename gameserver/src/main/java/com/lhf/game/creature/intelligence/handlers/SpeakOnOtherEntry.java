package com.lhf.game.creature.intelligence.handlers;

import com.lhf.Taggable;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.Command;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.RoomEnteredEvent;

public class SpeakOnOtherEntry extends AIHandler {
    protected String greeting;

    public SpeakOnOtherEntry() {
        super(GameEventType.ROOM_ENTERED);
        this.greeting = null;
    }

    public SpeakOnOtherEntry(String greeting) {
        super(GameEventType.ROOM_ENTERED);
        this.greeting = greeting;
    }

    public SpeakOnOtherEntry setGreeting(String greeting) {
        this.greeting = greeting;
        return this;
    }

    public String getGreeting() {
        return this.greeting;
    }

    @Override
    public void handle(BasicAI bai, GameEvent event) {
        if (GameEventType.ROOM_ENTERED.equals(event.getEventType())) {
            RoomEnteredEvent reom = (RoomEnteredEvent) event;
            if (reom.getNewbie() != null) {
                String sayit = null;
                if (this.greeting != null) {
                    sayit = this.greeting;
                } else if (bai.getNpc().getConvoTree() != null) {
                    sayit = bai.getNpc().getConvoTree().getAGreeting();
                }
                if (sayit == null) {
                    sayit = "Hello There!";
                }
                String name = Taggable.extract(reom.getNewbie());
                Command say = Command.parse("say \"" + sayit + "\" to " + name);
                bai.handleChain(null, say);
            }
        }

    }

}
