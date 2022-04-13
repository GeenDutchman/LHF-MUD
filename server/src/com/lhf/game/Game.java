package com.lhf.game;

import com.lhf.game.creature.Player;
import com.lhf.game.magic.ThirdPower;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.DungeonBuilder;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.client.user.UserManager;
import com.lhf.server.interfaces.ServerInterface;
import com.lhf.server.interfaces.UserListener;
import com.lhf.server.messages.Messenger;
import com.lhf.server.messages.in.*;
import com.lhf.server.messages.out.GameMessage;
import com.lhf.server.messages.out.NewInMessage;
import com.lhf.server.messages.out.UserLeftMessage;
import com.lhf.server.messages.out.WelcomeMessage;
import com.lhf.server.interfaces.NotNull;

import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Game implements UserListener {
	private ServerInterface server;
	private UserManager userManager;
	private Dungeon dungeon;
	private Logger logger;
	private Messenger messenger;
	private ThirdPower thirdPower;

	public Game(ServerInterface server, UserManager userManager) throws FileNotFoundException {
		this.logger = Logger.getLogger(this.getClass().getName());
		dungeon = DungeonBuilder.buildStaticDungeon();
		this.thirdPower = new ThirdPower(dungeon);
		this.server = server;
		this.userManager = userManager;
		this.userManager.setGame(this);
		this.messenger = new Messenger(server, dungeon);
		dungeon.setMessenger(this.messenger);
		server.registerCallback(this);
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
		removePlayer(id);
		server.sendMessageToAll(new UserLeftMessage(userManager.getUser(id)));
	}

	@Override
	public void messageReceived(UserID id, @NotNull InMessage msg) {
		this.logger.entering(this.getClass().toString(), "messageReceived()");
		this.logger.fine("Message:" + msg + " for:" + id);
		User user = userManager.getUser(id);
		if (msg instanceof ShoutMessage) {
			this.logger.finer("Shouting");
			server.sendMessageToAll(
					new com.lhf.server.messages.out.ShoutMessage(((ShoutMessage) msg).getMessage(),
							user));
		}
		if (msg instanceof SayMessage) {
			this.logger.finer("Saying");
			messenger.sendMessageToAllInRoom(
					new com.lhf.server.messages.out.SayMessage(((SayMessage) msg).getMessage(),
							user),
					id);
		}
		if (msg instanceof CastMessage) {
			this.logger.finer("Casting");
			CastMessage cMessage = (CastMessage) msg;
			messenger.sendMessageToUser(
					new GameMessage(this.thirdPower.onCastCommand(id, cMessage.getInvocation(), cMessage.getTarget())),
					id);
		}
		if (msg instanceof TellMessage) {
			this.logger.finer("Telling");
			TellMessage tellMsg = (TellMessage) msg;
			boolean success;
			if (!id.getUsername().equals(tellMsg.getTarget().getUsername())) {
				success = server.sendMessageToUser(
						new com.lhf.server.messages.out.TellMessage(id, tellMsg.getMessage()),
						tellMsg.getTarget());
				if (success) {
					server.sendMessageToUser(new com.lhf.server.messages.out.TellMessage(id,
							tellMsg.getMessage()), id);
				} else {
					server.sendMessageToUser(
							new com.lhf.server.messages.out.WrongUserMessage(
									tellMsg.getTarget().getUsername()),
							id);
				}
			}
		}
		if (msg instanceof ListPlayersMessage) {
			this.logger.finer("Listing Players");
			server.sendMessageToUser(
					new com.lhf.server.messages.out.ListPlayersMessage(
							userManager.getAllUsernames()),
					id);
		}
		if (msg instanceof ExitMessage) {
			this.logger.finer("Exiting");
			server.removeUser(id);
		}

		if (msg instanceof GoMessage) {
			AtomicBoolean didMove = new AtomicBoolean(false);

			server.sendMessageToUser(
					new GameMessage(
							dungeon.goCommand(id, ((GoMessage) msg).getDirection(),
									didMove)),
					id);
			if (didMove.get()) {
				messenger.sendMessageToAllInRoomExceptPlayer(
						new GameMessage(
								id.getUsername() + " has entered the room."),
						id);
			}
		}

		if (msg instanceof ExamineMessage) {
			server.sendMessageToUser(
					new GameMessage(
							dungeon.examineCommand(id, ((ExamineMessage) msg).getThing())),
					id);
		}
		if (msg instanceof LookMessage) {
			server.sendMessageToUser(
					new GameMessage(
							dungeon.lookCommand(id)),
					id);
		}
		if (msg instanceof InteractMessage) {
			server.sendMessageToUser(
					new GameMessage(
							dungeon.interactCommand(id,
									((InteractMessage) msg).getObject())),
					id);
		}

		if (msg instanceof TakeMessage) {
			server.sendMessageToUser(
					new GameMessage(
							dungeon.takeCommand(id, ((TakeMessage) msg).getTarget())),
					id);
		}

		if (msg instanceof DropMessage) {
			server.sendMessageToUser(
					new GameMessage(
							dungeon.dropCommand(id, ((DropMessage) msg).getTarget())),
					id);
			messenger.sendMessageToAllInRoomExceptPlayer(
					new GameMessage("An item just dropped to the floor.\r\n"), id);
		}

		if (msg instanceof EquipMessage) {
			server.sendMessageToUser(
					new GameMessage(
							dungeon.equip(id, ((EquipMessage) msg).getItemName(),
									((EquipMessage) msg).getEquipSlot())),
					id);
		}

		if (msg instanceof UnequipMessage) {
			server.sendMessageToUser(
					new GameMessage(
							dungeon.unequip(id, ((UnequipMessage) msg).getEquipSlot(),
									((UnequipMessage) msg).getPossibleWeapon())),
					id);
		}

		if (msg instanceof InventoryMessage) {
			server.sendMessageToUser(
					new GameMessage(
							dungeon.inventory(id)),
					id);
		}

		if (msg instanceof AttackMessage) {
			dungeon.attackCommand(id, ((AttackMessage) msg).getWeapon(), ((AttackMessage) msg).getTarget());
		}

		if (msg instanceof UseMessage) {
			server.sendMessageToUser(
					new GameMessage(
							dungeon.useCommand(id, ((UseMessage) msg).getUsefulItem(),
									((UseMessage) msg).getTarget())),
					id);
		}

		if (msg instanceof StatusMessage) {
			server.sendMessageToUser(
					new GameMessage(
							dungeon.statusCommand(id)),
					id);
		}
	}

	public void addNewPlayerToGame(UserID id, String name) {
		Player newPlayer = new Player(id, name);
		dungeon.addNewPlayer(newPlayer);
		dungeon.notifyAllInRoomOfNewPlayer(id, name);
	}

	public boolean removePlayer(UserID id) {
		return dungeon.removePlayer(id);
	}
}
