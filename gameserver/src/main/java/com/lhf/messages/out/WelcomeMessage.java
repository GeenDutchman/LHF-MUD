package com.lhf.messages.out;

import java.util.StringJoiner;

public class WelcomeMessage extends OutMessage {
    public String toString() {
        StringJoiner sj = new StringJoiner("\r\n");
        sj.add("Welcome to <title>LHF MUD</title>!");
        sj.add("<description>This is an old-school text-based adventure where multiple users can interact as they trawl the Dungeons of Ibaif!</description>");
        sj.add("If you wish to have fun with us, either log on or create a user.");
        sj.add("To create a user, use the command:");
        sj.add("<command>create \"[username]\" with \"[password]\"</command>");
        sj.add("If you wish to leave at any time, simply type:");
        sj.add("<command>exit</command>");

        return sj.toString();
    }
}
