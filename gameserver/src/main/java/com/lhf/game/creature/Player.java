package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;

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
        protected PlayerBuilder getThis() {
            return this;
        }

        public User getUser() {
            return user;
        }

        public PlayerBuilder setUser(User user) {
            this.user = user;
            this.setName(this.user.getUsername());
            return this;
        }

        @Override
        public Player build(Consumer<Player> controllerAssigner, CommandChainHandler successor,
                StatblockManager statblockManager, UnaryOperator<PlayerBuilder> composedLazyLoaders)
                throws FileNotFoundException {

            if (statblockManager != null) {
                this.loadStatblock(statblockManager);
            }
            if (composedLazyLoaders != null) {
                composedLazyLoaders.apply(this.getThis());
            }
            Player player = new Player(this.getThis());
            if (controllerAssigner != null) {
                controllerAssigner.accept(player);
            } else {
                User foundUser = this.getUser();
                if (foundUser == null) {
                    throw new IllegalArgumentException("Player cannot be created with null user!");
                }
                player.setController(foundUser);
            }
            try {
                this.getUser().setSuccessor(player);
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("Player cannot be created with null user!", e);
            }
            return player;
        }

        public Player build(User user, CommandChainHandler successor, StatblockManager statblockManager,
                UnaryOperator<PlayerBuilder> composedLazyLoaders) throws FileNotFoundException {
            this.setUser(user);
            Consumer<Player> controllerAssigner = user != null ? (player) -> {
                if (player == null) {
                    return;
                }
                player.setController(user);
                user.setSuccessor(player);
            } : null;
            return this.build(controllerAssigner, successor, statblockManager, composedLazyLoaders);
        }

        public Player build() {
            User foundUser = this.getUser();
            if (foundUser == null) {
                throw new IllegalArgumentException("Player cannot be created with null user!");
            }
            if (this.getStatblock() == null && this.getVocation() == null) {
                throw new IllegalArgumentException(
                        "Must have a statblock or a Vocation from which to define the statblock!");
            }
            this.setStatblock(this.getVocation().createNewDefaultStatblock("Player"));
            Player player = new Player(this.getThis());
            player.setController(foundUser);
            foundUser.setSuccessor(player);
            return player;
        }
    }

    public Player(PlayerBuilder builder) {
        super(builder);
        this.user = builder.getUser();
        this.user.setSuccessor(this);
    }

    public static PlayerBuilder getPlayerBuilder(User user) {
        return new PlayerBuilder(user);
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

}
