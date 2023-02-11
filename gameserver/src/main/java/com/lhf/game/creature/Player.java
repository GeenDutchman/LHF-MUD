package com.lhf.game.creature;

import com.lhf.game.enums.CreatureFaction;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;

public class Player extends Creature {
    private User user;

    public static class PlayerBuilder extends Creature.CreatureBuilder<PlayerBuilder> {
        private User user;

        private PlayerBuilder(User user) {
            this.setFaction(CreatureFaction.PLAYER);
            this.user = user;
            this.setController(user.getClient());
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
            return this;
        }

        @Override
        public Player build() {
            return new Player(this);
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
