package com.lhf.messages.out;

public class HelpMessage extends OutMessage {
    public String toString() {

        return "<description>Some commands that you can use (case insensitive):</description>\r\n"
                + "<command>SAY [message] to [username] </command>\r\nTells everyone (by default) in your current room the message\r\n"
                +
                "<command>SEE</command>\r\nGives you some information about your surroundings\r\n" +
                "<command>EXAMINE [object]</command>\r\nTells you about some things about interactable objects\r\n" +
                "<command>GO [direction]</command>\r\nMove in the desired direction, if that direction exists\r\n" +
                "<command>ATTACK [creature] with [weapon] </command>\r\nAttack a creature with a weapon that you have\r\n"
                +
                "<command>DROP [item]</command>\r\nDrop an item that you have\r\n" +
                "<command>EQUIP [item] [slot]</command>\r\nMove an item from your inventory to an equipment slot\r\n" +
                "<command>UNEQUIP [slot]</command>\r\nMove an item from an equipment slot to your inventory\r\n" +
                "<command>UNEQUIP [item]</command>\r\nMove that item from your equipment to your inventory\r\n" +
                "<command>INTERACT [item]</command>\r\nAttempt to interact with an item in the room\r\n" +
                "<command>INVENTORY</command>\r\nList what you have in your inventory and what you have equipped\r\n" +
                "<command>TAKE [item]</command>\r\nTake an item from the room and add it to your inventory\r\n" +
                "<command>USE [item] on [target]</command>\r\nApply effects from the item to the target\r\n" +
                "<command>STATUS</command>\r\nShow you how much HP you currently have\r\n" +
                "<command>PLAYERS</command>\r\nList the players currently on the server\r\n" +

                // lastly
                "<command>EXIT</command>\r\nDisconnect and leave Ibaif\r\n";
    }
}
