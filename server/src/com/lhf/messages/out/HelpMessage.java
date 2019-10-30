package com.lhf.messages.out;

public class HelpMessage extends OutMessage {
    public String toString() {
        StringBuilder sb = new StringBuilder("\n\rSome commands that you can use:\n\r");
        sb.append("SAY [message]\t\t\tTells everyone in your current room the message\n\r");
        sb.append("TELL [username] [message]\tTells the specified user your message\n\r");
        sb.append("LOOK\t\t\t\tGives you some information about your surroundings\n\r");
        sb.append("EXAMINE\t\t\t\tTells you about some things about interactable objects\n\r");
        sb.append("GO [direction]\t\t\tMove in the desired direction, if that direction exists\n\r");


        //lastly
        sb.append("EXIT\t\t\t\tDisconnect and leave Ibaif\n\r");
        return sb.toString();
    }
}
