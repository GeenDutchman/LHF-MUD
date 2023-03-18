package com.lhf.messages.out;

import java.util.Collection;
import java.util.Collections;
import java.util.StringJoiner;

import com.lhf.game.map.Directions;
import com.lhf.messages.OutMessageType;

public class BadGoMessage extends OutMessage {
    public enum BadGoType {
        DNE, BLOCKED, NO_ROOM;
    }

    private final BadGoType subType;
    private final Directions attempted;
    private final Collection<Directions> available;

    public static class Builder extends OutMessage.Builder<Builder> {
        private BadGoType subType;
        private Directions attempted;
        private Collection<Directions> available;

        protected Builder() {
            super(OutMessageType.BAD_GO);
        }

        protected Builder(BadGoType type) {
            super(OutMessageType.BAD_GO);
            this.subType = type;
        }

        public BadGoType getSubType() {
            return subType;
        }

        public Builder setSubType(BadGoType type) {
            this.subType = type;
            return this;
        }

        public Directions getAttempted() {
            return attempted;
        }

        public Builder setAttempted(Directions attempted) {
            this.attempted = attempted;
            return this;
        }

        public Collection<Directions> getAvailable() {
            return Collections.unmodifiableCollection(available);
        }

        public void setAvailable(Collection<Directions> available) {
            this.available = available;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BadGoMessage Build() {
            return new BadGoMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    protected BadGoMessage(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.attempted = builder.getAttempted();
        this.available = builder.getAvailable();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("You cannot go ");
        if (this.attempted != null) {
            sb.append(this.attempted.getColorTaggedName());
        } else {
            sb.append("that way");
        }
        sb.append(". ");
        if (this.subType == BadGoType.DNE || this.attempted == null) {
            sb.append("That way is a wall. ");
        } else if (this.subType == BadGoType.BLOCKED) {
            sb.append("Your path is blocked ");
        } else if (this.subType == BadGoType.NO_ROOM) {
            sb.append("You are not in a room. ");
        }
        if (this.available != null && this.available.size() > 0) {
            if (this.available.size() == 1 && this.attempted != null && this.subType == BadGoType.BLOCKED) {
                sb.append("No other directions are available.  Try finding a way to unblock it. ");
            } else {
                sb.append("You could try to go one of:");
                StringJoiner sj = new StringJoiner(", ");
                for (Directions s : this.available) {
                    if (!(this.subType == BadGoType.BLOCKED && s.equals(this.attempted))) {
                        sj.add(s.getColorTaggedName());
                    }
                }
                sb.append(sj.toString());
            }
        } else {
            sb.append("No directions are available.");
        }
        return sb.toString();
    }

    public Directions getAttempted() {
        return attempted;
    }

    public Collection<Directions> getAvailable() {
        return available;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
