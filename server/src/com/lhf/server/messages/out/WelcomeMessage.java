package com.lhf.server.messages.out;

public class WelcomeMessage extends OutMessage {
    public String toString() {
        return "Welcome to <title>LHF MUD</title>!\r\n" + "<description>This is an old-school text-based adventure where multiple users can interact as they trawl the Dungeons of Ibaif!</description>\r\n" +
                "If you wish to have fun with us, either log on or create a user:\r\n" +
                "To create a user, use the command '<command>create <username> <password></command>'\r\n" +
                "If you wish to leave at any time, simply type '<command>exit</command>'\r\n";
    }
}
