package com.lhf;

import com.lhf.messages.events.SeeEvent;

public interface Examinable {
    String getName();

    String printDescription();

    default SeeEvent produceMessage() {
        return this.produceMessage(SeeEvent.getBuilder().setExaminable(this));
    }

    default SeeEvent produceMessage(SeeEvent.ABuilder<?> seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeEvent.getBuilder().setExaminable(this);
        }
        return seeOutMessage.Build();
    }
}
