package com.lhf.messages.out;

import java.util.Collection;
import java.util.Collections;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.messages.OutMessageType;

public class StatsOutMessage extends OutMessage {
    private final Collection<BattleStatRecord> records;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Collection<BattleStatRecord> records;

        protected Builder() {
            super(OutMessageType.STATS);
            this.records = new TreeSet<>();
        }

        @Override
        public Builder getThis() {
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
    }

    @Override
    public String print() {
        StringJoiner sj = new StringJoiner("\n", "<BattleStats>Battle Statistics", "</BattleStats>")
                .setEmptyValue("No statistics found.");
        this.records.stream().forEach(record -> sj.add(record.toString()));
        return sj.toString();
    }

    @Override
    public String toString() {
        return this.print();
    }

    public Collection<BattleStatRecord> getRecords() {
        return this.records;
    }

}
