package com.lhf.game;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.creature.Player;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.VocationFactory;
import com.lhf.game.magic.ThirdPower;
import com.lhf.game.map.DMRoom;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.DungeonBuilder;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.events.PlayersListedEvent;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.client.user.UserManager;
import com.lhf.server.interfaces.NotNull;
import com.lhf.server.interfaces.ServerInterface;
import com.lhf.server.interfaces.UserListener;

public class Game implements UserListener, CommandChainHandler {
	private transient CommandChainHandler successor;
	private ServerInterface server;
	private UserManager userManager;
	private final Logger logger;
	private final ThirdPower thirdPower;
	private DMRoom controlRoom;
	private final GroupAIRunner groupAiRunner;
	private final ConversationManager conversationManager;
	private final StatblockManager statblockManager;
	private Map<CommandMessage, CommandHandler> commands;
	private final GameEventProcessorID gameEventProcessorID;

	public Game(ServerInterface server, UserManager userManager) throws FileNotFoundException {
		this.gameEventProcessorID = new GameEventProcessorID();
		this.logger = Logger.getLogger(this.getClass().getName());
		this.groupAiRunner = new GroupAIRunner(true);
		this.thirdPower = new ThirdPower(this, null);
		this.conversationManager = new ConversationManager();
		this.statblockManager = new StatblockManager();
		Dungeon dungeon = DungeonBuilder.buildStaticDungeon(this.thirdPower, this.groupAiRunner);
		this.controlRoom = DMRoom.DMRoomBuilder.buildDefault(groupAiRunner, new ConversationManager());
		this.controlRoom.addDungeon(dungeon);
		this.controlRoom.setSuccessor(this.thirdPower);
		this.successor = server;
		this.server = server;
		if (this.server != null) {
			this.server.registerCallback(this);
		}
		this.userManager = userManager;
		this.logger.log(Level.INFO, "Created Game");
		this.commands = new EnumMap<>(CommandMessage.class);
		this.commands.put(CommandMessage.PLAYERS, new PlayersHandler());
	}

	public Game(ServerInterface server, UserManager userManager, GroupAIRunner aiRunner, @NotNull Dungeon dungeon)
			throws FileNotFoundException {
		this.gameEventProcessorID = new GameEventProcessorID();
		this.logger = Logger.getLogger(this.getClass().getName());
		this.groupAiRunner = aiRunner;
		this.thirdPower = new ThirdPower(this, null);
		this.conversationManager = new ConversationManager();
		this.statblockManager = new StatblockManager();
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
		this.commands = new EnumMap<>(CommandMessage.class);
		this.commands.put(CommandMessage.PLAYERS, new PlayersHandler());
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

	@Override
	public void setSuccessor(CommandChainHandler successor) {
		this.successor = successor;
	}

	@Override
	public CommandChainHandler getSuccessor() {
		return this.successor;
	}

	@Override
	public GameEventProcessorID getEventProcessorID() {
		return this.gameEventProcessorID;
	}

	@Override
	public Collection<GameEventProcessor> getGameEventProcessors() {
		return Set.of(this.controlRoom);
	}

	protected class PlayersHandler implements CommandHandler {
		private static final String helpString = "List the players currently in the game.";

		@Override
		public CommandMessage getHandleType() {
			return CommandMessage.PLAYERS;
		}

		@Override
		public Optional<String> getHelp(CommandContext ctx) {
			return Optional.of(PlayersHandler.helpString);
		}

		@Override
		public Predicate<CommandContext> getEnabledPredicate() {
			return PlayersHandler.defaultPredicate;
		}

		@Override
		public Reply handleCommand(CommandContext ctx, Command cmd) {
			ctx.receive(PlayersListedEvent.getBuilder().setPlayerNames(Game.this.userManager.getAllUsernames()));
			return ctx.handled();
		}

		@Override
		public CommandChainHandler getChainHandler() {
			return Game.this;
		}

	}

	@Override
	public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
		return Collections.unmodifiableMap(this.commands);
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

	public GroupAIRunner getGroupAiRunner() {
		return groupAiRunner;
	}

	public ThirdPower getThirdPower() {
		return thirdPower;
	}

	public ConversationManager getConversationManager() {
		return conversationManager;
	}

	public StatblockManager getStatblockManager() {
		return statblockManager;
	}

	public void setServer(ServerInterface server) {
		// TODO: unregister from old server?
		this.server = server;
		this.setSuccessor(server);
		if (this.server != null) {
			this.server.registerCallback(this);
		}
	}

	@Override
	public String getColorTaggedName() {
		return this.getStartTag() + "Game" + this.getEndTag();
	}

	@Override
	public String getEndTag() {
		return "</Game>";
	}

	@Override
	public String getStartTag() {
		return "<Game>";
	}

}
