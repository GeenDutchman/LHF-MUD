package com.lhf.messages.events;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.lhf.game.TickType;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.AItem;
import com.lhf.game.item.IItem;
import com.lhf.messages.GameEventType;

public class ItemUnequippedEvent extends GameEvent {
    public enum UnequipResultType {
        SUCCESS, ITEM_NOT_EQUIPPED, ITEM_NOT_FOUND;
    }

    private final static TickType tickType = TickType.ACTION;

    private final UnequipResultType subType;
    private final AItem item;
    private final EquipmentSlots slot;
    private final String attemptedName;

    public static class Builder extends GameEvent.Builder<Builder> {
        private UnequipResultType subType;
        private AItem item;
        private EquipmentSlots slot;
        private String attemptedName;

        protected Builder() {
            super(GameEventType.UNEQUIP);
        }

        public AItem getItem() {
            return item;
        }

        public Builder setItem(AItem item) {
            this.item = item;
            return this;
        }

        public EquipmentSlots getSlot() {
            return slot;
        }

        public Builder setSlot(EquipmentSlots slot) {
            this.slot = slot;
            return this;
        }

        public String getAttemptedName() {
            return attemptedName;
        }

        public Builder setAttemptedName(String attemptedName) {
            this.attemptedName = attemptedName;
            return this;
        }

        public UnequipResultType getSubType() {
            return subType;
        }

        public Builder setSubType(UnequipResultType subType) {
            this.subType = subType;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ItemUnequippedEvent Build() {
            return new ItemUnequippedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ItemUnequippedEvent(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.item = builder.getItem();
        this.slot = builder.getSlot();
        this.attemptedName = builder.getAttemptedName();
    }

    private String describeItem() {
        if (this.item != null) {
            return this.item.getName();
        } else if (this.attemptedName != null && !this.attemptedName.isBlank()) {
            return this.attemptedName;
        } else {
            return "item";
        }
    }

    private Node describeItemXML(Document nodeGenerator) {
        if (this.item == null) {
            return this.item.buildXMLElement(nodeGenerator);
        } else if (this.attemptedName != null && !this.attemptedName.isBlank()) {
            return nodeGenerator.createTextNode(this.attemptedName);
        } else {
            return nodeGenerator.createTextNode("item");
        }
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public IItem getItem() {
        return item;
    }

    public EquipmentSlots getSlot() {
        return slot;
    }

    public String getAttemptedName() {
        return attemptedName;
    }

    @Override
    public TickType getTickType() {
        return tickType;
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        myElement.setAttribute("UnequipSubType", this.subType != null ? this.subType.toString() : "null");
        if (this.isBroadcast()) {
            myElement.appendChild(nodeGenerator.createTextNode(String.format("Someone %s an item",
                    UnequipResultType.SUCCESS.equals(this.subType) ? "has unequipped" : "attempted to unequip")));
            return myElement;

        }
        if (this.subType == null) {
            myElement.appendChild(nodeGenerator.createTextNode("You tried to unequip an item "));
            if (this.attemptedName != null && !this.attemptedName.isBlank()) {
                myElement.appendChild(
                        nodeGenerator.createTextNode(String.format("with the name of %s ", this.attemptedName)));
            }
            if (this.item != null) {
                myElement.appendChild(nodeGenerator.createTextNode("and an item "));
                myElement.appendChild(this.item.buildXMLElement(nodeGenerator));
                myElement.appendChild(nodeGenerator.createTextNode(" was found"));
            }
        } else {
            switch (this.subType) {
            case SUCCESS:
                myElement.appendChild(nodeGenerator.createTextNode("You have unequipped your "));
                myElement.appendChild(this.describeItemXML(nodeGenerator));
                break;
            case ITEM_NOT_EQUIPPED:
                myElement.appendChild(nodeGenerator.createTextNode("Your "));
                myElement.appendChild(this.describeItemXML(nodeGenerator));
                myElement.appendChild(nodeGenerator.createTextNode(" is not equipped"));
                break;
            case ITEM_NOT_FOUND:
                myElement.appendChild(nodeGenerator.createTextNode("That "));
                myElement.appendChild(this.describeItemXML(nodeGenerator));
                myElement.appendChild(nodeGenerator.createTextNode(" was not found"));
                break;
            default:
                myElement.appendChild(nodeGenerator.createTextNode("You tried to unequip an item "));
                if (this.attemptedName != null && !this.attemptedName.isBlank()) {
                    myElement.appendChild(
                            nodeGenerator.createTextNode(String.format("with the name of %s ", this.attemptedName)));
                }
                if (this.item != null) {
                    myElement.appendChild(nodeGenerator.createTextNode("and an item "));
                    myElement.appendChild(this.item.buildXMLElement(nodeGenerator));
                    myElement.appendChild(nodeGenerator.createTextNode(" was found"));
                }
                break;
            }
        }
        if (this.slot != null) {
            myElement.appendChild(nodeGenerator.createTextNode(" in your "));
            myElement.appendChild(this.slot.buildXMLElement(nodeGenerator));
            myElement.appendChild(nodeGenerator.createTextNode(" equipment slot"));
        }
        myElement.appendChild(nodeGenerator.createTextNode("."));
        return myElement;
    }

    @Override
    public String printString() {
        StringBuilder sb = new StringBuilder();
        if (this.isBroadcast()) {
            sb.append("Someone ");
            if (this.subType == UnequipResultType.SUCCESS) {
                sb.append("has unequipped");
            } else {
                sb.append("attempted to unequip");
            }
            sb.append("an item.");
            return sb.toString();
        }
        if (this.subType == null) {
            sb.append("You tried to unequip an item ");
            if (this.attemptedName != null && !this.attemptedName.isBlank()) {
                sb.append("with the name of ").append(this.attemptedName);
            }
            if (this.item != null) {
                sb.append("and an item ").append(this.item.getName()).append(" was found");
            }
        } else {
            switch (this.subType) {
            case SUCCESS:
                sb.append("You have unequipped your ").append(this.describeItem());
                break;
            case ITEM_NOT_EQUIPPED:
                sb.append("Your ").append(this.describeItem()).append(" is not equipped");
                break;
            case ITEM_NOT_FOUND:
                sb.append("That ").append(this.describeItem()).append(" was not found");
                break;
            default:
                sb.append("You tried to unequip an item ");
                if (this.attemptedName != null && !this.attemptedName.isBlank()) {
                    sb.append("with the name of ").append(this.attemptedName);
                }
                if (this.item != null) {
                    sb.append("and an item ").append(this.item.getName()).append(" was found");
                }
                break;
            }
        }
        if (this.slot != null) {
            sb.append(" in your ").append(this.slot).append(" equipment slot");
        }
        sb.append(".");
        return sb.toString();
    }
}
