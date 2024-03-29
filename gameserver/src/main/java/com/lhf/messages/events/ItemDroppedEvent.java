package com.lhf.messages.events;

import java.util.StringJoiner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.Taggable;
import com.lhf.messages.GameEventType;

public class ItemDroppedEvent extends GameEvent {
    private final Taggable item;
    private final String destination;
    private final DropType dropType;

    public enum DropType {
        SUCCESS, NO_ITEM, BAD_CONTAINER, LOCKED_CONTAINER
    }

    public static class Builder extends GameEvent.Builder<Builder> {
        private Taggable item;
        private String destination;
        private DropType dropType;

        protected Builder() {
            super(GameEventType.DROP_OUT);
        }

        public Taggable getItem() {
            return item;
        }

        public Builder setItem(Taggable item) {
            this.item = item;
            return this;
        }

        public String getDestination() {
            return destination;
        }

        public Builder setDestination(String destination) {
            this.destination = destination;
            return this;
        }

        public DropType getDropType() {
            return dropType;
        }

        public Builder setDropType(DropType dropType) {
            this.dropType = dropType;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ItemDroppedEvent Build() {
            return new ItemDroppedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ItemDroppedEvent(Builder builder) {
        super(builder);
        this.item = builder.getItem();
        this.destination = builder.getDestination();
        this.dropType = builder.getDropType();
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public Taggable getItem() {
        return item;
    }

    public String getDestination() {
        return this.destination;
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        myElement.setAttribute("dropType",
                this.dropType != null ? this.dropType.toString() : DropType.SUCCESS.toString());
        if (this.dropType == null) {
            myElement.appendChild(nodeGenerator.createTextNode("You glance at your empty hand as the "));
            myElement.appendChild(this.item != null ? this.item.buildXMLElement(nodeGenerator)
                    : nodeGenerator.createTextNode("item"));
            myElement.appendChild(nodeGenerator.createTextNode(" drops to the floor"));
            if (this.destination != null) {
                myElement.appendChild(nodeGenerator
                        .createTextNode(this.destination.trim().toLowerCase().startsWith("the") ? "of" : "of the"));
                myElement.appendChild(nodeGenerator.createTextNode(this.destination));
            }
            return myElement;
        }
        switch (this.dropType) {
        case BAD_CONTAINER:
            myElement.appendChild(nodeGenerator.createTextNode("You attempted to drop "));
            myElement.appendChild(this.item != null ? this.item.buildXMLElement(nodeGenerator)
                    : nodeGenerator.createTextNode("that"));
            myElement.appendChild(
                    nodeGenerator.createTextNode(String.format(" into an unrecognized container or source%s.",
                            this.destination != null ? ":" + this.destination : "")));

            return myElement;
        case LOCKED_CONTAINER:
            myElement.appendChild(nodeGenerator.createTextNode("You attempted to drop "));
            myElement.appendChild(this.item != null ? this.item.buildXMLElement(nodeGenerator)
                    : nodeGenerator.createTextNode("that"));
            String destName = "some container";
            if (this.destination != null) {
                if (this.destination.trim().toLowerCase().startsWith("the")) {
                    destName = "the '" + this.destination + "'";
                } else {
                    destName = "'" + this.destination + "'";
                }
            }
            myElement.appendChild(nodeGenerator.createTextNode(String.format("into %s but it is locked.", destName)));

            return myElement;
        case NO_ITEM:
            myElement.appendChild(nodeGenerator.createTextNode("You failed to name an item to drop."));
            return myElement;
        case SUCCESS:
        default:
            myElement.appendChild(nodeGenerator.createTextNode("You glance at your empty hand as the "));
            myElement.appendChild(this.item != null ? this.item.buildXMLElement(nodeGenerator)
                    : nodeGenerator.createTextNode("item"));
            myElement.appendChild(nodeGenerator.createTextNode(" drops to the floor"));
            if (this.destination != null) {
                myElement.appendChild(nodeGenerator
                        .createTextNode(this.destination.trim().toLowerCase().startsWith("the") ? "of" : "of the"));
                myElement.appendChild(nodeGenerator.createTextNode(this.destination));
            }
            return myElement;
        }
    }

    @Override
    public String printString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.dropType == null) {
            sj.add("You glance at your empty hand as the");
            if (this.item != null) {
                sj.add(this.item.getSimpleContent());
            } else {
                sj.add("item");
            }
            sj.add("drops to the floor");
            if (this.destination != null) {
                sj.add(this.destination.trim().toLowerCase().startsWith("the") ? "of" : "of the").add(this.destination);
            }
            return sj.toString() + ".";
        }
        switch (this.dropType) {
        case BAD_CONTAINER:
            sj.add("You attempted to drop");
            if (this.item != null) {
                sj.add("'" + this.item.getSimpleContent() + "'");
            } else {
                sj.add("that");
            }
            if (this.destination != null) {
                sj.add("into an unrecognized container or source: " + this.destination + ".");
            } else {
                sj.add("into an unrecognized container or source.");
            }
            sj.add("\n");
            return sj.toString();
        case LOCKED_CONTAINER:
            sj.add("You attempted to drop");
            if (this.item != null) {
                sj.add("'" + this.item.getSimpleContent() + "'");
            } else {
                sj.add("that");
            }
            sj.add("into");
            if (this.destination != null) {
                sj.add(this.destination.trim().toLowerCase().startsWith("the") ? "'" + this.destination + "'"
                        : "the '" + this.destination + "'");
            } else {
                sj.add("some container");
            }
            sj.add("but it is locked.");
            sj.add("\n");
            return sj.toString();
        case NO_ITEM:
            sj.add("You failed to name an item to drop.");
            return sj.toString();
        case SUCCESS:
        default:
            sj.add("You glance at your empty hand as the");
            if (this.item != null) {
                sj.add(this.item.getSimpleContent());
            } else {
                sj.add("item");
            }
            sj.add("drops to the floor");
            if (this.destination != null) {
                sj.add(this.destination.trim().toLowerCase().startsWith("the") ? "of" : "of the").add(this.destination);
            }
            return sj.toString() + ".";
        }
    }

    public DropType getDropType() {
        return dropType;
    }
}
