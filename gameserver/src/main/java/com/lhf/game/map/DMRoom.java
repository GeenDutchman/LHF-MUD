package com.lhf.game.map;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.game.creature.DungeonMaster;
import com.lhf.game.creature.Player;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.out.BadMessage;
import com.lhf.messages.out.BadMessage.BadMessageType;
import com.lhf.messages.out.RoomEnteredOutMessage;
import com.lhf.messages.out.SpeakingMessage;
import com.lhf.messages.out.UserLeftMessage;
import com.lhf.server.client.user.User;
import com.lhf.server.interfaces.NotNull;

public class DMRoom extends Room {
    private Set<User> users;
    private List<Dungeon> dungeons;

    DMRoom(String name) {
        super(name);
        this.users = new HashSet<>();
        this.dungeons = new ArrayList<>();
    }

    DMRoom(String name, String description) {
        super(name, description);
        this.users = new HashSet<>();
        this.dungeons = new ArrayList<>();
    }

    public boolean addDungeon(@NotNull Dungeon dungeon) {
        dungeon.setSuccessor(this);
        return this.dungeons.add(dungeon);
    }

    public boolean addUser(User user) {
        if (this.getCreaturesInRoom().size() < 2) {
            // shunt
            return this.addNewPlayer(new Player(user));
        }
        user.setSuccessor(this);
        this.sendMessageToAll(new RoomEnteredOutMessage(user));
        return this.users.add(user);
    }

    public User getUser(String username) {
        for (User user : this.users) {
            if (username.equals(user.getUsername())) {
                return user;
            }
        }
        return null;
    }

    public User removeUser(String username) {
        for (User user : this.users) {
            if (username.equals(user.getUsername())) {
                this.users.remove(user);
                return user;
            }
        }
        return null;
    }

    public boolean addNewPlayer(Player player) {
        return this.dungeons.get(0).addNewPlayer(player);
    }

    public void userExitSystem(User user) {
        for (Dungeon dungeon : this.dungeons) {
            if (dungeon.removePlayer(user.getUserID())) {
                dungeon.sendMessageToAll(new UserLeftMessage(user, false));
            }
        }
    }

    @Override
    protected Boolean handleSay(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SAY) {
            SayMessage sayMessage = (SayMessage) msg;
            if (sayMessage.getTarget() != null && !sayMessage.getTarget().isBlank()) {
                boolean sent = false;
                for (User u : this.users) {
                    if (u.getUsername().equals(sayMessage.getTarget())) {
                        ClientMessenger sayer = ctx;
                        if (ctx.getCreature() != null) {
                            sayer = ctx.getCreature();
                        } else if (ctx.getUser() != null) {
                            sayer = ctx.getUser();
                        }
                        u.sendMsg(new SpeakingMessage(sayer, sayMessage.getMessage(), u));
                        sent = true;
                        break;
                    }
                }
                if (sent) {
                    return sent;
                }
            }
        }
        return super.handleSay(ctx, msg);
    }

    @Override
    protected Boolean handleSee(CommandContext ctx, Command msg) {
        return super.handleSee(ctx, msg);
    }

    @Override
    public EnumMap<CommandMessage, String> gatherHelp(CommandContext ctx) {
        ctx = super.addSelfToContext(ctx);
        EnumMap<CommandMessage, String> gathered = super.gatherHelp(ctx);
        if (ctx.getCreature() == null || !(ctx.getCreature() instanceof DungeonMaster)) {
            StringJoiner sj = new StringJoiner(" ");
            sj.add("\"say [message] to [name]\"")
                    .add("Will tell a specific person somewhere in your current room your message.");
            sj.add("If your message contains the word 'to', put your message in quotes like")
                    .add("\"say 'They are taking the hobbits to Isengard' to Aragorn\"")
                    .add("\r\n");
            gathered.put(CommandMessage.SAY, sj.toString());
            for (CommandMessage cm : super.getCommands().keySet()) {
                if (!CommandMessage.SAY.equals(cm) || !CommandMessage.SEE.equals(cm)) {
                    gathered.remove(cm);
                }
            }
            gathered.remove(CommandMessage.CAST);
        }
        return gathered;
    }

    @Override
    public boolean handleMessage(CommandContext ctx, Command msg) {
        if (ctx.getCreature() != null && ctx.getCreature() instanceof DungeonMaster) {
            return super.handleMessage(ctx, msg);
        }
        boolean handled = false;
        CommandMessage type = msg.getType();
        ctx = this.addSelfToContext(ctx);
        if (type == CommandMessage.SAY) {
            handled = this.handleSay(ctx, msg);
        } else if (type == CommandMessage.SEE) {
            handled = this.handleSee(ctx, msg);
            if (handled) {
                return handled;
            }
            ctx.sendMsg(this.produceMessage());
            return true;
        } else if (type == CommandMessage.CAST) {
            if (ctx.getCreature() == null || !(ctx.getCreature() instanceof DungeonMaster)) {
                ctx.sendMsg(new BadMessage(BadMessageType.CREATURES_ONLY, this.gatherHelp(ctx), msg));
                return true;
            }
            handled = super.handleCast(ctx, msg);
        }
        if (handled) {
            return handled;
        }
        if (this.getSuccessor() != null) {
            return this.getSuccessor().handleMessage(ctx, msg);
        }
        return false;
    }

}
