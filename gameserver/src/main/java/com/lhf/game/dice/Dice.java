package com.lhf.game.dice;

import com.lhf.Taggable;

public abstract class Dice implements Taggable {
    protected final int count;
    protected final DieType type;

    public interface IRollResult extends Taggable {
        public int getRoll();

        public int getOrigRoll();

        @Override
        public default String getColorTaggedName() {
            return this.getStartTag() + this.toString() + this.getEndTag();
        }

        public IRollResult negative();

        public IRollResult positive();

        public IRollResult twice();

        public IRollResult half();

        public IRollResult none();
    }

    protected abstract class ARollResult implements IRollResult {

        @Override
        public IRollResult negative() {
            if (this.getRoll() <= 0) {
                return this;
            }
            return new Dice.AnnotatedRollResult(this, this.getRoll() * -1, "negative");
        }

        @Override
        public IRollResult positive() {
            if (this.getRoll() >= 0) {
                return this;
            }
            return new Dice.AnnotatedRollResult(this, this.getRoll() * -1, "negative");
        }

        @Override
        public IRollResult twice() {
            if (this.getRoll() == 0) {
                return this;
            }
            return new Dice.AnnotatedRollResult(this, this.getRoll() * 2, "doubled");
        }

        @Override
        public IRollResult half() {
            if (this.getRoll() == 0) {
                return this;
            }
            return new Dice.AnnotatedRollResult(this, this.getRoll() / 2, "halved");
        }

        @Override
        public IRollResult none() {
            if (this.getRoll() == 0) {
                return this;
            }
            return new Dice.AnnotatedRollResult(this, 0, "negated");
        }

        @Override
        public String getStartTag() {
            return Dice.this.getStartTag();
        }

        @Override
        public String getEndTag() {
            return Dice.this.getEndTag();
        }
    }

    public class RollResult extends ARollResult {
        protected final int roll;

        public RollResult(int total) {
            this.roll = total;
        }

        @Override
        public int getRoll() {
            return this.roll;
        }

        @Override
        public int getOrigRoll() {
            return this.roll;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(Dice.this.toString()).append("(").append(this.getRoll()).append(")");
            return sb.toString();
        }

    }

    protected class AnnotatedRollResult extends ARollResult {
        protected final IRollResult sub;
        protected final String note;
        protected final int alteredResult;

        public AnnotatedRollResult(final IRollResult result, int alteredResult, String note) {
            this.sub = result;
            this.alteredResult = alteredResult;
            this.note = note != null ? note.trim() : note;
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
        public int getRoll() {
            return this.alteredResult;
        }

        @Override
        public int getOrigRoll() {
            if (this.sub != null) {
                return this.sub.getOrigRoll();
            }
            return this.alteredResult;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (this.sub != null) {
                sb.append(this.sub.toString());
                sb.append("-->");
            } else {
                sb.append(Dice.this.toString());
            }
            if (this.note != null && !this.note.isBlank()) {
                sb.append(" ").append(this.note).append(" ");
            }
            sb.append("(").append(this.getRoll()).append(")");
            return sb.toString();
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
