package com.lhf.game;

import java.util.StringJoiner;

import com.lhf.server.interfaces.NotNull;

public class EffectPersistence implements Comparable<EffectPersistence> {
    public enum TickType {
        INSTANT, ACTION, BATTLE, ROOM, CONDITIONAL;
    }

    public class Ticker {
        private int countdown;

        public Ticker(int countdown) {
            this.countdown = countdown;
        }

        public int tick(TickType type) {
            if (EffectPersistence.this.getTickSize().equals(type) && this.countdown > 0) {
                this.countdown = this.countdown - 1;
            }
            return this.countdown;
        }

        public int getCountdown() {
            return countdown;
        }

        @Override
        public String toString() {
            if (this.countdown > 0) {
                return String.valueOf(this.countdown) + "/" + EffectPersistence.this.toString();
            }
            return EffectPersistence.this.toString();
        }

        public int getCount() {
            return count;
        }

        public TickType getTickSize() {
            return tickSize;
        }
    }

    private final int count;
    private final TickType tickSize;

    public EffectPersistence(@NotNull TickType tickSize) {
        this.tickSize = tickSize;
        if (TickType.CONDITIONAL.equals(tickSize)) {
            this.count = -1;
        } else {
            this.count = 1;
        }
    }

    public EffectPersistence(int count, @NotNull TickType tickSize) {
        this.tickSize = tickSize;
        if (TickType.INSTANT.equals(tickSize)) {
            this.count = 1;
        } else if (TickType.CONDITIONAL.equals(tickSize)) {
            this.count = -1;
        } else {
            this.count = count;
        }
    }

    public EffectPersistence(EffectPersistence persistence) {
        this.tickSize = persistence.tickSize;
        this.count = persistence.count;
    }

    public int getCount() {
        return count;
    }

    public TickType getTickSize() {
        return tickSize;
    }

    public Ticker getTicker() {
        return new Ticker(this.count);
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

    @Override
    public int compareTo(EffectPersistence o) {
        int compareSize = this.getTickSize().compareTo(o.getTickSize());
        if (compareSize != 0) {
            return compareSize;
        }
        return this.getCount() - o.getCount();
    }

}