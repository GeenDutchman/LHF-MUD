package com.lhf.game.map;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

import com.lhf.game.CreatureContainer;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.DungeonMaster;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.handlers.LewdAIHandler;
import com.lhf.game.creature.intelligence.handlers.SpeakOnOtherEntry;
import com.lhf.game.creature.intelligence.handlers.SpokenPromptChunk;
import com.lhf.game.item.Item;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.item.concrete.LewdBed;
import com.lhf.game.lewd.LewdBabyMaker;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.in.SayMessage;
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
    private transient Map<CommandMessage, CommandHandler> commands;

    public static class DMRoomBuilder implements Area.AreaBuilder {
        private Room.RoomBuilder delegate;
        private List<Dungeon> dungeons;

        private DMRoomBuilder() {
            this.delegate = Room.RoomBuilder.getInstance();
            this.dungeons = new ArrayList<>();
        }

        public static DMRoomBuilder getInstance() {
            return new DMRoomBuilder();
        }

        public DMRoomBuilder setName(String name) {
            this.delegate = delegate.setName(name);
            return this;
        }

        public DMRoomBuilder setDescription(String description) {
            this.delegate = delegate.setDescription(description);
            return this;
        }

        public DMRoomBuilder addItem(Item item) {
            this.delegate = delegate.addItem(item);
            return this;
        }

        public DMRoomBuilder addCreature(Creature creature) {
            this.delegate = delegate.addCreature(creature);
            return this;
        }

        public DMRoomBuilder addDungeonMaster(DungeonMaster dm) {
            this.delegate = delegate.addCreature(dm);
            return this;
        }

        public DMRoomBuilder setDungeon(Dungeon dungeon) {
            this.delegate = delegate.setDungeon(dungeon);
            return this;
        }

        public DMRoomBuilder addDungeon(Dungeon dungeon) {
            if (this.dungeons == null) {
                this.dungeons = new ArrayList<>();
            }
            if (dungeon != null) {
                this.dungeons.add(dungeon);
            }
            return this;
        }

        public DMRoomBuilder setSuccessor(MessageChainHandler successor) {
            this.delegate = delegate.setSuccessor(successor);
            return this;
        }

        @Override
        public Collection<Creature> getCreatures() {
            return this.delegate.getCreatures();
        }

        @Override
        public String getDescription() {
            return this.delegate.getDescription();
        }

        @Override
        public Collection<Item> getItems() {
            return this.delegate.getItems();
        }

        @Override
        public Land getLand() {
            return this.delegate.getLand();
        }

        @Override
        public String getName() {
            return this.delegate.getName();
        }

        @Override
        public MessageChainHandler getSuccessor() {
            return this.delegate.getSuccessor();
        }

        public DMRoom build() {
            return new DMRoom(this);
        }

        public static DMRoom buildDefault(AIRunner aiRunner, ConversationManager convoLoader)
                throws FileNotFoundException {
            DMRoomBuilder builder = DMRoomBuilder.getInstance();
            builder.setName("Control Room")
                    .setDescription("There are a lot of buttons and screens in here.  It looks like a home office.");

            DungeonMaster.DungeonMasterBuilder dmBuilder = DungeonMaster.DungeonMasterBuilder.getInstance(aiRunner);
            if (convoLoader != null) {
                dmBuilder.setConversationTree(convoLoader.convoTreeFromFile("verbal_default"));
            }
            LewdAIHandler lewdAIHandler = new LewdAIHandler().setPartnersOnly().setStayInAfter();
            dmBuilder.addAIHandler(lewdAIHandler);
            dmBuilder.addAIHandler(new SpokenPromptChunk().setAllowUsers());
            dmBuilder.addAIHandler(new SpeakOnOtherEntry());
            dmBuilder.setName("Ada Lovejax");
            DungeonMaster dmAda = dmBuilder.build();
            if (convoLoader != null) {
                dmBuilder.setConversationTree(convoLoader.convoTreeFromFile("gary"));
            }
            dmBuilder.setName("Gary Lovejax");
            DungeonMaster dmGary = dmBuilder.build();
            lewdAIHandler.addPartner(dmGary).addPartner(dmAda);

            builder.addCreature(dmAda).addCreature(dmGary);

            DMRoom built = builder.build();

            LewdBed.Builder bedBuilder = LewdBed.Builder.getInstance().setCapacity(2)
                    .setLewdProduct(new LewdBabyMaker()).addOccupant(dmGary).addOccupant(dmAda);
            LewdBed bed = bedBuilder.build(built); // TODO: figure out this room

            built.addItem(bed);

            return built;
        }
    }

    DMRoom(DMRoomBuilder builder) {
        super(builder.delegate);
        this.dungeons = builder.dungeons;
        this.users = new HashSet<>();
        this.commands = this.buildCommands();
    }

    public boolean addDungeon(@NotNull Dungeon dungeon) {
        dungeon.setSuccessor(this);
        return this.dungeons.add(dungeon);
    }

    public boolean addUser(User user) {
        if (this.filterCreatures(EnumSet.of(CreatureContainer.Filters.TYPE), null, null, null, null,
                DungeonMaster.class, null).size() < 2) {
            // shunt
            return this.addNewPlayer(Player.PlayerBuilder.getInstance(user).build());
        }
        boolean added = this.users.add(user);
        if (added) {
            user.setSuccessor(this);
            this.announce(RoomEnteredOutMessage.getBuilder().setNewbie(user).setBroacast().Build());
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
                this.announce(SomeoneLeftRoom.getBuilder().setLeaveTaker(user).setBroacast().Build());
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
                dungeon.announce(UserLeftMessage.getBuilder().setUser(user).setBroacast().Build());
            }
        }
    }

    @Override
    public Collection<ClientMessenger> getClientMessengers() {
        Collection<ClientMessenger> messengers = new ArrayList<>(super.getClientMessengers());
        messengers.addAll(this.users.stream()
                .filter(userThing -> userThing != null)
                .map(userThing -> (ClientMessenger) userThing).toList());
        return messengers;
    }

    @Override
    public boolean isCorrectEffectType(EntityEffect effect) {
        return effect != null && effect instanceof DMRoomEffect;
    }

    @Override
    public RoomAffectedMessage processEffect(EntityEffect effect, boolean reverse) {
        if (this.isCorrectEffectType(effect)) {
            DMRoomEffect dmRoomEffect = (DMRoomEffect) effect;
            this.logger.log(Level.FINER, () -> String.format("DMRoom processing effect '%s'", dmRoomEffect.getName()));
            if (dmRoomEffect.getEnsoulUsername() != null) {
                String name = dmRoomEffect.getEnsoulUsername();
                User user = this.getUser(name);
                if (user == null) {
                    this.logger.log(Level.FINEST,
                            () -> String.format("A user by the name of '%s' was not found", name));
                    if (dmRoomEffect.creatureResponsible() != null) {
                        OutMessage whoops = BadTargetSelectedMessage.getBuilder().setBde(BadTargetOption.DNE)
                                .setBadTarget(name).Build();
                        dmRoomEffect.creatureResponsible()
                                .sendMsg(whoops);
                        return null;
                    }
                }
                Optional<Item> maybeCorpse = this.getItem(name);
                if (maybeCorpse.isEmpty() || !(maybeCorpse.get() instanceof Corpse)) {
                    this.logger.log(Level.FINEST, () -> String.format("No corpse was found with the name '%s'", name));
                    if (effect.creatureResponsible() != null) {
                        effect.creatureResponsible().sendMsg(BadTargetSelectedMessage.getBuilder()
                                .setBde(BadTargetOption.DNE).setBadTarget(name).Build());
                        return null;
                    }
                }
                Corpse corpse = (Corpse) maybeCorpse.get();
                Player player = Player.PlayerBuilder.getInstance(user).setVocation(dmRoomEffect.getVocation())
                        .setCorpse(corpse).build();
                this.removeItem(corpse);
                this.addNewPlayer(player);
            }
        }
        return super.processEffect(effect, reverse);
    }

    protected class SayHandler extends Room.SayHandler {
        @Override
        public Reply handle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.SAY && cmd instanceof SayMessage sayMessage) {
                if (sayMessage.getTarget() != null && !sayMessage.getTarget().isBlank()) {
                    boolean sent = false;
                    for (User u : DMRoom.this.users) {
                        if (u.getUsername().equals(sayMessage.getTarget())) {
                            ClientMessenger sayer = ctx;
                            if (ctx.getCreature() != null) {
                                sayer = ctx.getCreature();
                            } else if (ctx.getUser() != null) {
                                sayer = ctx.getUser();
                            }
                            u.sendMsg(SpeakingMessage.getBuilder().setSayer(sayer).setMessage(sayMessage.getMessage())
                                    .setHearer(u).Build());
                            sent = true;
                            break;
                        }
                    }
                    if (sent) {
                        return ctx.handled();
                    }
                }
            }
            return super.handle(ctx, cmd);
        }

        @Override
        public MessageChainHandler getChainHandler() {
            return DMRoom.this;
        }
    }

    protected class CastHandler extends Room.CastHandler {
        private final static Predicate<CommandContext> enabledPredicate = Room.CastHandler.defaultRoomPredicate
                .and(ctx -> ctx.getCreature() instanceof DungeonMaster);

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return CastHandler.enabledPredicate;
        }

        @Override
        public MessageChainHandler getChainHandler() {
            return DMRoom.this;
        }
    }

    @Override
    protected Map<CommandMessage, CommandHandler> buildCommands() {
        Map<CommandMessage, CommandHandler> gathered = super.buildCommands();
        gathered.put(CommandMessage.SAY, new DMRoom.SayHandler());
        gathered.put(CommandMessage.CAST, new DMRoom.CastHandler());
        return gathered;
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        return Collections.unmodifiableMap(this.commands);
    }

}
