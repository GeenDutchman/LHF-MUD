package com.lhf.messages.events;

import java.io.Writer;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventProcessor.GameEventProcessorID;
import com.lhf.messages.GameEventType;

public abstract class GameEvent implements Comparable<GameEvent> {

    public static abstract class Builder<T extends Builder<T>> {
        private GameEventType type;
        private boolean broadcast;
        protected T thisObject;

        protected Builder(GameEventType type) {
            this.type = type;
            this.broadcast = false;
            this.thisObject = this.getThis();
        }

        // public T setType(OutMessageType type) {
        // this.type = type;
        // return this.getThis();
        // }

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

        public abstract T getThis();

        public abstract GameEvent Build();
    }

    private final GameEventType type;
    private final boolean broadcast;
    private final Builder<?> builder;
    private final UUID uuid;
    private final SortedSet<GameEventProcessorID> haveRecieved;

    public GameEvent(Builder<?> builder) {
        this.type = builder.getType();
        this.broadcast = builder.isBroadcast();
        this.uuid = UUID.randomUUID();
        this.builder = builder;
        this.haveRecieved = Collections.synchronizedSortedSet(new TreeSet<>());
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

    protected String addressCreature(ICreature creature, boolean capitalize) {
        if (!this.isBroadcast()) {
            return capitalize ? "You" : "you";
        } else if (creature != null) {
            return creature.getColorTaggedName();
        } else {
            return capitalize ? "Someone" : "someone";
        }
    }

    protected String possesiveCreature(ICreature creature, boolean capitalize) {
        if (!this.isBroadcast()) {
            return capitalize ? "Your" : "your";
        } else if (creature != null) {
            return creature.getColorTaggedName() + "'s";
        } else {
            return capitalize ? "Their" : "their";
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public TickType getTickType() {
        return null;
    }

    // Called to render as a human-readable string
    public abstract String print();

    protected final static String XML_EVENT_ROOT = "GameEvent";
    protected final static String XML_EVENT_TYPE = "GameEventType";
    protected final static String XML_EVENT_UUID = "GameEventUUID";

    protected final Document getXMLDocumentStart() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        Document document = docFactory.newDocumentBuilder().newDocument();
        Element root = document.createElement(XML_EVENT_ROOT);
        root.setAttribute(XML_EVENT_UUID, this.uuid.toString());
        root.setIdAttribute(XML_EVENT_UUID, true);
        document.appendChild(root);
        Element eventtype = document.createElement(XML_EVENT_TYPE);
        eventtype.setTextContent(this.type.toString());
        root.appendChild(eventtype);
        return document;
    }

    /**
     * Adds this GameEvent to the `document`. If the `document` is null, then this
     * does nothing and returns null. If the `addToMe` parameter is not null, then
     * it will append the created element to `addToMe`. If `addToMe` is null, the
     * new element will be appended to the first child of the `document` if it is
     * present, otherwise it will be added straight to the document. Once finished,
     * it will return the newly created element.
     * 
     * @param document
     * @param addToMe
     */
    public abstract Element buildXML(Document document, Node addToMe);

    public final void getXMLString(Writer writer)
            throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        if (writer == null) {
            return;
        }
        Document document = getXMLDocumentStart();
        this.buildXML(document, document.getFirstChild());
        Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
    }

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
