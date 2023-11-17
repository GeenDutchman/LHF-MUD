package com.lhf.game.dice;

import java.util.Comparator;

import com.lhf.Taggable;

public abstract class Dice implements Taggable, Comparable<Dice> {
    protected final int count;
    protected final DieType type;

    public interface IRollResult extends Taggable, Comparable<IRollResult> {
        public Dice getDice();

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

        @Override
        default int compareTo(IRollResult o) {
            if (o == null) {
                throw new NullPointerException("other IRollResult is null");
            }
            int dicecompare = this.getDice().compareTo(o.getDice());
            if (dicecompare != 0) {
                return dicecompare;
            }
            return this.getRoll() - o.getRoll();
        }

        static Comparator<IRollResult> origRollComparator() {
            return new Comparator<IRollResult>() {
                @Override
                public int compare(IRollResult first, IRollResult second) {
                    if (first == null || second == null) {
                        throw new NullPointerException("cannot compare null IRollResult");
                    }
                    return first.getOrigRoll() - second.getOrigRoll();
                }
            };
        }

        static Comparator<IRollResult> rollComparator() {
            return new Comparator<IRollResult>() {
                @Override
                public int compare(IRollResult first, IRollResult second) {
                    if (first == null || second == null) {
                        throw new NullPointerException("cannot compare null IRollResult");
                    }
                    return first.getRoll() - second.getRoll();
                }
            };
        }

    }

    protected abstract class ARollResult implements IRollResult {

        @Override
        public Dice getDice() {
            return Dice.this;
        }

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
                sb.append(" -->");
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

    public int getCount() {
        return count;
    }

    public DieType getType() {
        return type;
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

    @Override
    public int compareTo(Dice o) {
        if (o == null) {
            throw new NullPointerException("other Dice is null");
        }
        int typeCompare = this.type.compareTo(o.type);
        if (typeCompare != 0) {
            return typeCompare;
        }
        return this.count - o.count;
    }

}
