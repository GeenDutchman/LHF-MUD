package com.lhf.game.creature.intelligence.handlers;

import com.lhf.Taggable;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.RoomEnteredEvent;
import com.lhf.messages.in.SayMessage;

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
    public void handle(BasicAI bai, GameEvent msg) {
        if (GameEventType.ROOM_ENTERED.equals(msg.getEventType())) {
            RoomEnteredEvent reom = (RoomEnteredEvent) msg;
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
                SayMessage say = (SayMessage) CommandBuilder.fromCommand(CommandMessage.SAY,
                        "say \"" + sayit + "\" to " + name);
                CommandBuilder.addDirect(say, sayit);
                CommandBuilder.addIndirect(say, "to", name);
                bai.handleChain(null, say);
            }
        }

    }

}
