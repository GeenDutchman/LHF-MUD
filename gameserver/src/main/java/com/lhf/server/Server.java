package com.lhf.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.Game;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.in.CreateInMessage;
import com.lhf.messages.out.DuplicateUserMessage;
import com.lhf.messages.out.UserLeftMessage;
import com.lhf.messages.out.WelcomeMessage;
import com.lhf.server.client.Client;
import com.lhf.server.client.ClientID;
import com.lhf.server.client.ClientManager;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.client.user.UserManager;
import com.lhf.server.interfaces.ConnectionListener;
import com.lhf.server.interfaces.NotNull;
import com.lhf.server.interfaces.ServerInterface;
import com.lhf.server.interfaces.UserListener;

public class Server implements ServerInterface, ConnectionListener {
    protected Game game;
    protected UserManager userManager;
    protected ClientManager clientManager;
    protected Logger logger;
    protected ArrayList<UserListener> userListeners;
    protected Map<CommandMessage, CommandHandler> acceptedCommands;

    public Server() throws IOException {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.userManager = new UserManager();
        this.userListeners = new ArrayList<>();
        this.clientManager = new ClientManager();
        this.acceptedCommands = new EnumMap<>(CommandMessage.class);
        this.acceptedCommands.put(CommandMessage.EXIT, new ExitHandler());
        this.acceptedCommands.put(CommandMessage.CREATE, new CreateHandler());
        this.acceptedCommands = Collections.unmodifiableMap(this.acceptedCommands);
        this.game = new Game(this, this.userManager);
        this.logger.exiting(this.getClass().getName(), "NoArgConstructor");
    }

    public Server(@NotNull UserManager userManager, @NotNull ClientManager clientManager, @NotNull Game game) {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.userManager = userManager;
        this.userListeners = new ArrayList<>();
        this.clientManager = clientManager;
        this.acceptedCommands = new EnumMap<>(CommandMessage.class);
        this.acceptedCommands.put(CommandMessage.EXIT, new ExitHandler());
        this.acceptedCommands.put(CommandMessage.CREATE, new CreateHandler());
        this.acceptedCommands = Collections.unmodifiableMap(this.acceptedCommands);
        this.game = game;
        if (game != null) {
            game.setServer(this);
        }
        this.logger.exiting(this.getClass().getName(), "ArgConstructor");
    }

    public Client startClient(Client client) {
        this.logger.log(Level.FINER, "Sending welcome");
        client.setSuccessor(this);
        client.sendMsg(WelcomeMessage.getWelcomeBuilder().Build());
        return client;
    }

    public void start() {
    }

    @Override
    public void registerCallback(UserListener listener) {
        this.userListeners.add(listener);
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
    public void setSuccessor(MessageChainHandler successor) {
        // Server is IT, the buck stops here
        logger.log(Level.WARNING, "Attempted to put a successor on the Server");
    }

    @Override
    public MessageChainHandler getSuccessor() {
        // Server is IT, the buck stops here
        return null;
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
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

    protected class ExitHandler implements ServerCommandHandler {
        private static final String helpString = "Disconnect and leave Ibaif!";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.EXIT;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(ExitHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return ExitHandler.defaultPredicate;
        }

        @Override
        public Reply handle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.EXIT) {
                Server.this.logger.log(Level.INFO, "client " + ctx.getClientID().toString() + " is exiting");
                Client ch = Server.this.clientManager.getConnection(ctx.getClientID());

                if (ctx.getUserID() != null) {
                    Server.this.game.userLeft(ctx.getUserID());
                    User leaving = Server.this.userManager.getUser(ctx.getUserID());
                    Server.this.userManager.removeUser(ctx.getUserID());
                    leaving.sendMsg(UserLeftMessage.getBuilder().setUser(leaving).setNotBroadcast().Build());
                } else {
                    if (ch != null) {
                        ch.sendMsg(UserLeftMessage.getBuilder().setNotBroadcast().Build());
                    }
                }

                try {
                    Server.this.clientManager.removeClient(ctx.getClientID()); // ch is killed in here
                } catch (IOException e) {
                    Server.this.logger.log(Level.WARNING, "While removing client", e);
                }
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public MessageChainHandler getChainHandler() {
            return Server.this;
        }

    }

    protected class CreateHandler implements ServerCommandHandler {
        private static final String helpString = "Create a character in Ibaif!";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.CREATE;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(CreateHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return CreateHandler.alreadyCreatedPredicate;
        }

        @Override
        public Reply handle(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == this.getHandleType() && cmd instanceof CreateInMessage msg) {
                if (Server.this.userManager.getAllUsernames().contains(msg.getUsername())) {
                    ctx.sendMsg(DuplicateUserMessage.getBuilder().Build());
                    return ctx.handled();
                }
                User user = Server.this.userManager.addUser(msg, ctx.getClientMessenger());
                if (user == null) {
                    ctx.sendMsg(DuplicateUserMessage.getBuilder().Build());
                    return ctx.handled();
                }
                user.setSuccessor(Server.this);
                Client client = Server.this.clientManager.getConnection(ctx.getClientID());
                Server.this.clientManager.addUserForClient(client.getClientID(), user.getUserID());
                client.setSuccessor(user);
                Server.this.game.addNewPlayerToGame(user, msg.vocationRequest());
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public MessageChainHandler getChainHandler() {
            return Server.this;
        }

    }

}
