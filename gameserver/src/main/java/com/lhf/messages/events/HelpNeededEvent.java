package com.lhf.messages.events;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.messages.GameEventType;
import com.lhf.messages.in.AMessageType;

public class HelpNeededEvent extends GameEvent {

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends GameEvent.Builder<T> {

        private SortedMap<AMessageType, String> helps = new TreeMap<>();
        private AMessageType singleHelp;

        protected AbstractBuilder() {
            super(GameEventType.HELP);
        }

        protected AbstractBuilder(GameEventType type) {
            super(type);
        }

        // this stores a copy of helps;
        public T setHelps(Map<AMessageType, String> helps) {
            this.helps = helps != null ? new TreeMap<>(helps) : new TreeMap<>();
            return this.getThis();
        }

        public T removeHelp(AMessageType commandMessage) {
            if (this.helps != null && commandMessage != null) {
                this.helps.remove(commandMessage);
            }
            return this.getThis();
        }

        public T addHelp(AMessageType commandMessage, String description) {
            if (this.helps == null) {
                this.helps = new TreeMap<>();
            }
            this.helps.put(commandMessage, description);
            return this.getThis();
        }

        public Map<AMessageType, String> getConcreteHelps() {
            return Collections.unmodifiableSortedMap(this.helps);
        }

        public T setSingleHelp(AMessageType commandMessage) {
            this.singleHelp = commandMessage;
            return this.getThis();
        }

        public AMessageType getSingleHelp() {
            return this.singleHelp;
        }

    }

    public static class Builder extends AbstractBuilder<Builder> {
        protected Builder() {
            super();
        }

        @Override
        public HelpNeededEvent Build() {
            return new HelpNeededEvent(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }
    }

    private final Map<AMessageType, String> helps;
    private final AMessageType singleHelp;

    public static Builder getHelpBuilder() {
        return new Builder();
    }

    protected HelpNeededEvent(AbstractBuilder<?> builder) {
        super(builder);
        this.helps = builder.getConcreteHelps();
        this.singleHelp = builder.getSingleHelp();
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public Map<AMessageType, String> getHelps() {
        return helps;
    }

    public AMessageType getSingleHelp() {
        return singleHelp;
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        if (this.singleHelp != null && this.helps.containsKey(this.singleHelp)) {
            Element helpElement = nodeGenerator.createElement("help");
            helpElement.setAttribute("command", this.singleHelp.toString());
            helpElement.setIdAttribute("command", true);
            helpElement.appendChild(this.singleHelp.buildXMLElement(nodeGenerator));
            Element helpDescription = nodeGenerator.createElement("description");
            helpDescription.appendChild(nodeGenerator.createTextNode(this.helps.get(this.singleHelp)));
            helpElement.appendChild(helpDescription);
            myElement.appendChild(helpElement);
        } else {
            for (AMessageType cmdMsg : this.helps.keySet()) {
                Element helpElement = nodeGenerator.createElement("help");
                helpElement.setAttribute("command", cmdMsg.toString());
                helpElement.setIdAttribute("command", true);
                helpElement.appendChild(cmdMsg.buildXMLElement(nodeGenerator));
                Element helpDescription = nodeGenerator.createElement("description");
                helpDescription.appendChild(nodeGenerator.createTextNode(this.helps.get(cmdMsg)));
                helpElement.appendChild(helpDescription);
                myElement.appendChild(helpElement);
            }
        }
        return myElement;
    }

    @Override
    public String printString() {
        StringBuilder sb = new StringBuilder();
        if (this.singleHelp != null && this.helps.containsKey(this.singleHelp)) {
            sb.append(this.singleHelp).append(":").append("\r\n").append(this.helps.get(this.singleHelp))
                    .append("\r\n");
        } else {
            for (AMessageType cmdMsg : this.helps.keySet()) {
                sb.append(cmdMsg).append(":").append("\r\n").append(helps.get(cmdMsg)).append("\r\n");
            }
        }
        return sb.toString();
    }
}
