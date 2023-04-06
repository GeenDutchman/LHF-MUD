package com.lhf.game.dice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.game.dice.Dice.RollResult;

public class MultiRollResult implements Taggable, Iterable<RollResult> {
    protected final List<RollResult> rolls;
    protected final List<Integer> bonuses;

    public static class Builder {
        protected List<RollResult> rolls;
        protected List<Integer> bonuses;

        public Builder() {
            this.rolls = new ArrayList<>();
            this.bonuses = new ArrayList<>();
        }

        public Builder addRollResults(RollResult... rrs) {
            for (RollResult rr : rrs) {
                if (rr != null) {
                    this.rolls.add(rr);
                }
            }
            return this;
        }

        public Builder addBonuses(Integer... bonuses) {
            for (Integer bonus : bonuses) {
                if (bonus != null) {
                    this.bonuses.add(bonus);
                }
            }
            return this;
        }

        public Builder addRollResults(Iterable<RollResult> rrs) {
            for (RollResult rr : rrs) {
                if (rr != null) {
                    this.rolls.add(rr);
                }
            }
            return this;
        }

        public Builder addBonuses(Iterable<Integer> bonuses) {
            for (Integer bonus : bonuses) {
                if (bonus != null) {
                    this.bonuses.add(bonus);
                }
            }
            return this;
        }

        public Builder addMultiRollResult(MultiRollResult mrr) {
            this.rolls.addAll(mrr.rolls);
            this.bonuses.addAll(mrr.bonuses);
            return this;
        }

        public MultiRollResult Build() {
            return new MultiRollResult(this);
        }
    }

    protected MultiRollResult(Builder builder) {
        if (builder != null) {
            this.rolls = List.copyOf(builder.rolls);
            this.bonuses = List.copyOf(builder.bonuses);
        } else {
            this.rolls = List.of();
            this.bonuses = List.of();
        }
    }

    public List<Integer> getBonuses() {
        return this.bonuses;
    }

    public int getTotal() {
        int sum = 0;
        for (RollResult rr : this.rolls) {
            sum += rr.getRoll();
        }
        for (int bonus : this.bonuses) {
            sum += bonus;
        }
        return sum;
    }

    public int getRoll() {
        return this.getTotal();
    }

    public String toString() {
        StringJoiner sj = new StringJoiner(" + ");
        int sum = 0;
        for (RollResult rr : this.rolls) {
            sj.add(rr.toString());
            sum += rr.getRoll();
        }
        for (int bonus : this.bonuses) {
            if (bonus != 0) {
                sj.add(String.valueOf(bonus));
                sum += bonus;
            }
        }
        return sj.toString() + "=" + String.valueOf(sum);
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
    public Iterator<RollResult> iterator() {
        return this.rolls.listIterator();
    }

}
