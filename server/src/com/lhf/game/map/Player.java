package com.lhf.game.map;

import com.lhf.user.UserID;

public class Player {
    private UserID id;

    public Player(UserID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Player)) {
            return false;
        }
        Player p = (Player)obj;
        return p.getId().equals(getId());
    }

    public UserID getId() {
        return id;
    }
}
