package com.lhf.game.dice;

import com.lhf.game.map.objects.sharedinterfaces.Taggable;

public abstract class Dice implements Taggable {
    protected int count;
    protected DieType type;

    public Dice(int count, DieType type) {
        this.count = count;
        this.type = type;
    }

    abstract public int roll();

    @Override
    public String toString() {
        return "" + count + "d" + type.toString() + "";
    }

    @Override
    public String getStartTagName() {
        return "<dice>";
    }

    @Override
    public String getEndTagName() {
        return "</dice>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTagName() + this.toString() + this.getEndTagName();
    }

}
