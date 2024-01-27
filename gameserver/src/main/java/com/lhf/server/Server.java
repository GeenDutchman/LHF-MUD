package com.lhf.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.Game;
import com.lhf.game.Game.GameBuilder;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.events.BadUserDuplicationEvent;
import com.lhf.messages.events.UserLeftEvent;
import com.lhf.messages.events.WelcomeEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.CreateInMessage;
import com.lhf.server.client.Client;
import com.lhf.server.client.Client.ClientID;
import com.lhf.server.client.ClientManager;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.client.user.UserManager;
import com.lhf.server.interfaces.ConnectionListener;
import com.lhf.server.interfaces.NotNull;
import com.lhf.server.interfaces.ServerInterface;
import com.lhf.server.interfaces.UserListener;

public class Server implements ServerInterface, ConnectionListener {
    protected final GameEventProcessorID gameEventProcessorID;
    protected Game game;
    protected UserManager userManager;
    protected ClientManager clientManager;
    protected transient Logger logger;
    protected Set<UserListener> userListeners;
    protected Map<AMessageType, CommandHandler> acceptedCommands;

    public Server() throws IOException {
        this.gameEventProcessorID = new GameEventProcessorID();
        this.logger = Logger.getLogger(this.getClass().getName());
        this.userManager = new UserManager();
        this.userListeners = new LinkedHashSet<>();
        this.clientManager = new ClientManager();
        this.acceptedCommands = new EnumMap<>(AMessageType.class);
        this.acceptedCommands.put(AMessageType.EXIT, new ExitHandler());
        this.acceptedCommands.put(AMessageType.CREATE, new CreateHandler());
        this.acceptedCommands = Collections.unmodifiableMap(this.acceptedCommands);
        this.game = new GameBuilder().setServer(this).setDefaults().build(userManager);
        this.logger.exiting(this.getClass().getName(), "NoArgConstructor", "NoArgConstructor");
    }

    public Server(@NotNull UserManager userManager, @NotNull ClientManager clientManager,
            @NotNull GameBuilder gameBuilder) throws FileNotFoundException {
        this.gameEventProcessorID = new GameEventProcessorID();
        this.logger = Logger.getLogger(this.getClass().getName());
        this.userManager = userManager;
        this.userListeners = new LinkedHashSet<>();
        this.clientManager = clientManager;
        this.acceptedCommands = new EnumMap<>(AMessageType.class);
        this.acceptedCommands.put(AMessageType.EXIT, new ExitHandler());
        this.acceptedCommands.put(AMessageType.CREATE, new CreateHandler());
        this.acceptedCommands = Collections.unmodifiableMap(this.acceptedCommands);
        this.game = null;
        if (gameBuilder != null) {
            this.game = gameBuilder.setServer(this).build(userManager);
        }
        this.logger.exiting(this.getClass().getName(), "ArgConstructor", "ArgConstructor");
    }

    public Client startClient(Client client) {
        this.logger.log(Level.FINER, "Sending welcome");
        client.setSuccessor(this);
        Client.eventAccepter.accept(client, WelcomeEvent.getWelcomeBuilder().Build());
        return client;
    }

    public void start() {
    }

    @Override
    public void registerCallback(UserListener listener) {
        this.userListeners.add(listener);
    }

    @Override
    public void unregisterCallback(UserListener listener) {
        this.userListeners.remove(listener);
    }

    private void removeClient(ClientID id) {
        try {
            logger.log(Level.FINER, "Removing Client " + id);
            clientManager.removeClient(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeUser(UserID id) {
        logger.log(Level.FINER, "Removing User " + id);
        userManager.removeUser(id);
    }

    @Override
    public void clientConnected(ClientID id) {
        logger.log(Level.INFO, "User connected");
        clientManager.getUserForClient(id).ifPresent(userID -> {
            for (UserListener listener : userListeners) {
                listener.userConnected(userID);
            }
        });
    }

    /**
     * This will notify other created Users that a User has left.
     * 
     * @param id id of the User who has left
     */
    @Override
    public void clientLeft(ClientID id) {
        logger.entering(this.getClass().getName(), "userLeft()", id);
        clientManager.getUserForClient(id).ifPresent(userID -> {
            for (UserListener listener : userListeners) {
                listener.userLeft(userID);
            }
            userManager.removeUser(userID);
        });
    }

    @Override
    public void clientConnectionTerminated(ClientID id) {
        logger.entering(this.getClass().getName(), "connectionTerminated()", id);
        clientLeft(id);
        removeClient(id);
    }

    @Override
    public void setSuccessor(CommandChainHandler successor) {
        // Server is IT, the buck stops here
        logger.log(Level.WARNING, "Attempted to put a successor on the Server");
    }

    @Override
    public CommandChainHandler getSuccessor() {
        // Server is IT, the buck stops here
        return null;
    }

    @Override
    public GameEventProcessorID getEventProcessorID() {
        return this.gameEventProcessorID;
    }

    @Override
    public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
        return Collections.unmodifiableMap(this.acceptedCommands);
    }

    @Override
    public synchronized void log(Level logLevel, String logMessage) {
        this.logger.log(logLevel, logMessage);
    }

    @Override
    public synchronized void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.logger.log(logLevel, logMessageSupplier);
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return ctx;
    }

    @Override
    public Collection<GameEventProcessor> getGameEventProcessors() {
        return Set.of(this.game);
    }

    protected class ExitHandler implements ServerCommandHandler {
        private static final String helpString = "Disconnect and leave Ibaif!";

        @Override
        public AMessageType getHandleType() {
            return AMessageType.EXIT;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(ExitHandler.helpString);
        }

        @Override
        public boolean isEnabled(CommandContext ctx) {
            return ctx != null;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == AMessageType.EXIT) {
                Server.this.logger.log(Level.INFO, "client " + ctx.getClient().toString() + " is exiting");
                Client ch = Server.this.clientManager.getConnection(ctx.getClient().getClientID());

                if (ctx.getUserID() != null) {
                    Server.this.game.userLeft(ctx.getUserID());
                    User leaving = Server.this.userManager.getUser(ctx.getUserID());
                    Server.this.userManager.removeUser(ctx.getUserID());
                    ctx.receive(UserLeftEvent.getBuilder().setUser(leaving).setNotBroadcast().Build());
                } else {
                    if (ch != null) {
                        ctx.receive(UserLeftEvent.getBuilder().setNotBroadcast().Build());
                    }
                }

                try {
                    Server.this.clientManager.removeClient(ctx.getClient().getClientID()); // ch is killed in
                                                                                           // here
                } catch (IOException e) {
                    Server.this.logger.log(Level.WARNING, "While removing client", e);
                }
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler(CommandContext ctx) {
            return Server.this;
        }

    }

    protected class CreateHandler implements ServerCommandHandler {
        private static final String helpString = "Create a character in Ibaif!";

        @Override
        public AMessageType getHandleType() {
            return AMessageType.CREATE;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(CreateHandler.helpString);
        }

        @Override
        public boolean isEnabled(CommandContext ctx) {
            return ctx != null && ctx.getUserID() == null; // user not yet created
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == this.getHandleType()) {
                CreateInMessage msg = new CreateInMessage(cmd);
                if (Server.this.userManager.getForbiddenUsernames().contains(msg.getUsername())) {
                    ctx.receive(BadUserDuplicationEvent.getBuilder().Build());
                    return ctx.handled();
                }
                User user = Server.this.userManager.addUser(msg, ctx.getClient());
                if (user == null) {
                    ctx.receive(BadUserDuplicationEvent.getBuilder().Build());
                    return ctx.handled();
                }
                user.setSuccessor(Server.this);
                Client client = Server.this.clientManager.getConnection(ctx.getClient().getClientID());
                Server.this.clientManager.addUserForClient(client.getClientID(), user.getUserID());
                client.setSuccessor(user);
                ctx.setUser(user);
                Server.this.game.addNewPlayerToGame(user, msg.vocationRequest());
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler(CommandContext ctx) {
            return Server.this;
        }

    }

}
