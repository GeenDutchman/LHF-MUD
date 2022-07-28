package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.Examinable;
import com.lhf.messages.OutMessageType;

public class SeeOutMessage extends OutMessage {
    private Examinable examinable;
    private String extraInfo;
    private String deniedReason;

    public SeeOutMessage(Examinable examinable) {
        super(OutMessageType.SEE);
        this.examinable = examinable;
        this.extraInfo = null;
        this.deniedReason = null;
    }

    public SeeOutMessage(Examinable examinable, String extraInfo) {
        super(OutMessageType.SEE);
        this.examinable = examinable;
        this.extraInfo = extraInfo.trim();
        this.deniedReason = null;
    }

    public SeeOutMessage(String deniedReason) {
        super(OutMessageType.SEE);
        this.deniedReason = deniedReason.trim();
        this.examinable = null;
        this.extraInfo = null;
    }

    @Override
    public String toString() {
        if (this.isDenied()) {
            return this.deniedReason;
        }
        if (this.examinable == null) {
            return "You cannot see that.";
        }
        StringJoiner sj = new StringJoiner(" ");
        if (this.extraInfo != null) {
            sj.add(this.extraInfo);
        }
        sj.add("<description>").add(this.examinable.printDescription()).add("</description>");
        return sj.toString();
    }

    public Examinable getExaminable() {
        return this.examinable;
    }

    public boolean isDenied() {
        return this.deniedReason != null;
    }
}
