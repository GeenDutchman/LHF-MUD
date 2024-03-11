package com.lhf.game;

import java.util.Objects;
import java.util.StringJoiner;

import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.GameEventTester;
import com.lhf.server.interfaces.NotNull;

public class EffectPersistence implements Comparable<EffectPersistence> {
    public static class Ticker extends GameEventTester {
        private final int count;
        private int countdown;

        public Ticker(int count, TickType tickSize) {
            super(null, null, null, tickSize);
            this.count = count;
            this.countdown = count;
        }

        public Ticker(int count, final GameEventTester other) {
            super(other);
            this.count = count;
            this.countdown = count;
        }

        public Ticker(final Ticker other) {
            this(other.count, other);
        }

        @Override
        protected synchronized void successHook(GameEvent argument, String reason) {
            if (this.countdown > 0) {
                --this.countdown;
            }
        }

        public boolean isDone() {
            return this.countdown == 0;
        }

        public int getCountdown() {
            return countdown;
        }

        @Override
        public String toString() {
            if (this.countdown > 0) {
                return String.format("%d / %d %s", this.countdown, this.count, super.toString());
            }
            StringJoiner sj = new StringJoiner(" ");
            if (this.count > 0) {
                sj.add(String.valueOf(this.count));
            }
            sj.add(super.toString());
            return sj.toString();
        }

        public int getCount() {
            return count;
        }

    }

    private final Ticker baseTicker;

    public EffectPersistence(@NotNull TickType tickSize) {
        this(TickType.CONDITIONAL.equals(tickSize) ? -1 : 1, tickSize);
    }

    public EffectPersistence(int count, @NotNull TickType tickSize) {
        int calcCount = count;
        if (TickType.INSTANT.equals(tickSize)) {
            calcCount = 1;
        } else if (TickType.CONDITIONAL.equals(tickSize)) {
            calcCount = -1;
        }
        this.baseTicker = new Ticker(calcCount, tickSize);
    }

    public EffectPersistence(@NotNull Ticker base) {
        if (base != null) {
            this.baseTicker = new Ticker(base);
        } else {
            this.baseTicker = new Ticker(1, TickType.INSTANT);
        }
    }

    public EffectPersistence(EffectPersistence persistence) {
        this.baseTicker = new Ticker(persistence.baseTicker);
    }

    public int getCount() {
        return this.baseTicker.getCount();
    }

    public TickType getTickSize() {
        return this.baseTicker.getTickType();
    }

    // protected Ticker getTicker() {
    // return this.baseTicker;
    // }

    public Ticker getFreshTicker() {
        return new Ticker(this.baseTicker);
    }

    @Override
    public String toString() {
        return this.baseTicker.toString();
    }

    @Override
    public int compareTo(EffectPersistence o) {
        return this.baseTicker.compareTo(o.baseTicker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseTicker);
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
        return this.baseTicker.equals(other.baseTicker);
    }

}