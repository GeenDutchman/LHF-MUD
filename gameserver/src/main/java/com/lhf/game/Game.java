package com.lhf.game;

import com.lhf.game.creature.Player;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.magic.ThirdPower;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.DungeonBuilder;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.*;
import com.lhf.messages.out.GameMessage;
import com.lhf.messages.out.ListPlayersMessage;
import com.lhf.messages.out.NewInMessage;
import com.lhf.messages.out.SpawnMessage;
import com.lhf.messages.out.UserLeftMessage;
import com.lhf.messages.out.WelcomeMessage;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.client.user.UserManager;
import com.lhf.server.interfaces.ServerInterface;
import com.lhf.server.interfaces.UserListener;
import com.lhf.server.interfaces.NotNull;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Game implements UserListener, MessageHandler {
	private MessageHandler successor;
	private ServerInterface server;
	private UserManager userManager;
	private Dungeon dungeon;
	private Logger logger;
	private ThirdPower thirdPower;

	public Game(ServerInterface server, UserManager userManager) throws FileNotFoundException {
		this.logger = Logger.getLogger(this.getClass().getName());
		dungeon = DungeonBuilder.buildStaticDungeon(this);
		this.thirdPower = new ThirdPower(dungeon);
		this.successor = server;
		this.server = server;
		this.server.registerCallback(this);
		this.userManager = userManager;
		this.userManager.setGame(this);
		this.logger.info("Created Game");
	}

	@Override
	public void userConnected(UserID id) {
		this.logger.entering(this.getClass().toString(), "userConnected()", id);
		// this.dungeon.sendMessageToAllExcept(new NewInMessage(), id);
	}

	@Override
	public void userLeft(UserID id) {
		this.logger.entering(this.getClass().toString(), "userLeft()", id);
		this.dungeon.removePlayer(id);
		this.dungeon.sendMessageToAll(new UserLeftMessage(userManager.getUser(id)));
	}

	public void addNewPlayerToGame(UserID id, String name) {
		Player newPlayer = new Player(id, name);
		dungeon.addNewPlayer(newPlayer);
		dungeon.sendMessageToAllInRoomExcept(null, new SpawnMessage(newPlayer.getName()), newPlayer.getName());
	}

	private Boolean handleListPlayersMessage(CommandContext ctx, Command cmd) {
		ctx.sendMsg(new ListPlayersMessage(this.userManager.getAllUsernames()));
		return true;
	}

	@Override
	public void setSuccessor(MessageHandler successor) {
		if (this.successor == null) {
			this.successor = successor;
		} else if (successor != null && successor != this.successor) {
			successor.setSuccessor(this.successor); // maintain the link!
			this.successor = successor;
		}
	}

	@Override
	public MessageHandler getSuccessor() {
		return this.successor;
	}

	@Override
	public Map<CommandMessage, String> getCommands() {
		HashMap<CommandMessage, String> helps = new HashMap<>();
		helps.put(CommandMessage.PLAYERS, "List the players currently in the game.");
		return helps; // TODO: maybe handle exit a bit, but pass it on?
	}

	@Override
	public Boolean handleMessage(CommandContext ctx, Command msg) {
		if (msg.getType() == CommandMessage.PLAYERS) {
			return this.handleListPlayersMessage(ctx, msg);
		}
		return MessageHandler.super.handleMessage(ctx, msg);
	}

}