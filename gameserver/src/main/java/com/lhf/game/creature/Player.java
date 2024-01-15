package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.function.UnaryOperator;
import java.util.logging.Level;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.interfaces.NotNull;

public class Player extends Creature {
    private User user;

    public static class PlayerBuilder extends ICreature.CreatureBuilder<PlayerBuilder, Player> {
        private User user;

        private PlayerBuilder(User user) {
            this.setFaction(CreatureFaction.PLAYER);
            this.user = user;
            this.setName(this.user.getUsername());
        }

        public static PlayerBuilder getInstance(User user) {
            return new PlayerBuilder(user);
        }

        @Override
        public PlayerBuilder makeCopy() {
            throw new UnsupportedOperationException(
                    "Cannot make a copy of a User, and thus cannot make a copy of a PlayerBuilder");
        }

        @Override
        protected PlayerBuilder getThis() {
            return this;
        }

        public User getUser() {
            return user;
        }

        public PlayerBuilder setUser(User user) {
            if (user == null) {
                throw new IllegalArgumentException("Cannot set a null user!");
            }
            this.user = user;
            this.setName(this.user.getUsername());
            return this;
        }

        @Override
        public Player build(CommandInvoker controller,
                CommandChainHandler successor, StatblockManager statblockManager,
                UnaryOperator<PlayerBuilder> composedLazyLoaders)
                throws FileNotFoundException {

            if (statblockManager != null) {
                this.loadStatblock(statblockManager);
            }
            if (composedLazyLoaders != null) {
                composedLazyLoaders.apply(this.getThis());
            }
            return new Player(this.getThis(), controller, successor, this.getStatblock());
        }

        public Player build(User user, CommandChainHandler successor, StatblockManager statblockManager,
                UnaryOperator<PlayerBuilder> composedLazyLoaders) throws FileNotFoundException {
            this.setUser(user);
            return this.build(user, successor, statblockManager, composedLazyLoaders);
        }

        public Player build(CommandChainHandler successor) {
            User foundUser = this.getUser();
            if (foundUser == null) {
                throw new IllegalStateException("Player cannot be created with null user!");
            }
            Statblock currStatBlock = this.getStatblock();
            if (currStatBlock == null) {
                VocationName currVocation = this.getVocation();
                if (currVocation == null) {
                    throw new IllegalStateException(
                            "Must have a statblock or a Vocation from which to define the statblock!");
                }
                currStatBlock = currVocation.createNewDefaultStatblock("Player").build();
                this.setStatblock(currStatBlock);
            }
            return new Player(this.getThis(), foundUser, successor, this.getStatblock());
        }
    }

    public Player(PlayerBuilder builder,
            @NotNull CommandInvoker controller, CommandChainHandler successor,
            @NotNull Statblock statblock) {
        super(builder, controller, successor, statblock);
        this.user = builder.getUser();
        this.user.setSuccessor(this);
    }

    public static PlayerBuilder getPlayerBuilder(User user) {
        return new PlayerBuilder(user);
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
