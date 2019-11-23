package com.lhf.server.messages.out;

public class HelpMessage extends OutMessage {
    public String toString() {

        return "<description>Some commands that you can use:</description>\r\n" + "<command>SAY [message]</command>\t\t\tTells everyone in your current room the message\r\n" +
                "<command>TELL [username] [message]</command>\t\tTells the specified user your message\r\n" +
                "<command>LOOK</command>\t\t\t\tGives you some information about your surroundings\r\n" +
                "<command>EXAMINE [object]</command>\t\t\tTells you about some things about interactable objects\r\n" +
                "<command>GO [direction]</command>\t\t\tMove in the desired direction, if that direction exists\r\n" +
                "<command>ATTACK [creature] with [weapon] </command>\tAttack a creature with a weapon that you have\r\n" +
                "<command>DROP [item]</command>\t\t\tDrop an item that you have\r\n" +
                "<command>EQUIP [item] [slot]</command>\t\t\tMove an item from your inventory to an equipment slot\r\n" +
                "<command>UNEQUIP [slot]</command>\t\t\tMove an item from an equipment slot to your inventory\r\n" +
                "<command>UNEQUIP [item]</command>\t\t\tMove that item from your equipment to your inventory\r\n" +
                "<command>INTERACT [item]</command>\t\t\tAttempt to interact with an item in the room\r\n" +
                "<command>INVENTORY</command>\t\t\tList what you have in your inventory and what you have equipped\r\n" +
                "<command>TAKE [item]</command>\t\t\tTake an item from the room and add it to your inventory\r\n" +
                "<command>USE [item] on [target]</command>\t\tApply effects from the item to the target\r\n" +
                "<command>STATUS</command>\t\t\t\tShow you how much HP you currently have\r\n" +
                "<command>PLAYERS</command>\t\t\t\tList the players currently on the server\r\n" +

                //lastly
                "<command>EXIT</command>\t\t\t\tDisconnect and leave Ibaif\r\n";
    }
}
