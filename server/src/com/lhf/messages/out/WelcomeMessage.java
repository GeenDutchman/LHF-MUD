package com.lhf.messages.out;

public class WelcomeMessage extends OutMessage {
    public String toString() {
        StringBuilder sb = new StringBuilder("Welcome to <title>LHF MUD</title>!\n");
        sb.append("<description>This is an old-school text-based adventure where multiple users can interact as they trawl the Dungeons of Ibaif!</description>\n");
        sb.append("If you wish to have fun with us, either log on or create a user:\n");
        sb.append("To create a user, use the command '<command>create <username> <password></command>'\n");
        sb.append("If you wish to leave at any time, simply type '<command>exit</command>'");
        return sb.toString();
    }
}
