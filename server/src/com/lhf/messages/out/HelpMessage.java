package com.lhf.messages.out;

public class HelpMessage extends OutMessage {
    public String toString() {
        StringBuilder sb = new StringBuilder("\n\rSome commands that you can use:\n\r");
        sb.append("SAY [message]\t\t\tTells everyone in your current room the message\n\r");
        sb.append("TELL [username] [message]\tTells the specified user your message\n\r");
        sb.append("LOOK\t\t\t\tGives you some information about your surroundings\n\r");
        sb.append("EXAMINE [object]\t\t\tTells you about some things about interactable objects\n\r");
        sb.append("GO [direction]\t\t\tMove in the desired direction, if that direction exists\n\r");
        sb.append("ATTACK [creature] with [weapon] \twill attack a creature with a weapon that you have\n\r");
        sb.append("DROP [item]\t\t\twill drop an item that you have\n\r");
        sb.append("EQUIP [item] [slot]\t\t\twill move an item from your inventory to an equipment slot\n\r");
        sb.append("UNEQUIP [slot]\t\t\twill move an item from an equipment slot to your inventory\n\r");
        sb.append("INTERACT [item]\t\t\twill attempt to interact with an item in the room\n\r");
        sb.append("INVENTORY\t\t\t\twill list what you have in your inventory and what you have equipped\n\r");
        sb.append("TAKE [item]\t\t\twill take an item from the room and add it to your inventory\n\r");
        sb.append("USE [item] on [target]\t\t\twill attempt to apply effects from the item to the target\n\r");
        //lastly
        sb.append("EXIT\t\t\t\tDisconnect and leave Ibaif\n\r");
        return sb.toString();
    }
}
