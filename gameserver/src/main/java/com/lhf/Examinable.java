package com.lhf;

import com.lhf.messages.events.SeeEvent;

public interface Examinable {
    String getName();

    String printDescription();

    default SeeEvent produceMessage() {
        return this.produceMessage(SeeEvent.getBuilder());
    }

    default SeeEvent produceMessage(SeeEvent.Builder seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeEvent.getBuilder().setExaminable(this);
        }
        return seeOutMessage.Build();
    }
}
