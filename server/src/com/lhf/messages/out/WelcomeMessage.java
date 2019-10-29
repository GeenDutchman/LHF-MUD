package com.lhf.messages.out;

public class WelcomeMessage extends OutMessage {
    public String toString() {
        StringBuilder sb = new StringBuilder("Welcome to LHF MUD!\n\r");
        sb.append("This is an old-school text-based adventure where multiple users can interact as they trawl the Dungeons of Ibaif!\n\r");
        sb.append("If you wish to have fun with us, either log on or create a user:\n\r");
        sb.append("To create a user, use the command 'CREATE <username> <password>'\n\r");
        sb.append("If you wish to leave at any time, simply type 'exit'\n\r");
        return sb.toString();
    }
}
