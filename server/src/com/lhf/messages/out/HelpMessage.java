package com.lhf.messages.out;

public class HelpMessage extends OutMessage {
    public String toString() {
        StringBuilder sb = new StringBuilder("<description>Some commands that you can use:</description>\n");
        sb.append("<command>say [message]</command>\twill tell everyone in your current room the message\n");
        sb.append("<command>tell [username] [message]</command>\twill tell that specific user your message\n");
        sb.append("<command>look</command>\twill give you some information about your surroundings\n");
        sb.append("<command>examine [object/item]</command>\twill tell you about some things about objects\n");
        sb.append("<command>interact [object]</command>\ttry to interact with an object in a room\n");
        sb.append("<command>go [direction]</command>\texit a room in the given direction\n");

        //lastly
        sb.append("<command>exit</command>\twill let you disconnect and leave Ibaif\n");
        return sb.toString();
    }
}
