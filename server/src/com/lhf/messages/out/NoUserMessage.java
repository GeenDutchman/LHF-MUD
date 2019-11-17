package com.lhf.messages.out;

public class NoUserMessage extends OutMessage {
    public String toString() {
        return "You have neither created nor logged in as a User, so you can't do much yet.\r\nTry running 'CREATE <username> <password>'\r\n";
    }
}
