package com.lhf.messages.events;

import java.io.Writer;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;

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
        private Function<Document, Element> xmlCallbackFunction;
        protected T thisObject;

        protected Builder(GameEventType type) {
            this.type = type;
            this.broadcast = false;
            this.xmlCallbackFunction = null;
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

        public Function<Document, Element> getXmlCallbackFunction() {
            return xmlCallbackFunction;
        }

        public T setXmlCallbackFunction(Function<Document, Element> xmlCallbackFunction) {
            this.xmlCallbackFunction = xmlCallbackFunction;
            return this.getThis();
        }

        public abstract T getThis();

        public abstract GameEvent Build();

    }

    private final GameEventType type;
    private final boolean broadcast;
    private final Builder<?> builder;
    private final UUID uuid;
    private final SortedSet<GameEventProcessorID> haveRecieved;
    private final Function<Document, Element> xmlCallbackFunction;

    public GameEvent(Builder<?> builder) {
        this.type = builder.getType();
        this.broadcast = builder.isBroadcast();
        this.uuid = UUID.randomUUID();
        this.builder = builder;
        this.haveRecieved = Collections.synchronizedSortedSet(new TreeSet<>());
        this.xmlCallbackFunction = builder.getXmlCallbackFunction();
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
            return creature.getName();
        } else {
            return capitalize ? "Someone" : "someone";
        }
    }

    protected String possesiveCreature(ICreature creature, boolean capitalize) {
        if (!this.isBroadcast()) {
            return capitalize ? "Your" : "your";
        } else if (creature != null) {
            return creature.getName() + "'s";
        } else {
            return capitalize ? "Their" : "their";
        }
    }

    protected Node addressCreatureXML(Document nodeGenerator, ICreature creature, boolean capitalize) {
        if (nodeGenerator == null) {
            return null;
        }
        if (!this.isBroadcast()) {
            return nodeGenerator.createTextNode(capitalize ? "You" : "you");
        } else if (creature != null) {
            return creature.buildXMLElement(nodeGenerator);
        } else {
            return nodeGenerator.createTextNode(capitalize ? "Someone" : "someone");
        }
    }

    protected Node posessiveCreatureXML(Document nodeGenerator, ICreature creature, boolean capitalize) {
        if (nodeGenerator == null) {
            return null;
        }
        if (!this.isBroadcast()) {
            return nodeGenerator.createTextNode(capitalize ? "Your" : "your");
        } else if (creature != null) {
            Element named = creature.buildXMLElement(nodeGenerator);
            named.setAttribute("complex", "true");
            named.appendChild(nodeGenerator.createTextNode("'s"));
            return named;
        } else {
            return nodeGenerator.createTextNode(capitalize ? "Their" : "their");
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public TickType getTickType() {
        return null;
    }

    // Called to render as a human-readable string
    public abstract String printString();

    protected final static String XML_EVENT_ROOT = "GameEvent";
    protected final static String XML_EVENT_TYPE = "GameEventType";
    protected final static String XML_EVENT_TICK = "GameEventTick";
    protected final static String XML_EVENT_UUID = "GameEventUUID";
    protected final static String XML_EVENT_CONTENT = "GameEventContent";

    protected final Document getXMLDocumentStart() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        Document document = docFactory.newDocumentBuilder().newDocument();
        Element root = document.createElement(XML_EVENT_ROOT);
        root.setAttribute(XML_EVENT_UUID, this.uuid.toString());
        root.setIdAttribute(XML_EVENT_UUID, true);
        root.setAttribute(XML_EVENT_TYPE, this.type.toString());
        final TickType tick = this.getTickType();
        if (tick != null) {
            root.setAttribute(XML_EVENT_TICK, tick.toString());
        }
        document.appendChild(root);
        Element eventtype = document.createElement(XML_EVENT_TYPE);
        eventtype.setTextContent(this.type.toString());
        root.appendChild(eventtype);
        return document;
    }

    protected final Element produceContentNode(Document nodeGenerator) {
        if (nodeGenerator == null) {
            return null;
        }
        return nodeGenerator.createElement(XML_EVENT_CONTENT);
    }

    public abstract Element buildXMLElement(Document nodeGenerator);

    /**
     * Adds this GameEvent to the `document`. If the `document` is null, then this
     * does nothing and returns null. If the `addToMe` parameter is not null, then
     * it will append the created element to `addToMe`. If `addToMe` is null, the
     * new element will be appended to the first child of the `document` if it is
     * present, otherwise it will be added straight to the document. Once finished,
     * it will return the newly created element.
     * 
     * @param nodeGenerator
     * @param addToMe
     */
    public final void buildXML(Document nodeGenerator, Node addToMe) {
        if (nodeGenerator == null) {
            return;
        }
        Element myElement = this.buildXMLElement(nodeGenerator);
        if (this.xmlCallbackFunction != null) {
            Element builtElement = this.xmlCallbackFunction.apply(nodeGenerator);
            if (builtElement != null) {
                myElement.appendChild(builtElement);
            }
        }
        if (addToMe != null) {
            addToMe.appendChild(myElement);
        } else {
            Node first = nodeGenerator.getFirstChild();
            if (first != null) {
                first.appendChild(myElement);
            } else {
                nodeGenerator.appendChild(myElement);
            }
        }
    }

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
