package com.lhf.game.lewd;

import com.lhf.game.map.Area;

public abstract class LewdProduct {
    protected final String className;

    protected LewdProduct() {
        this.className = this.getClass().getName();
    }

    public abstract void onLewd(Area room, VrijPartij party);
}