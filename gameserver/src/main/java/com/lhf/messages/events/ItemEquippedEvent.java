package com.lhf.messages.events;

import java.util.List;
import java.util.StringJoiner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.lhf.game.TickType;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.AItem;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.IItem;
import com.lhf.messages.GameEventType;

public class ItemEquippedEvent extends GameEvent {
    public enum EquipResultType {
        SUCCESS, BADSLOT, NOTEQUIPBLE;
    }

    private final static TickType tickType = TickType.ACTION;
    private final EquipResultType subType;
    private final AItem item;
    private final String attemptedItemName;
    private final EquipmentSlots attemptedSlot;

    public static class Builder extends GameEvent.Builder<Builder> {
        private EquipResultType subType;
        private AItem item;
        private String attemptedItemName;
        private EquipmentSlots attemptedSlot;

        protected Builder() {
            super(GameEventType.EQUIP);
        }

        public EquipResultType getSubType() {
            return subType;
        }

        public Builder setSubType(EquipResultType type) {
            this.subType = type;
            return this;
        }

        public AItem getItem() {
            return item;
        }

        public Builder setItem(AItem item) {
            this.item = item;
            return this;
        }

        public String getAttemptedItemName() {
            return attemptedItemName;
        }

        public Builder setAttemptedItemName(String attemptedItemName) {
            this.attemptedItemName = attemptedItemName;
            return this;
        }

        public EquipmentSlots getAttemptedSlot() {
            return attemptedSlot;
        }

        public Builder setAttemptedSlot(EquipmentSlots attemptedSlot) {
            this.attemptedSlot = attemptedSlot;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ItemEquippedEvent Build() {
            return new ItemEquippedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ItemEquippedEvent(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.item = builder.getItem();
        this.attemptedItemName = builder.getAttemptedItemName();
        this.attemptedSlot = builder.getAttemptedSlot();
    }

    private String printItemName(String defaultItemName) {
        if (this.item != null) {
            return this.item.getName();
        } else if (this.attemptedItemName != null && !this.attemptedItemName.isBlank()) {
            return "'" + this.attemptedItemName + "'";
        } else if (defaultItemName != null && !defaultItemName.isBlank()) {
            return defaultItemName;
        } else {
            return "item";
        }
    }

    private Node printItemNameXML(Document nodeGenerator, String defaultItemName) {
        if (this.item != null) {
            return this.item.buildXMLElement(nodeGenerator);
        } else if (this.attemptedItemName != null && !this.attemptedItemName.isBlank()) {
            return nodeGenerator.createTextNode("'" + this.attemptedItemName + "'");
        } else if (defaultItemName != null && !defaultItemName.isBlank()) {
            return nodeGenerator.createTextNode(defaultItemName);
        } else {
            return nodeGenerator.createTextNode("item");
        }
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
        myElement.setAttribute("EquipResultType",
                this.subType != null ? this.subType.toString() : EquipResultType.SUCCESS.toString());
        if (this.isBroadcast()) {
            myElement.appendChild(nodeGenerator.createTextNode(String.format("Someone %s an item.",
                    this.getSubType() == EquipResultType.SUCCESS ? "equipped" : "attempted to equip")));
            return myElement;
        }
        if (this.subType == null) {
            myElement.appendChild(nodeGenerator.createTextNode(String.format("You searched to equip %s",
                    this.attemptedItemName != null ? "'" + this.attemptedItemName + "'" : "an item")));
            if (this.attemptedSlot != null) {
                myElement.appendChild(nodeGenerator.createTextNode(" to your "));
                myElement.appendChild(this.attemptedSlot.buildXMLElement(nodeGenerator));
                myElement.appendChild(nodeGenerator.createTextNode(" equipment slot"));
            }
            if (this.item != null) {
                myElement.appendChild(nodeGenerator.createTextNode(", and found "));
                myElement.appendChild(this.item.buildDetailedXMLElement(nodeGenerator));
                if (this.attemptedSlot != null) {
                    myElement.appendChild(nodeGenerator.createTextNode(" and you equipped it."));
                }
            } else {
                myElement.appendChild(nodeGenerator.createTextNode(" but did not find such in your inventory. "));
            }
            return myElement;
        }
        switch (this.subType) {
        case SUCCESS:
            myElement.appendChild(nodeGenerator.createTextNode("You successfully equipped your "));
            myElement.appendChild(this.printItemNameXML(nodeGenerator, null));
            if (this.attemptedSlot != null) {
                myElement.appendChild(nodeGenerator.createTextNode(" to your "));
                myElement.appendChild(this.attemptedSlot.buildXMLElement(nodeGenerator));
                myElement.appendChild(nodeGenerator.createTextNode(" equipment slot"));
            }
            myElement.appendChild(nodeGenerator.createTextNode("."));
            break;
        case BADSLOT:
            myElement.appendChild(this.attemptedSlot != null ? this.attemptedSlot.buildXMLElement(nodeGenerator)
                    : nodeGenerator.createTextNode("That slot"));

            myElement.appendChild(nodeGenerator.createTextNode(" is not an appropriate slot for equipping "));
            myElement.appendChild(this.printItemNameXML(nodeGenerator, "that item"));
            myElement.appendChild(nodeGenerator.createTextNode("."));
            if (this.item != null && this.getCorrectSlots().size() > 0) {
                myElement.appendChild(nodeGenerator.createTextNode("You can equip it to: "));
                for (EquipmentSlots slots : this.getCorrectSlots()) {
                    myElement.appendChild(slots.buildXMLElement(nodeGenerator));
                }
            }
            break;
        case NOTEQUIPBLE:
            myElement.appendChild(this.printItemNameXML(nodeGenerator, "that item"));
            myElement.appendChild(nodeGenerator.createTextNode(" is not equippable!"));
            break;
        default:
            myElement.appendChild(nodeGenerator.createTextNode(String.format("You searched to equip %s",
                    this.attemptedItemName != null ? "'" + this.attemptedItemName + "'" : "an item")));
            if (this.attemptedSlot != null) {
                myElement.appendChild(nodeGenerator.createTextNode(" to your "));
                myElement.appendChild(this.attemptedSlot.buildXMLElement(nodeGenerator));
                myElement.appendChild(nodeGenerator.createTextNode(" equipment slot"));
            }
            if (this.item != null) {
                myElement.appendChild(nodeGenerator.createTextNode(", and found "));
                myElement.appendChild(this.item.buildDetailedXMLElement(nodeGenerator));
                if (this.attemptedSlot != null) {
                    myElement.appendChild(nodeGenerator.createTextNode(" and you equipped it."));
                }
            } else {
                myElement.appendChild(nodeGenerator.createTextNode(" but did not find such in your inventory. "));
            }
            break;
        }
        return myElement;
    }

    @Override
    public String printString() {
        StringBuilder sb = new StringBuilder();
        if (this.isBroadcast()) {
            sb.append("Someone ");
            if (this.getSubType() == EquipResultType.SUCCESS) {
                sb.append("equipped ");
            } else {
                sb.append("attempted to equip ");
            }
            sb.append("an item.");
            return sb.toString();
        }
        if (this.subType == null) {
            sb.append("You searched to equip ");
            if (this.attemptedItemName != null && this.attemptedItemName.length() > 0) {
                sb.append("'").append(this.attemptedItemName).append("' ");
            } else {
                sb.append("an item ");
            }
            if (this.attemptedSlot != null) {
                sb.append("to your ").append(this.attemptedSlot).append(" equipment slot ");
            }
            if (this.item != null) {
                sb.append(", and found ").append(this.item.getName()).append(" ");
                if (this.item instanceof Equipable) {
                    sb.append("which could equip to any of these slots: ");
                    StringJoiner sj = new StringJoiner(", ");
                    for (EquipmentSlots slots : this.getCorrectSlots()) {
                        sj.add(slots.toString());
                    }
                    sb.append(sj.toString()).append(". ");
                    if (this.attemptedSlot != null) {
                        sb.append("And you equipped it.");
                    }
                }
            } else {
                sb.append(" but did not find such in your inventory. ");
            }
            return sb.toString();
        }
        switch (this.subType) {
        case SUCCESS:
            sb.append("You successfully equipped your ").append(this.printItemName(null));
            if (this.attemptedSlot != null) {
                sb.append(" to your ").append(this.attemptedSlot).append(" equiment slot");
            }
            sb.append(".");
            break;
        case BADSLOT:
            if (this.attemptedSlot != null) {
                sb.append(this.attemptedSlot);
            } else {
                sb.append("That slot");
            }
            sb.append(" is not an appropriate slot for equipping ");
            sb.append(this.printItemName("that item"));
            sb.append(".");
            if (this.item != null && this.getCorrectSlots().size() > 0) {
                sb.append("You can equip it to: ");
                StringJoiner sj = new StringJoiner(", ");
                for (EquipmentSlots slots : this.getCorrectSlots()) {
                    sj.add(slots.toString());
                }
                sb.append(sj.toString());
            }
            break;
        case NOTEQUIPBLE:
            if (this.item != null) {
                sb.append(this.item.getName());
            } else if (this.attemptedItemName != null) {
                sb.append("'").append(this.attemptedItemName).append("'");
            } else {
                sb.append("that");
            }
            sb.append(" is not equippable!");
            break;
        default:
            sb.append("You searched to equip ");
            if (this.attemptedItemName != null && this.attemptedItemName.length() > 0) {
                sb.append("'").append(this.attemptedItemName).append("' ");
            } else {
                sb.append("an item ");
            }
            if (this.attemptedSlot != null) {
                sb.append("to your ").append(this.attemptedSlot).append(" equipment slot ");
            }
            if (this.item != null) {
                sb.append(", and found ").append(this.item.getName()).append(" ");
                if (this.item instanceof Equipable) {
                    sb.append("which could equip to any of these slots: ");
                    StringJoiner sj = new StringJoiner(", ");
                    for (EquipmentSlots slots : this.getCorrectSlots()) {
                        sj.add(slots.toString());
                    }
                    sb.append(sj.toString()).append(". ");
                    if (this.attemptedSlot != null) {
                        sb.append("And you equipped it.");
                    }
                }
            } else {
                sb.append(" but did not find such in your inventory. ");
            }
            break;
        }
        return sb.toString();
    }

    public EquipResultType getSubType() {
        return subType;
    }

    public IItem getItem() {
        return item;
    }

    public String getAttemptedItemName() {
        return attemptedItemName;
    }

    public EquipmentSlots getAttemptedSlot() {
        return attemptedSlot;
    }

    public List<EquipmentSlots> getCorrectSlots() {
        if (this.item != null && this.item instanceof Equipable) {
            return ((Equipable) this.item).getWhichSlots();
        }
        return List.of();
    }

    @Override
    public TickType getTickType() {
        return tickType;
    }
}
