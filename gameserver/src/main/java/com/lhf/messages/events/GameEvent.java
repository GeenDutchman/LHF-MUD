package com.lhf.messages.events;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import com.lhf.RichOutput;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventProcessor.GameEventProcessorID;
import com.lhf.messages.GameEventType;

public abstract class GameEvent implements Comparable<GameEvent> {

    public static abstract class Builder<T extends Builder<T>> {
        private GameEventType type;
        private boolean broadcast;
        private Consumer<RichOutputBuilder> outputCallback;
        protected T thisObject;

        protected Builder(GameEventType type) {
            this.type = type;
            this.broadcast = false;
            this.outputCallback = null;
            this.thisObject = this.getThis();
        }

        public GameEventType getType() {
            return this.type;
        }

        public T setBroacast() {
            this.broadcast = true;
            return this.getThis();
        }

        public T setNotBroadcast() {
            this.broadcast = false;
            return this.getThis();
        }

        public boolean isBroadcast() {
            return this.broadcast;
        }

        public Consumer<RichOutputBuilder> getOutputCallback() {
            return outputCallback;
        }

        public T setOutputCallback(Consumer<RichOutputBuilder> xmlCallbackFunction) {
            this.outputCallback = xmlCallbackFunction;
            return this.getThis();
        }

        public abstract T getThis();

        public abstract GameEvent Build();

    }

    protected final static String XML_EVENT_ROOT = "GameEvent";
    protected final static String XML_EVENT_TYPE = "GameEventType";
    protected final static String XML_EVENT_TICK = "GameEventTick";
    protected final static String XML_EVENT_UUID = "GameEventUUID";

    public final static Document documentFromGameEvent(GameEvent event) throws ParserConfigurationException {
        if (event == null) {
            throw new IllegalArgumentException("Cannot generate document from null event!");
        }

        RichOutputBuilder sequence = new RichOutputBuilder(XML_EVENT_ROOT);
        event.buildOutput(sequence);
        Consumer<RichOutputBuilder> callback = event.getOutputCallback();
        if (callback != null) {
            callback.accept(sequence);
        }

        Map<String, String> attributes = new TreeMap<>();
        attributes.put(XML_EVENT_UUID, event.uuid.toString());
        attributes.put(XML_EVENT_TYPE, event.type.toString());
        final TickType tick = event.getTickType();
        if (tick != null) {
            attributes.put(XML_EVENT_TICK, tick.toString());
        }

        return RichOutput.documentFromOutput(sequence.build(), attributes);
    }

    private final GameEventType type;
    private final boolean broadcast;
    private final Builder<?> builder;
    private final UUID uuid;
    private final SortedSet<GameEventProcessorID> haveRecieved;
    private final Consumer<RichOutputBuilder> outputCallback;

    public GameEvent(Builder<?> builder) {
        this.type = builder.getType();
        this.broadcast = builder.isBroadcast();
        this.uuid = UUID.randomUUID();
        this.builder = builder;
        this.haveRecieved = Collections.synchronizedSortedSet(new TreeSet<>());
        this.outputCallback = builder.getOutputCallback();
    }

    /**
     * Checks to see if this is the first time that this message has been sent to
     * the specified Client.
     * 
     * @param client
     * @return true if this is the first time, false otherwise
     */
    public final synchronized boolean isFirstRecieve(GameEventProcessorID client) {
        synchronized (this.haveRecieved) {
            return this.haveRecieved.add(client);
        }
    }

    public Builder<?> copyBuilder() {
        return this.builder;
    }

    public GameEventType getXmlEventType() {
        return this.type;
    }

    public boolean isBroadcast() {
        return this.broadcast;
    }

    public Consumer<RichOutputBuilder> getOutputCallback() {
        return outputCallback;
    }

    protected final RichOutputBuilder addressCreature(RichOutputBuilder builder, ICreature creature) {
        return this.addressCreature(builder, creature, true);
    }

    protected final RichOutputBuilder addressCreature(RichOutputBuilder builder, ICreature creature,
            boolean capitalize) {
        if (!this.isBroadcast()) {
            builder.appendString(capitalize ? "You" : "you");
        } else if (creature != null) {
            builder.appendTaggable(creature);
        } else {
            builder.appendString(capitalize ? "Someone" : "someone");
        }
        return builder;
    }

    protected final RichOutputBuilder possesiveCreature(RichOutputBuilder builder, ICreature creature) {
        return this.possesiveCreature(builder, creature, true);
    }

    protected final RichOutputBuilder possesiveCreature(RichOutputBuilder builder, ICreature creature,
            boolean capitalize) {
        if (!this.isBroadcast()) {
            builder.appendString(capitalize ? "Your" : "your");
        } else if (creature != null) {
            builder.appendTaggable(creature, " ", "'s");
        } else {
            builder.appendString(capitalize ? "Their" : "their");
        }
        return builder;
    }

    public UUID getUuid() {
        return uuid;
    }

    public TickType getTickType() {
        return null;
    }

    // Called to render as a human-readable string
    public String printString() {
        RichOutputBuilder stringOut = new RichOutputBuilder();
        this.buildOutput(stringOut);
        if (this.outputCallback != null) {
            this.outputCallback.accept(stringOut);
        }
        return stringOut.build().printString();
    }

    @Override
    public final String toString() {
        return this.printString();
    }

    public final String printXML() throws ParserConfigurationException, TransformerException {
        StringWriter writer = new StringWriter();
        this.writeXML(writer);
        return writer.toString();
    }

    public final void writeXML(Writer writer) throws ParserConfigurationException, TransformerException {
        Document myDocument = GameEvent.documentFromGameEvent(this);
        RichOutput.writeDocument(myDocument, writer);
    }

    public abstract void buildOutput(RichOutputBuilder builder);

    @Override
    public int hashCode() {
        return Objects.hash(type, uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GameEvent)) {
            return false;
        }
        GameEvent other = (GameEvent) obj;
        return type == other.type && Objects.equals(uuid, other.uuid);
    }

    @Override
    public int compareTo(GameEvent arg0) {
        int runningCompare = this.type.compareTo(arg0.getXmlEventType());
        if (runningCompare != 0) {
            return runningCompare;
        }
        return this.uuid.compareTo(arg0.uuid);
    }

}
