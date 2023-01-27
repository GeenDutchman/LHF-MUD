package com.lhf.game.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.game.CreatureContainer;
import com.lhf.game.EntityEffect;
import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.DungeonMaster;
import com.lhf.game.creature.Player;
import com.lhf.game.item.Item;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.out.BadMessage;
import com.lhf.messages.out.BadMessage.BadMessageType;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.RoomAffectedMessage;
import com.lhf.messages.out.RoomEnteredOutMessage;
import com.lhf.messages.out.SomeoneLeftRoom;
import com.lhf.messages.out.SpeakingMessage;
import com.lhf.messages.out.UserLeftMessage;
import com.lhf.server.client.user.User;
import com.lhf.server.interfaces.NotNull;

public class DMRoom extends Room {
    private Set<User> users;
    private List<Dungeon> dungeons;

    DMRoom(String name, BattleManager.Builder battleManagerBuilder) {
        super(name, battleManagerBuilder);
        this.users = new HashSet<>();
        this.dungeons = new ArrayList<>();
    }

    DMRoom(String name, BattleManager.Builder battleManagerBuilder, String description) {
        super(name, battleManagerBuilder, description);
        this.users = new HashSet<>();
        this.dungeons = new ArrayList<>();
    }

    public boolean addDungeon(@NotNull Dungeon dungeon) {
        dungeon.setSuccessor(this);
        return this.dungeons.add(dungeon);
    }

    public boolean addUser(User user) {
        if (this.filterCreatures(EnumSet.of(CreatureContainer.Filters.TYPE), null, null, null, null,
                DungeonMaster.class, null).size() < 2) {
            // shunt
            return this.addNewPlayer(new Player(user));
        }
        boolean added = this.users.add(user);
        if (added) {
            user.setSuccessor(this);
            this.announce(new RoomEnteredOutMessage(user));
        }
        return added;
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
                this.announce(new SomeoneLeftRoom(user, null));
                return user;
            }
        }
        return null;
    }

    public boolean addNewPlayer(Player player) {
        return this.dungeons.get(0).addPlayer(player);
    }

    public void userExitSystem(User user) {
        for (Dungeon dungeon : this.dungeons) {
            if (dungeon.removePlayer(user.getUserID()).isPresent()) {
                dungeon.announce(new UserLeftMessage(user, false));
            }
        }
    }

    @Override
    public boolean announce(OutMessage message, String... deafened) {
        super.announce(message, deafened);
        List<String> deafenedNames = Arrays.asList(deafened);
        for (User user : this.users) {
            if (!deafenedNames.contains(user.getUsername())) {
                user.sendMsg(message);
            }
        }
        return true;
    }

    @Override
    public boolean isCorrectEffectType(EntityEffect effect) {
        return effect instanceof DMRoomEffect;
    }

    @Override
    public RoomAffectedMessage processEffect(EntityEffect effect, boolean reverse) {
        if (this.isCorrectEffectType(effect)) {
            DMRoomEffect dmRoomEffect = (DMRoomEffect) effect;
            if (dmRoomEffect.getEnsoulUsername() != null) {
                String name = dmRoomEffect.getEnsoulUsername();
                User user = this.getUser(name);
                if (user == null) {
                    if (dmRoomEffect.creatureResponsible() != null) {
                        OutMessage whoops = new BadTargetSelectedMessage(BadTargetOption.DNE, name);
                        dmRoomEffect.creatureResponsible()
                                .sendMsg(whoops);
                        return null;
                    }
                }
                Optional<Item> maybeCorpse = this.getItem(name);
                if (maybeCorpse.isEmpty() || !(maybeCorpse.get() instanceof Corpse)) {
                    if (effect.creatureResponsible() != null) {
                        effect.creatureResponsible().sendMsg(new BadTargetSelectedMessage(BadTargetOption.DNE, name));
                        return null;
                    }
                }
                Corpse corpse = (Corpse) maybeCorpse.get(); // TODO: actually use the corpse and get vocation
                Player player = new Player(user, dmRoomEffect.getVocation());
                this.removeItem(corpse);
                this.addNewPlayer(player);
            }
        }
        return super.processEffect(effect, reverse);
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
        if (ctx.getRoom() == null) { // if we aren't already in a room
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
        }
        if (this.getSuccessor() != null) {
            return this.getSuccessor().handleMessage(ctx, msg);
        }
        return false;
    }

}
