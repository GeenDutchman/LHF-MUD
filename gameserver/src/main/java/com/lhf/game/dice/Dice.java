package com.lhf.game.dice;

import com.lhf.Taggable;

public abstract class Dice implements Taggable {
    protected final int count;
    protected final DieType type;

    public class RollResult implements Taggable {
        protected final int roll;

        public RollResult(int total) {
            this.roll = total;
        }

        public int getRoll() {
            return this.roll;
        }

        public RollResult negative() {
            if (this.roll <= 0) {
                return this;
            }
            return new RollResult(this.roll * -1);
        }

        public RollResult positive() {
            if (this.roll >= 0) {
                return this;
            }
            return new RollResult(this.roll * -1);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(Dice.this.toString()).append("(").append(this.getRoll()).append(")");
            return sb.toString();
        }

        @Override
        public String getStartTag() {
            return Dice.this.getStartTag();
        }

        @Override
        public String getEndTag() {
            return Dice.this.getEndTag();
        }

        @Override
        public String getColorTaggedName() {
            return this.getStartTag() + this.toString() + this.getEndTag();
        }
    }

    public Dice(int count, DieType type) {
        this.count = count;
        this.type = type;
    }

    abstract protected int roll();

    public RollResult rollDice() {
        RollResult rr = new Dice.RollResult(this.roll());
        return rr;
    }

    @Override
    public String toString() {
        return "" + count + "d" + type.toString() + "";
    }

    @Override
    public String getStartTag() {
        return "<dice>";
    }

    @Override
    public String getEndTag() {
        return "</dice>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.toString() + this.getEndTag();
    }

}
