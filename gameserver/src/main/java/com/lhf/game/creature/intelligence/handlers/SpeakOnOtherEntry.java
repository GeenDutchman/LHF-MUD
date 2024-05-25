package com.lhf.game.creature.intelligence.handlers;

import java.util.logging.Level;

import com.lhf.game.creature.conversation.ConversationTransformer;
import com.lhf.game.creature.conversation.ConversationTreeNodeResult;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.RoomEnteredEvent;
import com.lhf.messages.in.SayMessage;
import com.lhf.server.client.CommandInvoker;

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
        if (GameEventType.ROOM_ENTERED.equals(event.getXmlEventType())) {
            RoomEnteredEvent reom = (RoomEnteredEvent) event;
            CommandInvoker newbie = reom.getNewbie();
            if (newbie != null) {
                ConversationTransformer transformer = ConversationTransformer.ofTalkerAndListener(bai.getNpc(), newbie);
                ConversationTreeNodeResult sayit = null;
                if (this.greeting != null) {
                    sayit = ConversationTreeNodeResult.fromString(transformer, this.greeting, null, null);
                } else if (bai.getNpc().getConvoTree() != null) {
                    sayit = bai.getNpc().getConvoTree().getAGreeting(transformer);
                }
                if (sayit == null) {
                    this.logger.log(Level.WARNING,
                            () -> String.format("Using fallback \"Hello There!\" for AI %s", bai.toString()));
                    sayit = ConversationTreeNodeResult.fromString(transformer, "Hello There!", null, null);
                }
                SayMessage say = SayMessage.fromOutputBuilder(sayit.getBody(), newbie.getName());
                bai.applyChain(null, say);
            }
        }

    }

}
