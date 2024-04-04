package com.lhf.messages.events;

import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilder;
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

import com.lhf.Examinable;
import com.lhf.OutputBuilder;
import com.lhf.Taggable;
import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventProcessor.GameEventProcessorID;
import com.lhf.messages.GameEventType;

public abstract class GameEvent implements Comparable<GameEvent> {

    public static abstract class Builder<T extends Builder<T>> {
        private GameEventType type;
        private boolean broadcast;
        private Function<XMLOutputBuilder, Element> xmlCallbackFunction;
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

        public Function<XMLOutputBuilder, Element> getXmlCallbackFunction() {
            return xmlCallbackFunction;
        }

        public T setXmlCallbackFunction(Function<XMLOutputBuilder, Element> xmlCallbackFunction) {
            this.xmlCallbackFunction = xmlCallbackFunction;
            return this.getThis();
        }

        public abstract T getThis();

        public abstract GameEvent Build();

    }

    public static class XMLOutputBuilder implements OutputBuilder {
        protected final static String XML_EVENT_ROOT = "GameEvent";
        protected final static String XML_EVENT_TYPE = "GameEventType";
        protected final static String XML_EVENT_TICK = "GameEventTick";
        protected final static String XML_EVENT_UUID = "GameEventUUID";
        protected final static String XML_DESCRIPTION = "description";

        private static DocumentBuilderFactory documentBuilderFactory;
        private static DocumentBuilder documentBuilder;

        static {
            XMLOutputBuilder.documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance();
            try {
                XMLOutputBuilder.documentBuilder = documentBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }

        private final Document document;
        private final Element root;

        public static XMLOutputBuilder StartEvent(GameEvent event) {
            return new XMLOutputBuilder(documentBuilder.newDocument(), XML_EVENT_ROOT).fromGameEvent(event);
        }

        private XMLOutputBuilder(Document doc, String rootName) {
            this.document = doc;
            this.root = this.document.createElement(rootName);
            this.document.appendChild(this.root);
        }

        private XMLOutputBuilder fromGameEvent(GameEvent event) {
            if (event != null) {
                this.root.setAttribute(XML_EVENT_UUID, event.uuid.toString());
                root.setIdAttribute(XML_EVENT_UUID, true);
                this.root.setAttribute(XML_EVENT_TYPE, event.type.toString());
                final TickType tick = event.getTickType();
                if (tick != null) {
                    root.setAttribute(XML_EVENT_TICK, tick.toString());
                }
            }
            return this;
        }

        @Override
        public XMLOutputBuilder appendString(String toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.root.appendChild(this.document.createTextNode(before));
                }
                this.root.appendChild(this.document.createTextNode(toAdd));
                if (after != null) {
                    this.root.appendChild(this.document.createTextNode(after));
                }
            }
            return this;
        }

        public Element produceAppendedElement(String elementName) {
            Element myElement = this.document.createElement(elementName);
            this.root.appendChild(myElement);
            return myElement;
        }

        @Override
        public XMLOutputBuilder appendExaminable(Examinable toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.root.appendChild(this.document.createTextNode(before));
                }
                Element myElement = this.produceAppendedElement(toAdd.getTagName());
                final String name = toAdd.getName();
                final String simpleContent = toAdd.getSimpleContent();
                if (name != null && !name.equals(simpleContent)) {
                    Element nameElement = this.document.createElement("name");
                    nameElement.setAttribute("colored", "false");
                    nameElement.appendChild(this.document.createTextNode(name));
                    myElement.appendChild(nameElement);
                }
                if (simpleContent != null && !simpleContent.isEmpty() && !simpleContent.isBlank()) {
                    myElement.appendChild(this.document.createTextNode(simpleContent));
                }
                if (myElement.getChildNodes().getLength() > 1) {
                    myElement.setAttribute("complex", "true");
                }
                final Map<String, String> tagAttributes = toAdd.getTagAttributes();
                if (tagAttributes != null) {
                    for (final Entry<String, String> entry : tagAttributes.entrySet()) {
                        final String key = entry.getKey();
                        final String value = entry.getValue();
                        if (key != null && value != null) {
                            myElement.setAttribute(key, value);
                        }
                    }
                }

                final String description = toAdd.getDescription();
                if (description != null && !description.isEmpty() && !description.isBlank()) {
                    myElement.setAttribute("complex", "true");
                    Element descriptionElement = this.document.createElement(XML_DESCRIPTION);
                    descriptionElement.setAttribute("colored", "true");
                    descriptionElement.appendChild(this.document.createTextNode(description.trim()));
                    myElement.appendChild(descriptionElement);
                }

                // TODO: build description for extra stuff

                if (after != null) {
                    this.root.appendChild(this.document.createTextNode(after));
                }
            }
            return this;
        }

        @Override
        public XMLOutputBuilder appendTaggable(Taggable toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.root.appendChild(this.document.createTextNode(before));
                }
                Element myElement = this.produceAppendedElement(toAdd.getTagName());
                myElement.appendChild(this.document.createTextNode(toAdd.getSimpleContent()));
                final Map<String, String> tagAttributes = toAdd.getTagAttributes();
                if (tagAttributes != null) {
                    for (final Entry<String, String> entry : tagAttributes.entrySet()) {
                        final String key = entry.getKey();
                        final String value = entry.getValue();
                        if (key != null && value != null) {
                            myElement.setAttribute(key, value);
                        }
                    }
                }
                if (after != null) {
                    this.root.appendChild(this.document.createTextNode(after));
                }
            }
            return this;
        }

        public Document getDocument() {
            return this.document;
        }

        public final void getXMLString(Writer writer)
                throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
            if (writer == null) {
                return;
            }

            Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
        }

    }

    private final GameEventType type;
    private final boolean broadcast;
    private final Builder<?> builder;
    private final UUID uuid;
    private final SortedSet<GameEventProcessorID> haveRecieved;
    private final Function<XMLOutputBuilder, Element> xmlCallbackFunction;

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

    protected final OutputBuilder addressCreature(OutputBuilder builder, ICreature creature, boolean capitalize) {
        if (!this.isBroadcast()) {
            builder.appendString(capitalize ? "You" : "you");
        } else if (creature != null) {
            builder.appendTaggable(creature);
        } else {
            builder.appendString(capitalize ? "Someone" : "someone");
        }
        return builder;
    }

    protected final OutputBuilder possesiveCreature(OutputBuilder builder, ICreature creature, boolean capitalize) {
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
    public abstract String printString();

    public abstract void buildOutput(OutputBuilder builder);

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
    public final void buildXML() {
        XMLOutputBuilder builder = XMLOutputBuilder.StartEvent(this);
        this.buildOutput(builder);
        if (this.xmlCallbackFunction != null) {
            this.xmlCallbackFunction.apply(builder);
        }

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
