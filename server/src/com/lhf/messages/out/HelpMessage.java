package com.lhf.messages.out;

public class HelpMessage extends OutMessage {
    public String toString() {
        StringBuilder sb = new StringBuilder("Some commands that you can use:\n");
        sb.append("say [message]\twill tell everyone in your current room the message\n");
        sb.append("tell [username] [message]\twill tell that specific user your message\n");
        sb.append("look\nwill give you some information about your surroundings\n");
        sb.append("examine\nwill tell you about some things about interactable objects\n");


        //lastly
        sb.append("exit\nwill let you disconnect and leave Ibaif");
        return sb.toString();
    }
}
