package com.lhf.game;

import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.creature.Player;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.VocationFactory;
import com.lhf.game.events.GameEventContext;
import com.lhf.game.events.GameEventHandler;
import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;
import com.lhf.game.events.messages.out.ListPlayersMessage;
import com.lhf.game.magic.ThirdPower;
import com.lhf.game.map.DMRoom;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.DungeonBuilder;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.client.user.UserManager;
import com.lhf.server.interfaces.NotNull;
import com.lhf.server.interfaces.ServerInterface;
import com.lhf.server.interfaces.UserListener;

public class Game implements UserListener, GameEventHandler {
	private transient GameEventHandler successor;
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
		Dungeon dungeon = DungeonBuilder.buildStaticDungeon(this.thirdPower, this.aiRunner);
		this.controlRoom = DMRoom.DMRoomBuilder.buildDefault(aiRunner, new ConversationManager());
		this.controlRoom.addDungeon(dungeon);
		this.controlRoom.setSuccessor(this.thirdPower);
		this.successor = server;
		this.server = server;
		if (this.server != null) {
			this.server.registerCallback(this);
		}
		this.userManager = userManager;
		this.logger.log(Level.INFO, "Created Game");
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
		this.logger.log(Level.INFO, "Created Game");
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
				builder.setController(user);
				Player player = builder.build();
				this.controlRoom.addNewPlayer(player);
				return;
			}
		}
		this.controlRoom.addUser(user);
	}

	private GameEventContext.Reply handleListPlayersMessage(GameEventContext ctx, Command cmd) {
		ctx.sendMsg(ListPlayersMessage.getBuilder().setPlayerNames(this.userManager.getAllUsernames()));
		return ctx.handled();
	}

	@Override
	public void setSuccessor(GameEventHandler successor) {
		this.successor = successor;
	}

	@Override
	public GameEventHandler getSuccessor() {
		return this.successor;
	}

	@Override
	public Map<CommandMessage, String> getCommands(GameEventContext ctx) {
		Map<CommandMessage, String> helps = new EnumMap<>(CommandMessage.class);
		helps.put(CommandMessage.PLAYERS, "List the players currently in the game.");
		return ctx.addHelps(helps);
	}

	@Override
	public GameEventContext addSelfToContext(GameEventContext ctx) {
		return ctx;
	}

	@Override
	public GameEventContext.Reply handleMessage(GameEventContext ctx, GameEvent msg) {
		ctx = this.addSelfToContext(ctx);
		if (msg.getGameEventType() == CommandMessage.PLAYERS) {
			return this.handleListPlayersMessage(ctx, msg);
		}
		return GameEventHandler.super.handleMessage(ctx, msg);
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
