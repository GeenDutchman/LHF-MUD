package com.lhf.game;

import java.util.StringJoiner;

public class EffectPersistence {
    public enum TickType {
        INSTANT, ACTION, BATTLE, ROOM, CONDITIONAL;
    }

    private int count;
    private TickType tickSize;

    private void initCount(int count) {
        if (TickType.INSTANT.equals(tickSize)) {
            this.count = 1;
        } else if (TickType.CONDITIONAL.equals(tickSize)) {
            this.count = -1;
        } else {
            this.count = count;
        }
    }

    public EffectPersistence(TickType tickSize) {
        this.tickSize = tickSize;
        this.initCount(1);
    }

    public EffectPersistence(int count, TickType tickSize) {
        this.tickSize = tickSize;
        this.initCount(count);

    }

    public int getCount() {
        return count;
    }

    public TickType getTickSize() {
        return tickSize;
    }

    public int tick(TickType type) {
        if (this.tickSize.equals(type) && this.count > 0) {
            this.count = this.count - 1;
        }
        return this.count;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.count > 0) {
            sj.add(String.valueOf(this.count));
        }
        sj.add(this.tickSize.name());
        return sj.toString();
    }

}