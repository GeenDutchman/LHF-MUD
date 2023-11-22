package com.lhf;

import com.lhf.messages.out.SeeOutMessage;

public interface Examinable {
    String getName();

    String printDescription();

    default SeeOutMessage produceMessage() {
        return this.produceMessage(SeeOutMessage.getBuilder());
    }

    default SeeOutMessage produceMessage(SeeOutMessage.Builder seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeOutMessage.getBuilder();
        }
        return seeOutMessage.setExaminable(this).Build();
    }
}
