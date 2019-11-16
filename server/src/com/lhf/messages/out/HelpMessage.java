package com.lhf.messages.out;

public class HelpMessage extends OutMessage {
    public String toString() {
        StringBuilder sb = new StringBuilder("<description>Some commands that you can use:</description>\r\n");
        sb.append("<command>SAY [message]</command>\t\t\tTells everyone in your current room the message\r\n");
        sb.append("<command>TELL [username] [message]</command>\tTells the specified user your message\r\n");
        sb.append("<command>LOOK</command>\t\t\t\tGives you some information about your surroundings\r\n");
        sb.append("<command>EXAMINE [object]</command>\t\t\tTells you about some things about interactable objects\r\n");
        sb.append("<command>GO [direction]</command>\t\t\tMove in the desired direction, if that direction exists\r\n");
        sb.append("<command>ATTACK [creature] with [weapon] </command>\twill attack a creature with a weapon that you have\r\n");
        sb.append("<command>DROP [item]</command>\t\t\twill drop an item that you have\r\n");
        sb.append("<command>EQUIP [item] [slot]</command>\t\t\twill move an item from your inventory to an equipment slot\r\n");
        sb.append("<command>UNEQUIP [slot]</command>\t\t\twill move an item from an equipment slot to your inventory\r\n");
        sb.append("<command>UNEQUIP [item]</command>\t\t\twill move that item from your equipment to your inventory\r\n");
        sb.append("<command>INTERACT [item]</command>\t\t\twill attempt to interact with an item in the room\r\n");
        sb.append("<command>INVENTORY</command>\t\t\t\twill list what you have in your inventory and what you have equipped\r\n");
        sb.append("<command>TAKE [item]</command>\t\t\twill take an item from the room and add it to your inventory\r\n");
        sb.append("<command>USE [item] on [target]</command>\t\t\twill attempt to apply effects from the item to the target\r\n");
        sb.append("<command>STATUS</command>\t\t\twill tell you how much HP you currently have\r\n");
        sb.append("<command>PLAYERS</command>\t\t\twill tell you all the players currently on the server\r\n");
      
        //lastly
        sb.append("<command>EXIT</command>\twill let you disconnect and leave Ibaif\r\n");
        return sb.toString();
    }
}
