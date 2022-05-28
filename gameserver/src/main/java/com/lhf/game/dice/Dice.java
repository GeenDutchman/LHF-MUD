package com.lhf.game.dice;

import com.lhf.Taggable;

public abstract class Dice implements Taggable {
    protected int count;
    protected DieType type;
    protected int staticBonus;

    public class RollResult implements Taggable {
        protected int total;
        protected String result;

        public RollResult(int total, String result) {
            this.total = total;
            this.result = result;
        }

        public int getTotal() {
            return this.total;
        }

        public RollResult addBonus(int bonus) {
            this.total += bonus;
            if (this.result.length() > 0) {
                this.result = this.result + '+' + String.valueOf(bonus);
            } else {
                this.result = String.valueOf(bonus);
            }
            return this;
        }

        public RollResult combine(RollResult other) {
            this.total += other.total;
            if (this.result.length() > 0 && other.result.length() > 0) {
                this.result = this.result + '+' + other.result;
            } else if (this.result.length() == 0 && other.result.length() > 0) {
                this.result = other.result;
            }
            return this;
        }

        @Override
        public String toString() {
            if (this.result.length() == 0) {
                return String.valueOf(this.total);
            }
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
            return this.getStartTagName() + this.toString() + this.getEndTagName();
        }
    }

    public Dice(int count, DieType type) {
        this.count = count;
        this.type = type;
        this.staticBonus = 0;
    }

    public Dice(int count, DieType type, int bonus) {
        this.count = count;
        this.type = type;
        this.staticBonus = bonus;
    }

    abstract protected int roll();

    public RollResult rollDice() {
        RollResult rr = new Dice.RollResult(this.roll(), this.toString());
        if (this.staticBonus != 0) {
            rr.addBonus(this.staticBonus);
        }
        return rr;
    }

    public Dice setStaticBonus(int bonus) {
        this.staticBonus = bonus;
        return this;
    }

    public RollResult rollDice(int bonus) {
        return this.rollDice().addBonus(bonus);
    }

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