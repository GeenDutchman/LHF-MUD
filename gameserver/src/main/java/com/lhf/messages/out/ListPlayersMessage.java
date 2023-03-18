package com.lhf.messages.out;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lhf.messages.OutMessageType;

public class ListPlayersMessage extends OutMessage {
    private final List<String> playerNames;

    public static class Builder extends OutMessage.Builder<Builder> {
        private List<String> playerNames;

        protected Builder() {
            super(OutMessageType.LIST_PLAYERS);
        }

        public List<String> getPlayerNames() {
            return Collections.unmodifiableList(playerNames);
        }

        public Builder setPlayerNames(List<String> names) {
            this.playerNames = names;
            return this;
        }

        public Builder addPlayerName(String name) {
            if (this.playerNames == null) {
                this.playerNames = new ArrayList<>();
            }
            this.playerNames.add(name);
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ListPlayersMessage Build() {
            return new ListPlayersMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ListPlayersMessage(Builder builder) {
        super(builder);
        this.playerNames = builder.getPlayerNames();
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("All players currently on this server:\r\n");
        for (String username : this.playerNames) {
            sb.append("<player>");
            sb.append(username);
            sb.append("</player>\r\n");
        }
        return sb.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }
}
