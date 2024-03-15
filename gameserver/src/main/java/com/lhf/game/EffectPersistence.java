package com.lhf.game;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Predicate;

import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.GameEventTester;
import com.lhf.server.interfaces.NotNull;

public class EffectPersistence implements Comparable<EffectPersistence> {
    public static class Ticker implements Predicate<GameEvent> {
        private final int count;
        private final TickType tickSize;
        private final GameEventTester tester;
        private int countdown;

        public Ticker(int count, TickType tickSize) {
            this.count = count;
            this.tickSize = tickSize;
            this.tester = new GameEventTester(null, null, null, tickSize);
            this.countdown = count;
        }

        public Ticker(int count, GameEventTester tester) {
            this.count = count;
            this.tickSize = null;
            this.tester = tester;
        }

        public Ticker(Ticker other) {
            if (other != null) {
                this.count = other.count;
                this.tickSize = other.tickSize;
                this.tester = other.getGameEventTester();
            } else {
                this.count = 0;
                this.tickSize = null;
                this.tester = null;
            }
        }

        @Override
        public synchronized boolean test(GameEvent event) {
            final GameEventTester eventTester = this.getGameEventTester();
            if (eventTester == null) {
                return false;
            }
            boolean testResult = eventTester.test(event);
            if (testResult && this.countdown > 0) {
                --this.countdown;
                return testResult;
            }
            return false;
        }

        public boolean isDone() {
            return this.countdown == 0;
        }

        public int getCountdown() {
            return countdown;
        }

        @Override
        public String toString() {
            final GameEventTester eventTester = this.getGameEventTester();
            if (this.countdown > 0) {
                return String.format("%d / %d %s", this.countdown, this.count,
                        eventTester != null ? eventTester.toString() : this.tickSize);
            }
            StringJoiner sj = new StringJoiner(" ");
            if (this.count > 0) {
                sj.add(String.valueOf(this.count));
            }
            if (eventTester != null) {
                sj.add(eventTester.toString());
            }
            return sj.toString();
        }

        public int getCount() {
            return count;
        }

        public TickType getTickSize() {
            return tickSize;
        }

        public GameEventTester getGameEventTester() {
            return this.tester != null ? this.tester : new GameEventTester(null, null, null, this.getTickSize());
        }
    }

    private final int count;
    private final TickType tickSize;
    private final GameEventTester basicTester;

    public EffectPersistence(@NotNull TickType tickSize) {
        this.tickSize = tickSize;
        this.basicTester = new GameEventTester(null, null, null, tickSize);
        if (TickType.CONDITIONAL.equals(tickSize)) {
            this.count = -1;
        } else {
            this.count = 1;
        }
    }

    public EffectPersistence(int count, @NotNull TickType tickSize) {
        this.tickSize = tickSize;
        this.basicTester = new GameEventTester(null, null, null, tickSize);
        if (TickType.INSTANT.equals(tickSize)) {
            this.count = 1;
        } else if (TickType.CONDITIONAL.equals(tickSize)) {
            this.count = -1;
        } else {
            this.count = count;
        }
    }

    public EffectPersistence(int count, GameEventTester tester) {
        this.count = 1;
        this.basicTester = tester;
        this.tickSize = null;
    }

    public EffectPersistence(EffectPersistence persistence) {
        this.tickSize = persistence.tickSize;
        this.count = persistence.count;
        this.basicTester = persistence.basicTester;
    }

    public int getCount() {
        return count;
    }

    public TickType getTickSize() {
        return tickSize;
    }

    public GameEventTester getGameEventTester() {
        return this.basicTester != null ? this.basicTester : new GameEventTester(null, null, null, this.getTickSize());
    }

    public Ticker getTicker() {
        return new Ticker(this.count, this.tickSize);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.count > 0) {
            sj.add(String.valueOf(this.count));
        }
        sj.add(this.getGameEventTester().toString());
        return sj.toString();
    }

    @Override
    public int compareTo(EffectPersistence o) {
        if (this.equals(o)) {
            return 0;
        }
        int compareSize = this.getGameEventTester().compareTo(o.getGameEventTester());
        if (compareSize != 0) {
            return compareSize;
        }
        return this.getCount() - o.getCount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, tickSize, basicTester);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof EffectPersistence))
            return false;
        EffectPersistence other = (EffectPersistence) obj;
        return count == other.count && tickSize == other.tickSize && Objects.equals(basicTester, other.basicTester);
    }

}