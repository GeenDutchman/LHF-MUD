package com.lhf.messages.out;

public class HelpMessage extends OutMessage {
    public String toString() {
        StringBuilder sb = new StringBuilder("\n\r<description>Some commands that you can use:</description>\n\r");
        sb.append("<command>SAY [message]</command>\t\t\tTells everyone in your current room the message\n\r");
        sb.append("<command>TELL [username] [message]</command>\tTells the specified user your message\n\r");
        sb.append("<command>LOOK</command>\t\t\t\tGives you some information about your surroundings\n\r");
        sb.append("<command>EXAMINE [object]</command>\t\t\tTells you about some things about interactable objects\n\r");
        sb.append("<command>GO [direction]</command>\t\t\tMove in the desired direction, if that direction exists\n\r");
        sb.append("<command>ATTACK [creature] with [weapon] </command>\twill attack a creature with a weapon that you have\n\r");
        sb.append("<command>DROP [item]</command>\t\t\twill drop an item that you have\n\r");
        sb.append("<command>EQUIP [item] [slot]</command>\t\t\twill move an item from your inventory to an equipment slot\n\r");
        sb.append("<command>UNEQUIP [slot]</command>\t\t\twill move an item from an equipment slot to your inventory\n\r");
        sb.append("<command>UNEQUIP [item]</command>\t\t\twill move that item from your equipment to your inventory\n\r");
        sb.append("<command>INTERACT [item]</command>\t\t\twill attempt to interact with an item in the room\n\r");
        sb.append("<command>INVENTORY</command>\t\t\t\twill list what you have in your inventory and what you have equipped\n\r");
        sb.append("<command>TAKE [item]</command>\t\t\twill take an item from the room and add it to your inventory\n\r");
        sb.append("<command>USE [item] on [target]</command>\t\t\twill attempt to apply effects from the item to the target\n\r");
        sb.append("<command>STATUS</command>\t\t\twill tell you how much HP you currently have\n\r");
        sb.append("<command>PLAYERS</command>\t\t\twill tell you all the players currently on the server\n\r");
      
        //lastly
        sb.append("<command>EXIT</command>\twill let you disconnect and leave Ibaif\n");
        return sb.toString();
    }
}
