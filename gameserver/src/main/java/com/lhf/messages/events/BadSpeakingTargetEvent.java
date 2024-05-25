package com.lhf.messages.events;

import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.messages.GameEventType;

public class BadSpeakingTargetEvent extends GameEvent {
    private final String creatureName;
    private final String msg;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String creatureName;
        private String msg;

        protected Builder() {
            super(GameEventType.CANNOT_SPEAK_TO);
        }

        public String getCreatureName() {
            return creatureName;
        }

        public Builder setCreatureName(String creatureName) {
            this.creatureName = creatureName;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public Builder setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BadSpeakingTargetEvent Build() {
            return new BadSpeakingTargetEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BadSpeakingTargetEvent(Builder builder) {
        super(builder);
        this.creatureName = builder.getCreatureName();
        StringBuilder temp = new StringBuilder("This room does not contain anyone named ");
        if (this.creatureName != null && this.creatureName.length() > 0) {
            temp.append("'").append(this.creatureName).append("'");
        } else {
            temp.append("anything like that");
        }
        temp.append(".  So you are just talking to the air.");
        this.msg = temp.toString();
    }

    public String getCreatureName() {
        return creatureName;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public void buildOutput(RichOutputBuilder builder) {
        if (builder == null) {
            return;
        }
        builder.appendString(this.msg);
    }

}
