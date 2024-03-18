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

        @Deprecated(forRemoval = false)
        public Ticker(int count, TickType tickSize) {
            this.count = count;
            this.tickSize = tickSize;
            this.tester = new GameEventTester(null, null, null, tickSize, false);
            this.countdown = count;
        }

        public Ticker(int count, GameEventTester tester) {
            this.count = count;
            this.tickSize = null;
            this.tester = tester;
            this.countdown = count;
        }

        public Ticker(Ticker other) {
            if (other != null) {
                this.count = other.count;
                this.tickSize = other.tickSize;
                this.tester = other.getGameEventTester();
                this.countdown = other.count;
            } else {
                this.count = 0;
                this.tickSize = null;
                this.tester = null;
                this.countdown = 0;
            }
        }

        @Override
        public synchronized boolean test(GameEvent event) {
            final GameEventTester eventTester = this.getGameEventTester();
            if (eventTester == null) {
                return false;
            }
            boolean testResult = eventTester.test(event);
            if (testResult && this.count >= 0 && this.countdown > 0) {
                --this.countdown;
                return testResult;
            }
            return false;
        }

        public boolean isDone() {
            if (this.count == 0) {
                return true;
            }
            return this.count >= 0 && this.countdown == 0;
        }

        /**
         * Returns the countdown for the ticker
         * 
         * @deprecated do not draw conclusions about the timer being done from the value
         *             returned from this method
         * @return the countdown value
         */
        @Deprecated(forRemoval = false)
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
            return this.tester != null ? this.tester : new GameEventTester(null, null, null, this.getTickSize(), false);
        }
    }

    private final int count;
    private final TickType tickSize;
    private final GameEventTester basicTester;

    public EffectPersistence(@NotNull TickType tickSize) {
        this.tickSize = tickSize;
        this.basicTester = new GameEventTester(null, null, null, tickSize, false);
        if (TickType.CONDITIONAL.equals(tickSize)) {
            this.count = -1;
        } else {
            this.count = 1;
        }
    }

    public EffectPersistence(int count, @NotNull TickType tickSize) {
        this.tickSize = tickSize;
        this.basicTester = new GameEventTester(null, null, null, tickSize, false);
        if (TickType.INSTANT.equals(tickSize)) {
            this.count = 1;
        } else if (TickType.CONDITIONAL.equals(tickSize)) {
            this.count = -1;
        } else {
            this.count = count;
        }
    }

    public EffectPersistence(int count, GameEventTester tester) {
        this.count = count;
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
        return this.basicTester != null ? this.basicTester
                : new GameEventTester(null, null, null, this.getTickSize(), false);
    }

    public Ticker getTicker() {
        return new Ticker(this.count, this.getGameEventTester());
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