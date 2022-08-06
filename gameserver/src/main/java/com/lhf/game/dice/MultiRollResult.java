package com.lhf.game.dice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.game.dice.Dice.RollResult;

public class MultiRollResult implements Taggable, Iterable<RollResult> {
    protected List<RollResult> rolls;
    protected List<Integer> bonuses;

    public MultiRollResult(RollResult first) {
        this.rolls = new ArrayList<>();
        this.rolls.add(first);
        this.bonuses = new ArrayList<>();
    }

    public MultiRollResult(RollResult first, int... bonuses) {
        this.rolls = new ArrayList<>();
        this.rolls.add(first);
        this.bonuses = new ArrayList<>();
        for (int b : bonuses) {
            this.bonuses.add(b);
        }
    }

    public MultiRollResult(RollResult first, List<Integer> bonuses) {
        this.rolls = new ArrayList<>();
        this.rolls.add(first);
        this.bonuses = new ArrayList<>(bonuses);
    }

    public MultiRollResult(List<RollResult> rolls) {
        this.rolls = new ArrayList<>(rolls);
        this.bonuses = new ArrayList<>();
    }

    public MultiRollResult(List<RollResult> rolls, List<Integer> bonuses) {
        this.rolls = new ArrayList<>(rolls);
        this.bonuses = new ArrayList<>(bonuses);
    }

    public MultiRollResult combine(MultiRollResult otherToConsume) {
        this.rolls.addAll(otherToConsume.rolls);
        this.bonuses.addAll(otherToConsume.bonuses);
        return this;
    }

    public MultiRollResult addResult(RollResult next) {
        this.rolls.add(next);
        return this;
    }

    public List<Integer> getBonuses() {
        return this.bonuses;
    }

    public MultiRollResult addBonus(int bonus) {
        this.bonuses.add(bonus);
        return this;
    }

    public MultiRollResult twiceDice() {
        List<RollResult> next = new ArrayList<>();
        for (RollResult rr : this.rolls) {
            next.add(rr.twice());
        }
        this.rolls = next;
        return this;
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

    public String toString() {
        StringJoiner sj = new StringJoiner(" + ");
        int sum = 0;
        for (RollResult rr : this.rolls) {
            sj.add(rr.toString());
            sum += rr.getRoll();
        }
        for (int bonus : this.bonuses) {
            sj.add(String.valueOf(bonus));
            sum += bonus;
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
