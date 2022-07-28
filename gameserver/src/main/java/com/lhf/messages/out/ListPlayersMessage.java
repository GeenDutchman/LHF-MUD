package com.lhf.messages.out;

import java.util.List;

import com.lhf.messages.OutMessageType;

public class ListPlayersMessage extends OutMessage {
    private String message;

    public ListPlayersMessage(List<String> usernames) {
        super(OutMessageType.LIST_PLAYERS);
        StringBuilder sb = new StringBuilder();
        sb.append("All players currently on this server:\r\n");
        for (String username : usernames) {
            sb.append("<player>");
            sb.append(username);
            sb.append("</player>\r\n");
        }
        message = sb.toString();
    }

    public String toString() {
        return message;
    }
}
