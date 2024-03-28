package com.lhf.messages.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.battle.BattleStats.BattleStatRecord.BattleStat;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.messages.GameEventType;

public class BattleStatsRequestedEvent extends GameEvent {
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

    public static class Builder extends GameEvent.Builder<Builder> {
        private Collection<BattleStatRecord> records;
        private Optional<Integer> roundCount;
        private Optional<Integer> turnCount;

        protected Builder() {
            super(GameEventType.STATS);
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
        public BattleStatsRequestedEvent Build() {
            return new BattleStatsRequestedEvent(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BattleStatsRequestedEvent(Builder builder) {
        super(builder);
        this.records = Collections.unmodifiableCollection(builder.records);
        this.roundCount = builder.roundCount;
        this.turnCount = builder.turnCount;
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        if (this.records.isEmpty()) {
            return myElement;
        }
        if (this.roundCount.isPresent()) {
            Element round = nodeGenerator.createElement("Round");
            round.setTextContent(this.roundCount.get().toString());
            myElement.appendChild(round);
        }
        if (this.turnCount.isPresent()) {
            Element turn = nodeGenerator.createElement("Turn");
            turn.setTextContent(this.turnCount.get().toString());
            myElement.appendChild(turn);
        }
        Element battleStats = nodeGenerator.createElement("BattleStats");
        for (final BattleStatRecord record : this.records) {
            Element battleStat = nodeGenerator.createElement("BattleStat");
            Element targetName = nodeGenerator.createElement("TargetName");
            targetName.setTextContent(record.getTargetName());
            battleStat.appendChild(targetName);
            Element faction = nodeGenerator.createElement("Faction");
            faction.setTextContent(record.getFaction().toString());
            battleStat.appendChild(faction);
            final Vocation vocation = record.getVocation();
            Element vocationElement = nodeGenerator.createElement("Vocation");
            vocationElement.setTextContent(vocation != null ? vocation.getName() : "null");
            battleStat.appendChild(vocationElement);
            Element bucket = nodeGenerator.createElement("HealthBucket");
            bucket.setTextContent(record.getBucket().toString());
            battleStat.appendChild(bucket);
            Element stats = nodeGenerator.createElement("Stats");
            for (final Entry<BattleStat, Integer> entry : record.getStats().entrySet()) {
                Element stat = nodeGenerator.createElement(entry.getKey().toString());
                stat.appendChild(nodeGenerator.createTextNode(entry.getValue().toString()));
                stats.appendChild(stat);
            }
            battleStat.appendChild(stats);
            battleStats.appendChild(battleStat);
        }
        myElement.appendChild(battleStats);
        return myElement;
    }

    @Override
    public String printString() {
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
        return this.printString();
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
