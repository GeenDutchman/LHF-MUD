package com.lhf.game.map;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.lhf.game.creature.Creature;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.in.SeeMessage;
import com.lhf.messages.out.CannotSpeakToMessage;
import com.lhf.messages.out.RoomEnteredOutMessage;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SpeakingMessage;
import com.lhf.server.client.user.User;

public class DMRoom extends Room {
    private Set<User> users;

    DMRoom(String name) {
        super(name);
        this.users = new TreeSet<>();
    }

    DMRoom(String name, String description) {
        super(name, description);
        this.users = new TreeSet<>();
    }

    public boolean addUser(User user) {
        user.setSuccessor(user);
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

    private boolean handleSay(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SAY) {
            SayMessage sayMessage = (SayMessage) msg;
            if (sayMessage.getTarget() != null && !sayMessage.getTarget().isBlank()) {
                boolean sent = false;
                for (Creature c : this.getCreaturesInRoom()) {
                    if (c.CheckNameRegex(sayMessage.getTarget(), 3)) {
                        c.sendMsg(new SpeakingMessage(ctx, sayMessage.getMessage(), c));
                        sent = true;
                        break;
                    }
                }
                if (!sent) {
                    for (User u : this.users) {
                        if (u.getUsername().equalsIgnoreCase(sayMessage.getTarget())) {
                            u.sendMsg(new SpeakingMessage(ctx, sayMessage.getMessage(), u));
                            sent = true;
                            break;
                        }
                    }
                }
                if (!sent) {
                    ctx.sendMsg(new CannotSpeakToMessage(sayMessage.getTarget(), sayMessage.getTarget()));
                }
            } else {
                this.sendMessageToAll(new SpeakingMessage(ctx.getCreature(), sayMessage.getMessage()));
            }
            return true;
        }
        return false;
    }

    private boolean handleSee(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SEE) {
            SeeMessage seeMessage = (SeeMessage) msg;
            if (seeMessage.getThing() != null) {
                ArrayList<Creature> found = this.getCreaturesInRoom(seeMessage.getThing());
                if (found.size() == 1) {
                    ctx.sendMsg(found.get(0).produceMessage().addExtraInfo("They are in the room with you.  "));
                }
                ctx.sendMsg(new SeeOutMessage("You couldn't find " + seeMessage.getThing() + " to examine. "));

            } else {
                ctx.sendMsg(this.produceMessage());
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean handleMessage(CommandContext ctx, Command msg) {
        boolean handled = false;
        CommandMessage type = msg.getType();
        ctx = this.addSelfToContext(ctx);
        if (type != null && this.getCommands().containsKey(type)) {
            if (ctx.getCreature() == null) {
                if (type == CommandMessage.SAY) {
                    handled = this.handleSay(ctx, msg);
                } else if (type == CommandMessage.SEE) {
                    handled = this.handleSee(ctx, msg);
                }
            } else {
                return super.handleMessage(ctx, msg);
            }
        }
        if (handled) {
            return handled;
        }
        return super.handleMessage(ctx, msg);
    }

    @Override
    public EnumMap<CommandMessage, String> gatherHelp(CommandContext ctx) {
        ctx = super.addSelfToContext(ctx);
        EnumMap<CommandMessage, String> gathered = super.gatherHelp(ctx);
        if (ctx.getCreature() == null) {
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
        }
        return gathered;
    }

}
