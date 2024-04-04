package com.lhf;

import com.lhf.messages.events.SeeEvent;

public interface Examinable extends Taggable {
    String getName();

    String getDescription();

    @Override
    public default String getSimpleContent() {
        return this.getName();
    }

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
