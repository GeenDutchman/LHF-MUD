package com.lhf.game;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.creature.CreatureFactory;
import com.lhf.game.creature.Player;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.lhf.game.creature.BuildInfoManager;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.VocationFactory;
import com.lhf.game.magic.ThirdPower;
import com.lhf.game.map.DMRoom;
import com.lhf.game.map.DMRoom.DMRoomBuilder;
import com.lhf.game.map.Land;
import com.lhf.game.map.Land.LandBuilder;
import com.lhf.game.map.StandardDungeonProducer;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.events.PlayersListedEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.client.user.UserManager;
import com.lhf.server.interfaces.ServerInterface;
import com.lhf.server.interfaces.UserListener;

public class Game implements UserListener, CommandChainHandler {
	private transient CommandChainHandler successor;
	private ServerInterface server;
	private UserManager userManager;
	private final transient Logger logger;
	private final ThirdPower thirdPower;
	private DMRoom controlRoom;
	private final AIRunner aiRunner;
	private final ConversationManager conversationManager;
	private final BuildInfoManager statblockManager;
	private Map<AMessageType, CommandHandler> commands;
	private final GameEventProcessorID gameEventProcessorID;

	public static class GameBuilder {
		private ServerInterface server;
		private ThirdPower thirdPower;
		private AIRunner aiRunner;
		private ConversationManager conversationManager;
		private BuildInfoManager statblockManager;
		private DMRoomBuilder dmRoomBuilder;
		private ArrayList<Land.LandBuilder> additionalLands;

		public GameBuilder() {
			this.server = null;
			this.thirdPower = null;
			this.aiRunner = null;
			this.conversationManager = null;
			this.statblockManager = null;
			this.dmRoomBuilder = null;
			this.additionalLands = new ArrayList<>();
		}

		public ServerInterface getServer() {
			return server;
		}

		public ThirdPower getThirdPower() {
			if (this.thirdPower == null) {
				this.thirdPower = new ThirdPower(null, null);
			}
			return thirdPower;
		}

		public AIRunner getAiRunner() {
			if (this.aiRunner == null) {
				this.aiRunner = new GroupAIRunner(true);
			}
			return aiRunner;
		}

		public ConversationManager getConversationManager() {
			if (this.conversationManager == null) {
				this.conversationManager = new ConversationManager();
			}
			return conversationManager;
		}

		public BuildInfoManager getStatblockManager() {
			if (this.statblockManager == null) {
				this.statblockManager = new BuildInfoManager();
			}
			return statblockManager;
		}

		public DMRoomBuilder getDmRoomBuilder() {
			return dmRoomBuilder;
		}

		public ArrayList<Land.LandBuilder> getAdditionalLands() {
			return additionalLands;
		}

		public GameBuilder setDefaults() throws JsonIOException, JsonSyntaxException, IOException {
			this.thirdPower = new ThirdPower(null, null);
			this.aiRunner = new GroupAIRunner(true);
			this.conversationManager = new ConversationManager();
			this.statblockManager = new BuildInfoManager();
			this.additionalLands.add(0, StandardDungeonProducer.buildStaticDungeonBuilder(statblockManager));
			return this;
		}

		public GameBuilder setServer(ServerInterface server) {
			this.server = server;
			return this;
		}

		public GameBuilder setThirdPower(ThirdPower thirdPower) {
			this.thirdPower = thirdPower;
			return this;
		}

		public GameBuilder setAiRunner(AIRunner aiRunner) {
			this.aiRunner = aiRunner;
			return this;
		}

		public GameBuilder setConversationManager(ConversationManager conversationManager) {
			this.conversationManager = conversationManager;
			return this;
		}

		public GameBuilder setStatblockManager(BuildInfoManager statblockManager) {
			this.statblockManager = statblockManager;
			return this;
		}

		public GameBuilder setDmRoomBuilder(DMRoomBuilder dmRoomBuilder) {
			this.dmRoomBuilder = dmRoomBuilder;
			return this;
		}

		public GameBuilder addAdditionalLands(Land.LandBuilder additionalLand) {
			this.additionalLands.add(additionalLand);
			return this;
		}

		public GameBuilder setAdditionalLands(ArrayList<Land.LandBuilder> additionalLands) {
			this.additionalLands = additionalLands;
			return this;
		}

		public Game build(UserManager userManager) throws FileNotFoundException {
			Game game = new Game(this, userManager);
			game.setServer(server);
			if (this.thirdPower != null) {
				this.thirdPower.setSuccessor(game);
			}
			return game;
		}
	}

	public Game(GameBuilder builder, UserManager userManager) throws FileNotFoundException {
		this.gameEventProcessorID = new GameEventProcessorID();
		this.logger = Logger.getLogger(this.getClass().getName());
		this.aiRunner = builder.getAiRunner();
		this.thirdPower = builder.getThirdPower();
		this.thirdPower.setSuccessor(this);
		this.conversationManager = builder.getConversationManager();
		this.statblockManager = builder.getStatblockManager();
		DMRoom.DMRoomBuilder dmRoomBuilder = builder.getDmRoomBuilder();
		if (dmRoomBuilder == null) {
			dmRoomBuilder = DMRoom.DMRoomBuilder.buildDefault(aiRunner, conversationManager);
		}

		this.controlRoom = dmRoomBuilder.build(this.thirdPower, null, aiRunner, conversationManager, false);
		this.controlRoom.setSuccessor(this.thirdPower);
		ArrayList<LandBuilder> moreLands = builder.getAdditionalLands();
		if (moreLands != null) {
			for (LandBuilder landBuilder : moreLands) {
				if (landBuilder == null) {
					continue;
				}
				this.controlRoom.addLand(
						landBuilder.build(this.thirdPower, aiRunner, conversationManager, false));
			}
		}
		this.successor = builder.getServer();
		this.server = builder.getServer();
		if (this.server != null) {
			this.server.registerCallback(this);
		}
		this.userManager = userManager;
		this.logger.log(Level.INFO, "Created Game");
		this.commands = new EnumMap<>(AMessageType.class);
		this.commands.put(AMessageType.PLAYERS, new PlayersHandler());
	}

	@Override
	public void userConnected(UserID id) {
		this.logger.entering(this.getClass().getName(), "userConnected()", id);
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
				Player.PlayerBuildInfo builder = Player.PlayerBuildInfo.getInstance(user);
				builder.setVocation(selected);
				CreatureFactory factory = new CreatureFactory();
				Player player = factory.buildPlayer(builder);
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
		public AMessageType getHandleType() {
			return AMessageType.PLAYERS;
		}

		@Override
		public Optional<String> getHelp(CommandContext ctx) {
			return Optional.of(PlayersHandler.helpString);
		}

		@Override
		public boolean isEnabled(CommandContext ctx) {
			return ctx != null;
		}

		@Override
		public Reply handleCommand(CommandContext ctx, Command cmd) {
			ctx.receive(PlayersListedEvent.getBuilder().setPlayerNames(Game.this.userManager.getAllUsernames()));
			return ctx.handled();
		}

		@Override
		public CommandChainHandler getChainHandler(CommandContext ctx) {
			return Game.this;
		}

	}

	@Override
	public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
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

	public AIRunner getAiRunner() {
		return aiRunner;
	}

	public ThirdPower getThirdPower() {
		return thirdPower;
	}

	public ConversationManager getConversationManager() {
		return conversationManager;
	}

	public BuildInfoManager getStatblockManager() {
		return statblockManager;
	}

	public void setServer(ServerInterface server) {
		if (this.server != null) {
			this.server.unregisterCallback(this);
		}
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
