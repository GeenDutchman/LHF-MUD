package com.lhf.messages.out;

public class HelpMessage extends OutMessage {
    public String toString() {
        StringBuilder sb = new StringBuilder("Some commands that you can use:\n");
        sb.append("say [message]\twill tell everyone in your current room the message\n");
        sb.append("tell [username] [message]\twill tell that specific user your message\n");
        sb.append("look\twill give you some information about your surroundings\n");
        sb.append("examine\twill tell you about some things about interactable objects\n");
        sb.append("go [direction]\twill move you in the desired direction, if that direction exists\n");


        //lastly
        sb.append("exit\twill let you disconnect and leave Ibaif");
        return sb.toString();
    }
}
