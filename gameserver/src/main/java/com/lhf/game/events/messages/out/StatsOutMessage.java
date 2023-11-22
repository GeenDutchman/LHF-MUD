package com.lhf.game.events.messages.out;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.battle.BattleStats.BattleStatRecord.BattleStat;
import com.lhf.game.events.messages.OutMessageType;

public class StatsOutMessage extends OutMessage {
    private final Collection<BattleStatRecord> records;
    private final Optional<Integer> roundCount;
    private final Optional<Integer> turnCount;

    private final static Map<String, Integer> WIDTHS;
    private final static String FORMAT_STRING;
    private final static String HEADER_STRING;
    private final static String DELINEATOR_STRING;
    static {
        LinkedHashMap<String, Integer> widths = new LinkedHashMap<>(5 + BattleStat.values().length);
        widths.put("Name", 25);
        widths.put("Faction", 10);
        widths.put("Vocation", 8);
        widths.put("Health", 20);
        StringJoiner formatSj = new StringJoiner("|", "|", "|");
        widths.entrySet().stream().forEachOrdered(entry -> formatSj.add("%-" + entry.getValue().toString() + "s"));
        BattleStat.asList().stream().forEachOrdered(stat -> {
            widths.put(stat.name(), stat.name().length());
            formatSj.add("%" + Integer.toString(stat.name().length()) + "s");
        });
        WIDTHS = Collections.unmodifiableMap(widths);
        FORMAT_STRING = formatSj.toString();
        HEADER_STRING = String.format(FORMAT_STRING, WIDTHS.keySet().toArray());
        DELINEATOR_STRING = String.format(FORMAT_STRING,
                WIDTHS.values().stream().map(value -> "-".repeat(value)).toArray());
    }

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
        String header = "";
        if (this.records.size() > 0) {
            header = HEADER_STRING + "\n" + DELINEATOR_STRING + "\n";
        }
        StringJoiner sj = new StringJoiner("\n", "<BattleStats>\nBattle Statistics\n" + header, "\n</BattleStats>")
                .setEmptyValue("No statistics found.");
        this.records.stream().forEach(record -> {
            ArrayList<Object> toFormat = new ArrayList<>();
            toFormat.add(record.getTargetName());
            toFormat.add(record.getFaction());
            toFormat.add(record.getVocation() != null ? record.getVocation().getName() : null);
            toFormat.add(record.getBucket());
            toFormat.addAll(record.getStats().values());
            sj.add(String.format(FORMAT_STRING, toFormat.toArray()));
        });
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
