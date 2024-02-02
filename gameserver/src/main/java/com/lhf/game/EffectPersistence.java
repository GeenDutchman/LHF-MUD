package com.lhf.game;

import java.util.Objects;
import java.util.StringJoiner;

import com.lhf.server.interfaces.NotNull;

public class EffectPersistence implements Comparable<EffectPersistence> {
    public static class Ticker {
        private final int count;
        private final TickType tickSize;
        private int countdown;

        public Ticker(int count, TickType tickSize) {
            this.count = count;
            this.tickSize = tickSize;
            this.countdown = count;
        }

        public int tick(TickType type) {
            if (this.tickSize.equals(type) && this.countdown > 0) {
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
                return String.format("%d / %d %s", this.countdown, this.count, this.tickSize);
            }
            StringJoiner sj = new StringJoiner(" ");
            if (this.count > 0) {
                sj.add(String.valueOf(this.count));
            }
            sj.add(this.tickSize.name());
            return sj.toString();
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

    @Override
    public int hashCode() {
        return Objects.hash(count, tickSize);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EffectPersistence)) {
            return false;
        }
        EffectPersistence other = (EffectPersistence) obj;
        return count == other.count && tickSize == other.tickSize;
    }

}