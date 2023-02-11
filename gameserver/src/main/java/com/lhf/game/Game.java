package com.lhf.game;

import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

import com.lhf.game.creature.Player;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.VocationFactory;
import com.lhf.game.magic.ThirdPower;
import com.lhf.game.map.DMRoom;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.DungeonBuilder;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.ListPlayersMessage;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.client.user.UserManager;
import com.lhf.server.interfaces.NotNull;
import com.lhf.server.interfaces.ServerInterface;
import com.lhf.server.interfaces.UserListener;

public class Game implements UserListener, MessageHandler {
	private MessageHandler successor;
	private ServerInterface server;
	private UserManager userManager;
	private Logger logger;
	private ThirdPower thirdPower;
	private DMRoom controlRoom;
	private AIRunner aiRunner;

	public Game(ServerInterface server, UserManager userManager) throws FileNotFoundException {
		this.logger = Logger.getLogger(this.getClass().getName());
		this.aiRunner = new GroupAIRunner(true);
		this.thirdPower = new ThirdPower(this, null);
		Dungeon dungeon = DungeonBuilder.buildStaticDungeon(null, this.aiRunner);
		this.controlRoom = DMRoom.DMRoomBuilder.buildDefault(aiRunner, new ConversationManager());
		this.controlRoom.addDungeon(dungeon);
		this.controlRoom.setSuccessor(this.thirdPower);
		this.successor = server;
		this.server = server;
		if (this.server != null) {
			this.server.registerCallback(this);
		}
		this.userManager = userManager;
		this.logger.info("Created Game");
	}

	public Game(ServerInterface server, UserManager userManager, AIRunner aiRunner, @NotNull Dungeon dungeon)
			throws FileNotFoundException {
		this.logger = Logger.getLogger(this.getClass().getName());
		this.aiRunner = aiRunner;
		this.thirdPower = new ThirdPower(this, null);
		this.controlRoom = DMRoom.DMRoomBuilder.buildDefault(aiRunner, new ConversationManager());
		this.controlRoom.addDungeon(dungeon);
		this.controlRoom.setSuccessor(this.thirdPower);
		this.successor = server;
		this.server = server;
		if (this.server != null) {
			this.server.registerCallback(this);
		}
		this.userManager = userManager;
		this.logger.info("Created Game");
	}

	@Override
	public void userConnected(UserID id) {
		this.logger.entering(this.getClass().getName(), "userConnected()", id);
		// this.dungeon.sendMessageToAllExcept(new NewInMessage(), id);
	}

	@Override
	public void userLeft(UserID id) {
		this.logger.entering(this.getClass().getName(), "userLeft()", id);
		this.controlRoom.userExitSystem(userManager.getUser(id));
	}

	public void addNewPlayerToGame(User user, String vocationRequest) {
		if (vocationRequest != null && vocationRequest.length() > 0) {
			Vocation selected = VocationFactory.getVocation(vocationRequest);
			if (selected != null) {
				Player.PlayerBuilder builder = Player.PlayerBuilder.getInstance(user);
				builder.setVocation(selected);
				Player player = builder.build();
				this.controlRoom.addNewPlayer(player);
				return;
			}
		}
		this.controlRoom.addUser(user);
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
		Map<CommandMessage, String> helps = new EnumMap<>(CommandMessage.class);
		helps.put(CommandMessage.PLAYERS, "List the players currently in the game.");
		return helps;
	}

	@Override
	public CommandContext addSelfToContext(CommandContext ctx) {
		return ctx;
	}

	@Override
	public boolean handleMessage(CommandContext ctx, Command msg) {
		ctx = this.addSelfToContext(ctx);
		if (msg.getType() == CommandMessage.PLAYERS) {
			return this.handleListPlayersMessage(ctx, msg);
		}
		return MessageHandler.super.handleMessage(ctx, msg);
	}

	public void setServer(ServerInterface server) {
		// TODO: unregister from old server?
		this.server = server;
		this.setSuccessor(server);
		if (this.server != null) {
			this.server.registerCallback(this);
		}
	}

}
