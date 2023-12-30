package com.lhf.game.dice;

import java.util.Comparator;
import java.util.function.IntUnaryOperator;

import com.lhf.Taggable;

public abstract class Dice implements Taggable, Comparable<Dice> {
    protected final int count;
    protected final DieType type;

    public class RollResult implements Taggable, Comparable<RollResult> {
        protected final RollResult origin;
        protected final String note;
        protected final int roll;

        public RollResult(final int roll) {
            this.roll = roll;
            this.note = null;
            this.origin = null;
        }

        protected RollResult(final RollResult result, final int alteredResult, final String note) {
            this.origin = result;
            this.roll = alteredResult;
            this.note = note != null ? note.trim() : note;
        }

        public Dice getDice() {
            return Dice.this;
        }

        public int getRoll() {
            return this.roll;
        }

        public int getOrigRoll() {
            if (this.origin != null) {
                return this.origin.getOrigRoll();
            }
            return this.roll;
        }

        public RollResult getOrigin() {
            return this.origin;
        }

        @Override
        public String getColorTaggedName() {
            return this.getStartTag() + this.toString() + this.getEndTag();
        }

        /**
         * Overridable factory method to produce an annotated result
         * 
         * @param operation The operation to perform on the result's roll
         * @param note      An optional note to display
         * @return this if `operation` is null, else a new result
         */
        protected RollResult annotate(final IntUnaryOperator operation, final String note) {
            if (operation == null) {
                return this;
            }
            return new Dice.RollResult(this, operation.applyAsInt(this.roll), note);
        }

        /** Returns a modified result of the roll * -1 if the roll is positive */
        public final RollResult negative() {
            if (this.getRoll() <= 0) {
                return this;
            }
            return this.annotate(rolled -> rolled * -1, "negative");
        }

        /** Returns a modified result of the roll * -1 if the roll is negative */
        public final RollResult positive() {
            if (this.getRoll() >= 0) {
                return this;
            }
            return this.annotate(rolled -> rolled * -1, "positive");
        }

        /** Returns a modified result of the roll * 2 if the roll is non-zero */
        public final RollResult twice() {
            if (this.getRoll() == 0) {
                return this;
            }
            return this.annotate(rolled -> rolled * 2, "doubled");
        }

        /** Returns a modified result of the roll / 2 if the roll is non-zero */
        public final RollResult half() {
            if (this.getRoll() == 0) {
                return this;
            }
            return this.annotate(rolled -> rolled / 2, "halved");
        }

        /** Returns a modified result of the roll * 0 if the roll is non-zero */
        public final RollResult none() {
            if (this.getRoll() == 0) {
                return this;
            }
            return this.annotate(rolled -> 0, "negated");
        }

        @Override
        public int compareTo(RollResult o) {
            if (o == null) {
                throw new NullPointerException("other IRollResult is null");
            }
            int dicecompare = this.getDice().compareTo(o.getDice());
            if (dicecompare != 0) {
                return dicecompare;
            }
            return this.getRoll() - o.getRoll();
        }

        static Comparator<RollResult> origRollComparator() {
            return new Comparator<RollResult>() {
                @Override
                public int compare(RollResult first, RollResult second) {
                    if (first == null || second == null) {
                        throw new NullPointerException("cannot compare null IRollResult");
                    }
                    return first.getOrigRoll() - second.getOrigRoll();
                }
            };
        }

        static Comparator<RollResult> rollComparator() {
            return new Comparator<RollResult>() {
                @Override
                public int compare(RollResult first, RollResult second) {
                    if (first == null || second == null) {
                        throw new NullPointerException("cannot compare null IRollResult");
                    }
                    return first.getRoll() - second.getRoll();
                }
            };
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
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (this.origin != null) {
                sb.append(this.origin.toString());
                sb.append(" -->");
            } else {
                sb.append(Dice.this.toString());
            }
            if (this.note != null && !this.note.isBlank()) {
                sb.append(" ").append(this.note).append(" ");
            }
            sb.append("(").append(this.roll).append(")");
            return sb.toString();
        }

    }

    public Dice(int count, DieType type) {
        this.count = count;
        this.type = type;
    }

    public Dice(Dice other) {
        this.count = other.count;
        this.type = other.type;
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
