package com.lhf.messages.out;

import com.lhf.messages.GameEventType;

public class CannotSpeakToTargetEvent extends GameEvent {
    private final String creatureName;
    private final String taggedCreatureName;
    private final String msg;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String creatureName;
        private String taggedCreatureName;
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

        public String getTaggedCreatureName() {
            return taggedCreatureName;
        }

        public Builder setTaggedCreatureName(String taggedCreatureName) {
            this.taggedCreatureName = taggedCreatureName;
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
        public CannotSpeakToTargetEvent Build() {
            return new CannotSpeakToTargetEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public CannotSpeakToTargetEvent(Builder builder) {
        super(builder);
        this.creatureName = builder.getCreatureName();
        this.taggedCreatureName = builder.getTaggedCreatureName();
        StringBuilder temp = new StringBuilder("This room does not contain anyone named ");
        if (this.taggedCreatureName != null && this.taggedCreatureName.length() > 0) {
            temp.append(this.taggedCreatureName);
        } else if (this.creatureName != null && this.creatureName.length() > 0) {
            temp.append("'").append(this.creatureName).append("'");
        } else {
            temp.append("anything like that");
        }
        temp.append(".  So you are just talking to the air.");
        this.msg = temp.toString();
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

    @Override
    public String print() {
        return this.msg;
    }
}
