package com.lhf;

import com.lhf.messages.out.SeeOutMessage;

public interface Examinable {
    String getName();

    String printDescription();

    SeeOutMessage produceMessage();
}
