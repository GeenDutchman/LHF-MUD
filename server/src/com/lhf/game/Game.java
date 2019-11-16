package com.lhf.game;

import com.lhf.game.creature.Player;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.DungeonBuilder;
import com.lhf.interfaces.ServerInterface;
import com.lhf.interfaces.UserListener;
import com.lhf.messages.in.*;
import com.lhf.messages.out.GameMessage;
import com.lhf.messages.out.NewInMessage;
import com.lhf.messages.out.UserLeftMessage;
import com.lhf.messages.out.WelcomeMessage;
import com.lhf.user.User;
import com.lhf.user.UserID;
import com.lhf.user.UserManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Game implements UserListener {
    ServerInterface server;
    UserManager userManager;
    Dungeon dungeon;
    private Logger logger;
    Messenger messenger;

    public Game(ServerInterface server, UserManager userManager) {
        this.logger = Logger.getLogger(this.getClass().getName());
        dungeon = DungeonBuilder.buildStaticDungeon();
        this.server = server;
        this.userManager = userManager;
        this.userManager.setGame(this);
        this.messenger = new Messenger(server, dungeon);
        dungeon.setMessenger(this.messenger);
        server.registerCallback((UserListener) this);
        this.logger.info("Created Game");
        server.start();
    }

    @Override
    public void userConnected(UserID id) {
        this.logger.entering(this.getClass().toString(), "userConnected()", id);
        server.sendMessageToUser(new WelcomeMessage(), id);
        server.sendMessageToAllExcept(new NewInMessage(), id);
    }

    @Override
    public void userLeft(UserID id) {
        this.logger.entering(this.getClass().toString(), "userLeft()", id);
        server.sendMessageToAll(new UserLeftMessage(userManager.getUser(id)));
    }

    @Override
    public void messageReceived(UserID id, @NotNull InMessage msg) {
        this.logger.entering(this.getClass().toString(), "messageReceived()");
        this.logger.fine("Message:" + msg + " for:" + id);
        User user = userManager.getUser(id);
        if (msg instanceof ShoutMessage) {
            this.logger.finer("Shouting");
            server.sendMessageToAll(new com.lhf.messages.out.ShoutMessage(((ShoutMessage) msg).getMessage(), user));
        }
        if (msg instanceof SayMessage) {
            this.logger.finer("Saying");
            messenger.sendMessageToAllInRoom(new com.lhf.messages.out.SayMessage(((SayMessage) msg).getMessage(), user), id);
        }
        if (msg instanceof TellMessage) {
            this.logger.finer("Telling");
            TellMessage tellMsg = (TellMessage) msg;
            boolean success = server.sendMessageToUser(new com.lhf.messages.out.TellMessage(id, tellMsg.getMessage()), tellMsg.getTarget());
            if (!success) {
                server.sendMessageToUser(new com.lhf.messages.out.WrongUserMessage("Message not sent. Check the username."), id);
            }
        }
        if (msg instanceof ExitMessage) {
            this.logger.finer("Exiting");
            server.removeUser(id);
        }

        if (msg instanceof GoMessage) {
            AtomicBoolean didMove = new AtomicBoolean(false);

            server.sendMessageToUser(
                    new GameMessage(
                        dungeon.goCommand(id, ((GoMessage) msg).getDirection(), didMove)
                    ),
                    id
            );
            if (didMove.get()) {
                messenger.sendMessageToAllInRoomExceptPlayer(
                        new GameMessage(
                                id.getUsername() + " has entered the room."
                        ),
                        id
                );
            }
        }

        if (msg instanceof ExamineMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.examineCommand(id, ((ExamineMessage) msg).getThing())
                    ),
                    id
            );
        }
        if (msg instanceof LookMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.lookCommand(id)
                    ),
                    id
            );
        }
        if (msg instanceof InteractMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.interactCommand(id, ((InteractMessage) msg).getObject())
                    ),
                    id
            );
        }

        if (msg instanceof TakeMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.takeCommand(id, ((TakeMessage) msg).getTarget())
                    ),
                    id
            );
        }

        if (msg instanceof DropMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.dropCommand(id, ((DropMessage) msg).getTarget())
                    ),
                    id
            );
            messenger.sendMessageToAllInRoomExceptPlayer(new GameMessage("An item just dropped to the floor."), id);
        }

        if (msg instanceof EquipMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.equip(id, ((EquipMessage) msg).getItemName(), ((EquipMessage) msg).getEquipSlot())
                    ),
                    id
            );
        }

        if (msg instanceof UnequipMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.unequip(id, ((UnequipMessage) msg).getEquipSlot(), ((UnequipMessage) msg).getPossibleWeapon())
                    ),
                    id
            );
        }

        if (msg instanceof InventoryMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.inventory(id)
                    ),
                    id
            );
        }

        if (msg instanceof AttackMessage) {
            dungeon.attackCommand(id, ((AttackMessage) msg).getWeapon(), ((AttackMessage) msg).getTarget());
        }

        if (msg instanceof UseMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.useCommand(id, ((UseMessage) msg).getUsefulItem(), ((UseMessage) msg).getTarget())
                    ),
                    id
            );
        }

        if (msg instanceof StatusMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.statusCommand(id)
                    ),
                    id
            );
        }
    }



    public void addNewPlayerToGame(UserID id, String name) {
        Player newPlayer = new Player(id, name);
        dungeon.addNewPlayer(newPlayer);
    }
}
