package com.lhf.game;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.lhf.game.creature.Player;
import com.lhf.game.magic.ThirdPower;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.DungeonBuilder;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.ListPlayersMessage;
import com.lhf.messages.out.UserLeftMessage;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.client.user.UserManager;
import com.lhf.server.interfaces.ServerInterface;
import com.lhf.server.interfaces.UserListener;

public class Game implements UserListener, MessageHandler {
	private MessageHandler successor;
	private ServerInterface server;
	private UserManager userManager;
	private Dungeon dungeon;
	private Logger logger;
	private ThirdPower thirdPower;

	public Game(ServerInterface server, UserManager userManager) throws FileNotFoundException {
		this.logger = Logger.getLogger(this.getClass().getName());
		this.thirdPower = new ThirdPower(this);
		this.dungeon = DungeonBuilder.buildStaticDungeon(this.thirdPower);
		this.successor = server;
		this.server = server;
		this.server.registerCallback(this);
		this.userManager = userManager;
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

	public void addNewPlayerToGame(User user) {
		Player newPlayer = new Player(user);
		dungeon.addNewPlayer(newPlayer);
	}

	private Boolean handleListPlayersMessage(CommandContext ctx, Command cmd) {
		ctx.sendMsg(new ListPlayersMessage(this.userManager.getAllUsernames()));
		return true;
	}

	@Override
	public void setSuccessor(MessageHandler successor) {
		this.successor = successor;
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
