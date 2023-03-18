package com.lhf;

import com.lhf.messages.out.SeeOutMessage;

public interface Examinable {
    String getName();

    String printDescription();

    default SeeOutMessage produceMessage() {
        return SeeOutMessage.getBuilder().setExaminable(this).Build();
    }
}
