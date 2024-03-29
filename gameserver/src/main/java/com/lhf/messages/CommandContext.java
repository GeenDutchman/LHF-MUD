package com.lhf.messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;

import com.lhf.game.creature.ICreature;
import com.lhf.game.map.Area;
import com.lhf.game.map.Land;
import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.client.Client;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;

public class CommandContext {
    protected Client client;
    protected User user;
    protected ICreature creature;
    protected NavigableSet<SubArea> subAreas = new TreeSet<>();
    protected Area area;
    protected Land land;
    protected EnumMap<AMessageType, String> helps = new EnumMap<>(AMessageType.class);
    protected List<GameEvent> messages = new ArrayList<>();

    public class Reply {
        protected boolean handled;

        protected Reply(boolean isHandled) {
            this.handled = isHandled;
        }

        public Map<AMessageType, String> getHelps() {
            if (CommandContext.this.helps == null) {
                CommandContext.this.helps = new EnumMap<>(AMessageType.class);
            }
            return Collections.unmodifiableMap(CommandContext.this.helps);
        }

        public List<GameEvent> getMessages() {
            if (CommandContext.this.messages == null) {
                CommandContext.this.messages = new ArrayList<>();
            }
            return Collections.unmodifiableList(CommandContext.this.messages);
        }

        public Optional<GameEvent> getLastMessage() {
            if (CommandContext.this.messages == null || CommandContext.this.messages.size() == 0) {
                return Optional.empty();
            }
            try {
                return Optional.ofNullable(CommandContext.this.messages.get(CommandContext.this.messages.size() - 1));
            } catch (IndexOutOfBoundsException e) {
                return Optional.empty();
            }
        }

        public boolean isHandled() {
            return handled;
        }

        public Reply resolve() {
            this.handled = true;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Reply [handled=").append(handled)
                    .append(",messageTypes=")
                    .append(this.getMessages().stream().map(gameEvent -> gameEvent.getEventType()).toList())
                    .append(",helps=").append(this.getHelps().keySet())
                    .append("]");
            return builder.toString();
        }

    }

    public CommandContext copy() {
        CommandContext theCopy = new CommandContext();
        theCopy.client = this.client;
        theCopy.user = this.user;
        theCopy.creature = this.creature;
        theCopy.subAreas = this.subAreas;
        theCopy.area = this.area;
        theCopy.land = this.land;
        theCopy.helps = new EnumMap<>(this.helps);
        theCopy.messages = new ArrayList<>(this.messages);
        return theCopy;
    }

    public Reply failhandle() {
        return this.new Reply(false);
    }

    public Reply handled() {
        return this.new Reply(true);
    }

    public CommandContext addMessage(GameEvent message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        if (message != null) {
            this.messages.add(message);
        }
        return this;
    }

    /**
     * Adds help data to the context, returns the provided helps found
     * 
     * @param helpsFound help data to collect in the context
     * @return the helpsFound
     */
    public Map<AMessageType, String> addHelps(Map<AMessageType, String> helpsFound) {
        if (this.helps == null) {
            this.helps = new EnumMap<>(AMessageType.class);
        }
        if (helpsFound != null) {
            for (Map.Entry<AMessageType, String> entry : helpsFound.entrySet()) {
                this.helps.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        return helpsFound;
    }

    public CommandContext addHelp(AMessageType cmd, String help) {
        if (this.helps == null) {
            this.helps = new EnumMap<>(AMessageType.class);
        }
        if (cmd != null && help != null) {
            this.helps.putIfAbsent(cmd, help);
        }
        return this;
    }

    public Map<AMessageType, String> getHelps() {
        return Collections.unmodifiableMap(helps);
    }

    public Client getClient() {
        return this.client;
    }

    public ICreature getCreature() {
        return creature;
    }

    public CommandContext setCreature(ICreature creature) {
        this.creature = creature;
        return this;
    }

    public synchronized CommandContext receive(GameEvent event) {
        if (event != null) {
            this.addMessage(event);
            if (this.creature != null) {
                ICreature.eventAccepter.accept(this.creature, event);
            } else if (this.user != null) {
                User.eventAccepter.accept(this.user, event);
            } else if (this.client != null) {
                Client.eventAccepter.accept(this.client, event);
            }
        }
        return this;
    }

    public CommandContext receive(GameEvent.Builder<?> builder) {
        this.receive(builder.Build());
        return this;
    }

    public CommandContext setClient(Client client) {
        this.client = client;
        return this;
    }

    public User getUser() {
        return user;
    }

    public UserID getUserID() {
        if (user != null) {
            return user.getUserID();
        }
        return null;
    }

    public CommandContext setUser(User user) {
        this.user = user;
        return this;
    }

    public final NavigableSet<SubArea> getSubAreas() {
        return Collections.unmodifiableNavigableSet(this.subAreas);
    }

    public boolean addSubArea(SubArea subArea) {
        if (subArea == null) {
            return false;
        }
        return this.subAreas.add(subArea);
    }

    public final SubArea getSubAreaForSort(SubAreaSort sort) {
        if (sort == null) {
            return null;
        }
        final NavigableSet<SubArea> subAreas = this.getSubAreas();
        if (subAreas == null) {
            return null;
        }
        for (final SubArea subArea : subAreas) {
            if (sort.equals(subArea.getSubAreaSort())) {
                return subArea;
            }
        }
        return null;
    }

    public final boolean hasSubAreaSort(SubAreaSort sort) {
        if (sort == null) {
            return false;
        }
        return this.getSubAreaForSort(sort) != null;
    }

    public CommandContext setArea(Area room) {
        this.area = room;
        return this;
    }

    public Area getArea() {
        return this.area;
    }

    public Land getLand() {
        return land;
    }

    public CommandContext setLand(Land dungeon) {
        this.land = dungeon;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CommandContext [client=").append(client).append(", user=").append(user).append(", creature=")
                .append(creature).append(", room=").append(area).append(", subAreas=").append(subAreas)
                .append(", land=").append(land).append("]");
        return builder.toString();
    }

}
