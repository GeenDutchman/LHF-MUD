package com.lhf.messages.out;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.messages.OutMessageType;

public class StatsOutMessage extends OutMessage {
    private final Collection<BattleStatRecord> records;
    private final Optional<Integer> roundCount;
    private final Optional<Integer> turnCount;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Collection<BattleStatRecord> records;
        private Optional<Integer> roundCount;
        private Optional<Integer> turnCount;

        protected Builder() {
            super(OutMessageType.STATS);
            this.records = new TreeSet<>();
            this.roundCount = Optional.empty();
            this.turnCount = Optional.empty();
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public Builder setRoundCount(Integer round) {
            if (round == null) {
                this.roundCount = Optional.empty();
            } else {
                this.roundCount = Optional.of(round.intValue());
            }
            return this;
        }

        public Builder setTurnCount(Integer turn) {
            if (turn == null) {
                this.turnCount = Optional.empty();
            } else {
                this.turnCount = Optional.of(turn.intValue());
            }
            return this;
        }

        public Builder addRecords(Collection<BattleStatRecord> recordsToAdd) {
            this.records.addAll(recordsToAdd);
            return this;
        }

        public Builder addRecord(BattleStatRecord record) {
            if (record != null) {
                this.records.add(record);
            }
            return this;
        }

        @Override
        public StatsOutMessage Build() {
            return new StatsOutMessage(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public StatsOutMessage(Builder builder) {
        super(builder);
        this.records = Collections.unmodifiableCollection(builder.records);
        this.roundCount = builder.roundCount;
        this.turnCount = builder.turnCount;
    }

    @Override
    public String print() {
        StringJoiner sj = new StringJoiner("\n", "<BattleStats>Battle Statistics", "</BattleStats>")
                .setEmptyValue("No statistics found.");
        this.records.stream().forEach(record -> sj.add(record.toString()));
        if (this.roundCount.isPresent()) {
            sj.add("Round: " + String.valueOf(this.roundCount.get()));
        }
        if (this.turnCount.isPresent()) {
            sj.add("Turn: " + String.valueOf(this.turnCount.get()));
        }
        return sj.toString();
    }

    @Override
    public String toString() {
        return this.print();
    }

    public Collection<BattleStatRecord> getRecords() {
        return this.records;
    }

    public Optional<Integer> getRoundCount() {
        return roundCount;
    }

    public Optional<Integer> getTurnCount() {
        return turnCount;
    }

}
