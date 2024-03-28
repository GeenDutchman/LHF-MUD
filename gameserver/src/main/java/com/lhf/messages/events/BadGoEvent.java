package com.lhf.messages.events;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.StringJoiner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.game.map.Directions;
import com.lhf.messages.GameEventType;

public class BadGoEvent extends GameEvent {
    public enum BadGoType {
        DNE, BLOCKED, NO_ROOM;
    }

    private final BadGoType subType;
    private final Directions attempted;
    private final Collection<Directions> available;

    public static class Builder extends GameEvent.Builder<Builder> {
        private BadGoType subType;
        private Directions attempted;
        private Collection<Directions> available = EnumSet.noneOf(Directions.class);

        protected Builder() {
            super(GameEventType.BAD_GO);
        }

        protected Builder(BadGoType type) {
            super(GameEventType.BAD_GO);
            this.subType = type;
        }

        public BadGoType getSubType() {
            return subType;
        }

        public Builder setSubType(BadGoType type) {
            this.subType = type;
            return this;
        }

        public Directions getAttempted() {
            return attempted;
        }

        public Builder setAttempted(Directions attempted) {
            this.attempted = attempted;
            return this;
        }

        public Collection<Directions> getAvailable() {
            return Collections.unmodifiableCollection(available);
        }

        public Builder setAvailable(Collection<Directions> available) {
            this.available = available != null ? available : EnumSet.noneOf(Directions.class);
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BadGoEvent Build() {
            return new BadGoEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    protected BadGoEvent(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.attempted = builder.getAttempted();
        this.available = builder.getAvailable();
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public Directions getAttempted() {
        return attempted;
    }

    public Collection<Directions> getAvailable() {
        return available;
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        myElement.setAttribute("complex", "true");
        myElement.appendChild(nodeGenerator.createTextNode("You cannot go "));
        if (this.attempted != null) {
            myElement.appendChild(this.attempted.buildXMLElement(nodeGenerator));
            myElement.appendChild(nodeGenerator.createTextNode(". "));
        } else {
            myElement.appendChild(nodeGenerator.createTextNode("that way. "));
        }
        if (this.subType == BadGoType.DNE || this.attempted == null) {
            myElement.appendChild(nodeGenerator.createTextNode("That way is a wall. "));
        } else if (this.subType == BadGoType.BLOCKED) {
            myElement.appendChild(nodeGenerator.createTextNode("Your path is blocked "));
        } else if (this.subType == BadGoType.NO_ROOM) {
            myElement.appendChild(nodeGenerator.createTextNode("You are not in a room. "));
        }
        if (this.available != null && this.available.size() > 0) {
            if (this.available.size() == 1 && this.attempted != null && this.subType == BadGoType.BLOCKED) {
                myElement.appendChild(nodeGenerator
                        .createTextNode("No other directions are available.  Try finding a way to unblock it. "));
            } else {
                Element available = nodeGenerator.createElement("AvailableDirections");
                available.setAttribute("complex", "true");
                available.setAttribute("colored", "false");
                available.appendChild(nodeGenerator.createTextNode("You could try to go one of:"));
                for (Directions s : this.available) {
                    if (!(this.subType == BadGoType.BLOCKED && s.equals(this.attempted))) {
                        available.appendChild(s.buildXMLElement(nodeGenerator));
                    }
                }
                myElement.appendChild(available);
            }
        } else {
            myElement.appendChild(nodeGenerator.createTextNode("No directions are available."));
        }
        return myElement;
    }

    @Override
    public String printString() {
        StringBuilder sb = new StringBuilder();
        sb.append("You cannot go ");
        if (this.attempted != null) {
            sb.append(this.attempted.toString());
        } else {
            sb.append("that way");
        }
        sb.append(". ");
        if (this.subType == BadGoType.DNE || this.attempted == null) {
            sb.append("That way is a wall. ");
        } else if (this.subType == BadGoType.BLOCKED) {
            sb.append("Your path is blocked ");
        } else if (this.subType == BadGoType.NO_ROOM) {
            sb.append("You are not in a room. ");
        }
        if (this.available != null && this.available.size() > 0) {
            if (this.available.size() == 1 && this.attempted != null && this.subType == BadGoType.BLOCKED) {
                sb.append("No other directions are available.  Try finding a way to unblock it. ");
            } else {
                sb.append("You could try to go one of:");
                StringJoiner sj = new StringJoiner(", ");
                for (Directions s : this.available) {
                    if (!(this.subType == BadGoType.BLOCKED && s.equals(this.attempted))) {
                        sj.add(s.toString());
                    }
                }
                sb.append(sj.toString());
            }
        } else {
            sb.append("No directions are available.");
        }
        return sb.toString();
    }

}
