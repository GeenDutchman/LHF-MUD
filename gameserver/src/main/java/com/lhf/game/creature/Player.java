package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.logging.Level;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.interfaces.NotNull;

public class Player extends Creature {
    private User user;

    public static class PlayerBuildInfo implements ICreatureBuildInfo {
        private final String className;
        private User user;
        private final CreatureBuildInfo creatureBuilder;
        protected final CreatureBuilderID id;

        private PlayerBuildInfo(User user) {
            this.className = this.getClass().getName();
            this.creatureBuilder = new CreatureBuildInfo().setFaction(CreatureFaction.PLAYER)
                    .setName(user.getUsername());
            this.id = new CreatureBuilderID();
            this.user = user;
        }

        public static PlayerBuildInfo getInstance(User user) {
            return new PlayerBuildInfo(user);
        }

        public User getUser() {
            return user;
        }

        @Override
        public String getClassName() {
            return this.className;
        }

        @Override
        public CreatureBuilderID getCreatureBuilderID() {
            return this.id;
        }

        public PlayerBuildInfo setUser(User user) {
            if (user == null) {
                throw new IllegalArgumentException("Cannot set a null user!");
            }
            this.user = user;
            this.setName(this.user.getUsername());
            return this;
        }

        public PlayerBuildInfo setName(String name) {
            creatureBuilder.setName(name);
            return this;
        }

        public String getName() {
            return creatureBuilder.getName();
        }

        public PlayerBuildInfo setFaction(CreatureFaction faction) {
            creatureBuilder.setFaction(faction);
            return this;
        }

        public CreatureFaction getFaction() {
            return creatureBuilder.getFaction();
        }

        public PlayerBuildInfo setVocation(Vocation vocation) {
            creatureBuilder.setVocation(vocation);
            return this;
        }

        public PlayerBuildInfo setVocation(VocationName vocationName) {
            creatureBuilder.setVocation(vocationName);
            return this;
        }

        public PlayerBuildInfo setVocationLevel(int level) {
            creatureBuilder.setVocationLevel(level);
            return this;
        }

        public VocationName getVocation() {
            return creatureBuilder.getVocation();
        }

        public Integer getVocationLevel() {
            return creatureBuilder.getVocationLevel();
        }

        public PlayerBuildInfo setStatblock(Statblock statblock) {
            creatureBuilder.setStatblock(statblock);
            return this;
        }

        public PlayerBuildInfo setStatblockName(String statblockName) {
            creatureBuilder.setStatblockName(statblockName);
            return this;
        }

        public String getStatblockName() {
            return creatureBuilder.getStatblockName();
        }

        public Statblock loadStatblock(StatblockManager statblockManager) throws FileNotFoundException {
            return creatureBuilder.loadStatblock(statblockManager);
        }

        @Override
        public Statblock loadBlankStatblock() {
            return creatureBuilder.loadBlankStatblock();
        }

        public PlayerBuildInfo useBlankStatblock() {
            creatureBuilder.useBlankStatblock();
            return this;
        }

        public Statblock getStatblock() {
            return creatureBuilder.getStatblock();
        }

        public PlayerBuildInfo setCorpse(Corpse corpse) {
            creatureBuilder.setCorpse(corpse);
            return this;
        }

        public Corpse getCorpse() {
            return creatureBuilder.getCorpse();
        }

        @Override
        public void acceptBuildInfoVisitor(ICreatureBuildInfoVisitor visitor) {
            visitor.visit(this);
        }

        // @Override
        // public Player build(CommandInvoker controller,
        // CommandChainHandler successor, StatblockManager statblockManager,
        // UnaryOperator<PlayerBuilder> composedLazyLoaders)
        // throws FileNotFoundException {

        // if (statblockManager != null) {
        // this.loadStatblock(statblockManager);
        // }
        // if (composedLazyLoaders != null) {
        // composedLazyLoaders.apply(this.getThis());
        // }
        // return new Player(this.getThis(), controller, successor,
        // this.getStatblock());
        // }

        // public Player build(User user, CommandChainHandler successor,
        // StatblockManager statblockManager,
        // UnaryOperator<PlayerBuilder> composedLazyLoaders) throws
        // FileNotFoundException {
        // this.setUser(user);
        // return this.build(user, successor, statblockManager, composedLazyLoaders);
        // }

        // public Player build(CommandChainHandler successor) {
        // User foundUser = this.getUser();
        // if (foundUser == null) {
        // throw new IllegalStateException("Player cannot be created with null user!");
        // }
        // Statblock currStatBlock = this.getStatblock();
        // if (currStatBlock == null) {
        // VocationName currVocation = this.getVocation();
        // if (currVocation == null) {
        // throw new IllegalStateException(
        // "Must have a statblock or a Vocation from which to define the statblock!");
        // }
        // currStatBlock = currVocation.createNewDefaultStatblock("Player").build();
        // this.setStatblock(currStatBlock);
        // }
        // return new Player(this.getThis(), foundUser, successor, this.getStatblock());
        // }
    }

    public Player(PlayerBuildInfo builder,
            @NotNull CommandInvoker controller, CommandChainHandler successor,
            @NotNull Statblock statblock) {
        super(builder, controller, successor, statblock);
        this.user = builder.getUser();
        this.user.setSuccessor(this);
    }

    public static PlayerBuildInfo getPlayerBuilder(User user) {
        return new PlayerBuildInfo(user);
    }

    @Override
    public void acceptCreatureVisitor(CreatureVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void restoreFaction() {
        this.setFaction(CreatureFaction.PLAYER);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Player)) {
            return false;
        }
        Player p = (Player) obj;
        return p.getId().equals(getId());
    }

    public UserID getId() {
        return this.user.getUserID();
    }

    public User getUser() {
        return this.user;
    }

    public CommandInvoker disconnectController() {
        CommandInvoker stored = this.getController();
        this.log(Level.WARNING, () -> String.format("Disconnecting controller %s", stored));
        this.setController(null);
        return stored;
    }

}
