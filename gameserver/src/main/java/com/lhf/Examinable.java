package com.lhf;

import com.lhf.messages.out.SeeEvent;

public interface Examinable {
    String getName();

    String printDescription();

    default SeeEvent produceMessage() {
        return this.produceMessage(SeeEvent.getBuilder());
    }

    default SeeEvent produceMessage(SeeEvent.Builder seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeEvent.getBuilder();
        }
        return seeOutMessage.setExaminable(this).Build();
    }
}
