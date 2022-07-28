package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class CannotSpeakToMessage extends OutMessage {
    private String creatureName;
    private String taggedCreatureName;
    private String msg;

    public CannotSpeakToMessage(String creatureName, String taggedCreatureName) {
        super(OutMessageType.CANNOT_SPEAK_TO);
        this.creatureName = creatureName;
        this.taggedCreatureName = taggedCreatureName;
        String temp = "This room does not contain anyone named ";
        if (this.taggedCreatureName != null && this.taggedCreatureName.length() > 0) {
            temp += this.taggedCreatureName;
        } else if (this.creatureName != null && this.creatureName.length() > 0) {
            temp += "'" + this.creatureName + "'";
        } else {
            temp += "anything like that";
        }
        temp += ".  So you are just talking to the air.";
        this.msg = temp;
    }

    @Override
    public String toString() {
        return msg;
    }

    public String getCreatureName() {
        return creatureName;
    }

    public String getTaggedCreatureName() {
        return taggedCreatureName;
    }

    public String getMsg() {
        return msg;
    }
}
