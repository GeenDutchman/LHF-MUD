package com.lhf.game.lewd;

import java.util.Objects;
import java.util.function.Consumer;

import com.lhf.game.map.Area;

public abstract class LewdProduct {
    protected final String className;

    protected LewdProduct() {
        this.className = this.getClass().getName();
    }

    public abstract Consumer<Area> onLewdAreaChanges(VrijPartij party);

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof LewdProduct))
            return false;
        LewdProduct other = (LewdProduct) obj;
        return Objects.equals(className, other.className);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LewdProduct [className=").append(className).append("]");
        return builder.toString();
    }

}