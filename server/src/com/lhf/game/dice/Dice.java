package com.lhf.game.dice;

import com.lhf.game.map.objects.sharedinterfaces.Taggable;

public abstract class Dice implements Taggable {
    protected int count;
    protected DieType type;

    public class RollResult implements Taggable {
        protected int total;
        protected String result;

        public RollResult(int total, String result) {
            this.total = total;
            this.result = result;
        }

        public RollResult combine(RollResult other) {
            this.total += other.total;
            this.result = this.result + '+' + other.result;
            return this;
        }

        @Override
        public String toString() {
            return this.result + " (" + String.valueOf(this.total) + ")";
        }

        @Override
        public String getStartTagName() {
            return Dice.this.getStartTagName();
        }

        @Override
        public String getEndTagName() {
            return Dice.this.getEndTagName();
        }

        @Override
        public String getColorTaggedName() {
            // TODO Auto-generated method stub
            return null;
        }
    }

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
