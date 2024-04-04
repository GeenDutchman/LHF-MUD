package com.lhf.messages.events;

import java.util.StringJoiner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.Taggable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.item.Usable;
import com.lhf.messages.GameEventType;

public class ItemUsedEvent extends GameEvent {
    public enum UseOutMessageOption {
        OK, USED_UP, NO_USES, REQUIRE_EQUIPPED;
    }

    private final UseOutMessageOption subType;
    private final ICreature itemUser;
    private final Usable usable;
    private final Taggable target;
    private final String message;

    public static class Builder extends GameEvent.Builder<Builder> {
        private UseOutMessageOption subType;
        private ICreature itemUser;
        private Usable usable;
        private Taggable target;
        private String message;

        protected Builder() {
            super(GameEventType.USE);
        }

        public UseOutMessageOption getSubType() {
            return subType;
        }

        public Builder setSubType(UseOutMessageOption subType) {
            this.subType = subType;
            return this;
        }

        public ICreature getItemUser() {
            return itemUser;
        }

        public Builder setItemUser(ICreature itemUser) {
            this.itemUser = itemUser;
            return this;
        }

        public Usable getUsable() {
            return usable;
        }

        public Builder setUsable(Usable usable) {
            this.usable = usable;
            return this;
        }

        public Taggable getTarget() {
            return target;
        }

        public Builder setTarget(Taggable target) {
            this.target = target;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ItemUsedEvent Build() {
            return new ItemUsedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ItemUsedEvent(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.itemUser = builder.getItemUser();
        this.usable = builder.getUsable();
        this.target = builder.getTarget();
        this.message = builder.getMessage();
    }

    public UseOutMessageOption getSubType() {
        return subType;
    }

    public ICreature getItemUser() {
        return itemUser;
    }

    public Usable getUsable() {
        return usable;
    }

    public Taggable getTarget() {
        return target;
    }

    public String getMessage() {
        return message;
    }

    private String printItem() {
        return this.usable != null ? this.usable.getName() : "item";
    }

    @Override
    public String toString() {
        return this.printString();
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        if (this.subType == null) {
            myElement.appendChild(this.addressCreatureXML(nodeGenerator, itemUser, true));
            myElement.appendChild(nodeGenerator.createTextNode(" used this "));
            myElement.appendChild(this.usable != null ? this.usable.buildXMLElement(nodeGenerator)
                    : nodeGenerator.createTextNode("item"));
            if (this.target != null) {
                myElement.appendChild(nodeGenerator.createTextNode(" on "));
                myElement.appendChild(this.target.buildXMLElement(nodeGenerator));
            }
            myElement.appendChild(nodeGenerator.createTextNode("."));
        } else {
            myElement.setAttribute("UseOutMessageOption", this.subType.toString());
            switch (this.subType) {
            case NO_USES:
                myElement.appendChild(nodeGenerator.createTextNode("You cannot use this "));
                myElement.appendChild(this.usable != null ? this.usable.buildXMLElement(nodeGenerator)
                        : nodeGenerator.createTextNode("item"));
                myElement.appendChild(nodeGenerator.createTextNode(" like that!"));
                break;
            case USED_UP:
                myElement.appendChild(nodeGenerator.createTextNode("This "));
                myElement.appendChild(this.usable != null ? this.usable.buildXMLElement(nodeGenerator)
                        : nodeGenerator.createTextNode("item"));
                myElement.appendChild(nodeGenerator.createTextNode(" has been used up."));
                break;
            case REQUIRE_EQUIPPED:
                myElement.appendChild(nodeGenerator.createTextNode("YOu need to have this "));
                myElement.appendChild(this.usable != null ? this.usable.buildXMLElement(nodeGenerator)
                        : nodeGenerator.createTextNode("item"));
                myElement.appendChild(nodeGenerator.createTextNode(" equipped in order to use it!"));
                break;
            case OK:
            default:
                myElement.appendChild(this.addressCreatureXML(nodeGenerator, itemUser, true));
                myElement.appendChild(nodeGenerator.createTextNode(" used this "));
                myElement.appendChild(this.usable != null ? this.usable.buildXMLElement(nodeGenerator)
                        : nodeGenerator.createTextNode("item"));
                if (this.target != null) {
                    myElement.appendChild(nodeGenerator.createTextNode(" on "));
                    myElement.appendChild(this.target.buildXMLElement(nodeGenerator));
                }
                myElement.appendChild(nodeGenerator.createTextNode("."));
            }
        }
        if (this.message != null && !this.message.isBlank()) {
            myElement.appendChild(nodeGenerator.createTextNode(this.message));
        }
        return myElement;
    }

    @Override
    public String printString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.subType == null) {
            sj.add(this.addressCreature(this.itemUser, true)).add("used this")
                    .add(this.printItem() + (this.target != null ? "on " + this.target.getSimpleContent() + "." : "."));
        } else {
            switch (this.subType) {
            case NO_USES:
                sj.add("You cannot use this").add(this.printItem()).add("like that!");
            case USED_UP:
                sj.add("This").add(this.printItem()).add("has been used up.");
            case REQUIRE_EQUIPPED:
                sj.add("You need to have this").add(this.printItem()).add("equipped in order to use it!");
            case OK:
            default:
                sj.add(this.addressCreature(this.itemUser, true)).add("used this").add(
                        this.printItem() + (this.target != null ? "on " + this.target.getSimpleContent() + "." : "."));
            }
        }
        if (this.message != null && !this.message.isBlank()) {
            sj.add(this.getMessage());
        }
        return sj.toString();
    }
}
