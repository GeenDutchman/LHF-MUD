package com.lhf.game.creature;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.Fighter;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;

public class Player extends Creature {
    private User user;

    public Player(User user) {
        super(user.getUsername(), new Fighter(), CreatureFaction.PLAYER);
        this.user = user;
        this.user.setSuccessor(this);
        this.setController(this.user.getClient());
    }

    public Player(User user, Vocation vocation) {
        super(user.getUsername(), vocation, CreatureFaction.PLAYER);
        this.user = user;
        this.user.setSuccessor(this);
        this.setController(this.user.getClient());
    }

    public Player(User user, Statblock statblock, Vocation vocation) {
        super(user.getUsername(), statblock, CreatureFaction.PLAYER);
        this.user = user;
        this.user.setSuccessor(this);
        this.setController(this.user.getClient());
        this.setVocation(vocation);
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
