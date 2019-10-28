package com.lhf.messages.in;

public class EquipMessage extends InMessage {
    String itemName;
    public EquipMessage(String args) {
       itemName = args;
    }

    public String getItemName() {
        return itemName;
    }
}
