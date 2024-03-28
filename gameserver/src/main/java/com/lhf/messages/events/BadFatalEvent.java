package com.lhf.messages.events;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.messages.GameEventType;

public class BadFatalEvent extends GameEvent {

    private final String extraInfo;
    private final Exception exception;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String extraInfo;
        private Exception exception;

        protected Builder() {
            super(GameEventType.FATAL);
        }

        public String getExtraInfo() {
            return extraInfo;
        }

        public Builder setExtraInfo(String extraInfo) {
            this.extraInfo = extraInfo;
            return this;
        }

        public Exception getException() {
            return exception;
        }

        public Builder setException(Exception exception) {
            this.exception = exception;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BadFatalEvent Build() {
            return new BadFatalEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    // no nonsense constructor
    public BadFatalEvent() {
        super(new Builder());
        this.extraInfo = null;
        this.exception = null;
    }

    public BadFatalEvent(Builder builder) {
        super(builder);
        this.extraInfo = builder.getExtraInfo();
        this.exception = builder.getException();
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        if (nodeGenerator == null) {
            return null;
        }
        Element myElement = this.produceContentNode(nodeGenerator);
        myElement.appendChild(nodeGenerator.createTextNode("You made a fatal mistake"));
        if (this.extraInfo != null && !this.extraInfo.isBlank()) {
            myElement.appendChild(nodeGenerator.createTextNode(":" + this.extraInfo));
        }
        if (this.exception != null) {
            Element exceptElement = nodeGenerator.createElement("Exception");
            exceptElement.appendChild(nodeGenerator.createTextNode(this.exception.toString()));
            myElement.appendChild(exceptElement);
        }
        return myElement;
    }

    @Override
    public String printString() {
        StringBuilder sb = new StringBuilder("You made a fatal mistake");
        if (this.extraInfo != null && !this.extraInfo.isBlank()) {
            sb.append(":").append(this.extraInfo);
        }
        sb.append("\n");
        sb.append("MessageUUID:").append(this.getUuid()).append("\n");
        if (this.exception != null) {
            sb.append("Error:").append(this.exception.toString());
        }
        return sb.toString();
    }

}
