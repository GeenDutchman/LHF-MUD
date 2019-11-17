package com.lhf.messages.out;

import com.lhf.user.User;

import java.util.List;

public class ListPlayersMessage extends OutMessage {
    private String message;

    public ListPlayersMessage(List<String> usernames) {
        StringBuilder sb = new StringBuilder();
        sb.append("All players currently on this server:\r\n");
        for (int i = 0; i < usernames.size(); i++) {
            sb.append("<player>");
            sb.append(usernames.get(i));
            sb.append("</player>\r\n");
        }
        message = sb.toString();
    }

    public String toString() {
        return message;
    }
}
