package com.lhf.game.creature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.TreeMap;

import com.lhf.game.EntityEffectSource;
import com.lhf.game.TickType;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.Stats;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.GameEventTester;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.SeeCategory;

public class CreatureEffectSource extends EntityEffectSource {

    public static class Deltas {

        final protected Map<Stats, Integer> statChanges;

        final protected Map<Attributes, Integer> attributeScoreChanges;

        final protected Map<Attributes, Integer> attributeBonusChanges;

        final protected List<DamageDice> damages;

        protected boolean restoreFaction;

        public Deltas() {
            this.statChanges = new EnumMap<>(Stats.class);
            this.attributeScoreChanges = new EnumMap<>(Attributes.class);
            this.attributeBonusChanges = new EnumMap<>(Attributes.class);
            this.damages = new ArrayList<>();
            this.restoreFaction = false;
        }

        public Deltas(Map<Stats, Integer> statChanges, Map<Attributes, Integer> attributeScoreChanges,
                Map<Attributes, Integer> attributeBonusChanges, List<DamageDice> damages, boolean restoreFaction) {
            this.statChanges = statChanges != null ? new EnumMap<>(statChanges) : new EnumMap<>(Stats.class);
            this.attributeScoreChanges = attributeScoreChanges != null ? new EnumMap<>(attributeScoreChanges)
                    : new EnumMap<>(Attributes.class);
            this.attributeBonusChanges = attributeBonusChanges != null ? new EnumMap<>(attributeBonusChanges)
                    : new EnumMap<>(Attributes.class);
            this.damages = damages != null ? new ArrayList<>(damages) : new ArrayList<>();
            this.restoreFaction = restoreFaction;
        }

        public Deltas(Deltas other) {
            this(other.statChanges, other.attributeScoreChanges, other.attributeBonusChanges, other.damages,
                    other.restoreFaction);
        }

        public Deltas reversal() {
            final Deltas reversed = new Deltas();
            for (final Entry<Stats, Integer> statChange : this.statChanges.entrySet()) {
                if (statChange != null && statChange.getKey() != null) {
                    final Integer value = statChange.getValue();
                    if (value == null || value == 0) {
                        continue;
                    }
                    reversed.statChanges.put(statChange.getKey(), value * -1);
                }
            }

            for (final Entry<Attributes, Integer> scoreChange : this.attributeScoreChanges.entrySet()) {
                if (scoreChange != null && scoreChange.getValue() != null) {
                    final Integer value = scoreChange.getValue();
                    if (value == null || value == 0) {
                        continue;
                    }
                    reversed.attributeScoreChanges.put(scoreChange.getKey(), value * -1);
                }
            }

            for (final Entry<Attributes, Integer> bonusChange : this.attributeBonusChanges.entrySet()) {
                if (bonusChange != null && bonusChange.getValue() != null) {
                    final Integer value = bonusChange.getValue();
                    if (value == null || value == 0) {
                        continue;
                    }
                    reversed.attributeBonusChanges.put(bonusChange.getKey(), value * -1);
                }
            }
            return reversed;
        }

        public boolean isRestoreFaction() {
            return restoreFaction;
        }

        public Map<Stats, Integer> getStatChanges() {
            return Collections.unmodifiableMap(statChanges);
        }

        public Map<Attributes, Integer> getAttributeScoreChanges() {
            return Collections.unmodifiableMap(attributeScoreChanges);
        }

        public Map<Attributes, Integer> getAttributeBonusChanges() {
            return Collections.unmodifiableMap(attributeBonusChanges);
        }

        public List<DamageDice> getDamages() {
            return Collections.unmodifiableList(damages);
        }

        public Deltas setStatChange(Stats stat, int value) {
            if (stat != null) {
                this.statChanges.put(stat, value);
            }
            return this;
        }

        public Deltas setAttributeScoreChange(Attributes attribute, int value) {
            if (attribute != null) {
                this.attributeScoreChanges.put(attribute, value);
            }
            return this;
        }

        public Deltas setAttributeBonusChange(Attributes attribute, int value) {
            if (attribute != null) {
                this.attributeBonusChanges.put(attribute, value);
            }
            return this;
        }

        public Deltas addDamage(DamageDice dice) {
            if (dice != null) {
                this.damages.add(dice);
            }
            return this;
        }

        public Deltas setRestoreFaction(boolean restoreFaction) {
            this.restoreFaction = restoreFaction;
            return this;
        }

        public String printDescription() {
            StringJoiner sj = new StringJoiner(" ");
            if (this.statChanges.size() > 0) {
                sj.add("The target's");
                for (Map.Entry<Stats, Integer> deltas : this.statChanges.entrySet()) {
                    sj.add(deltas.getKey().toString()).add("stat will change by").add(deltas.getValue().toString());
                }
                sj.add("\r\n");
            }
            if (this.attributeScoreChanges.size() > 0) {
                sj.add("The target's");
                for (Map.Entry<Attributes, Integer> deltas : this.getAttributeScoreChanges().entrySet()) {
                    sj.add(deltas.getKey().toString()).add("score will change by").add(deltas.getValue().toString());
                }
                sj.add("\r\n");
            }
            if (this.attributeBonusChanges.size() > 0) {
                sj.add("The target's");
                for (Map.Entry<Attributes, Integer> deltas : this.getAttributeBonusChanges().entrySet()) {
                    sj.add(deltas.getKey().toString()).add("bonus will change by").add(deltas.getValue().toString());
                }
                sj.add("\r\n");
            }
            if (this.damages.size() > 0) {
                sj.add("The target will take");
                for (final DamageDice dd : this.damages) {
                    sj.add(dd.toString());
                }
                sj.add("damage\r\n");
            }
            if (this.isRestoreFaction()) {
                sj.add("And will attempt to restore").add("the target's").add("faction");
            }
            return sj.toString();
        }

        public MultiRollResult rollDamages() {
            MultiRollResult.Builder rollBuilder = new MultiRollResult.Builder();
            for (final DamageDice dd : this.damages) {
                rollBuilder.addRollResults(dd.rollDice());
            }
            return rollBuilder.Build();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Deltas [statChanges=").append(statChanges).append(", attributeScoreChanges=")
                    .append(attributeScoreChanges).append(", attributeBonusChanges=").append(attributeBonusChanges)
                    .append(", damages=").append(damages).append(", restoreFaction=").append(restoreFaction)
                    .append("]");
            return builder.toString();
        }

        public boolean isOffensive() {
            for (DamageDice dd : this.damages) {
                if (!DamageFlavor.HEALING.equals(dd.getDamageFlavor())) {
                    return true;
                }
            }
            for (Integer i : this.statChanges.values()) {
                if (i < 0) {
                    return true;
                }
            }
            for (Integer i : this.attributeScoreChanges.values()) {
                if (i < 0) {
                    return true;
                }
            }
            for (Integer i : this.attributeBonusChanges.values()) {
                if (i < 0) {
                    return true;
                }
            }
            return false;
        }

        public int aiScore() {
            int score = 0;
            for (DamageDice dd : this.damages) {
                score += Math.abs(dd.getCount() * dd.getType().getType());
            }
            for (Integer i : this.statChanges.values()) {
                if (i != null) {
                    score += Math.abs(i);
                }
            }
            for (Integer i : this.attributeScoreChanges.values()) {
                if (i != null) {
                    score += Math.abs(i);
                }
            }
            for (Integer i : this.attributeBonusChanges.values()) {
                if (i != null) {
                    score += Math.abs(i);
                }
            }
            return score;
        }

    }

    protected final Deltas onApplication, onRemoval;
    protected final Map<GameEventTester, Deltas> onTickEvent;

    protected static abstract class AbstractBuilder<AB extends AbstractBuilder<AB>>
            extends EntityEffectSource.Builder<AB> {
        private Deltas onApplication, onRemoval;
        private Map<GameEventTester, Deltas> onTickEvent;
        private boolean reverseApplication = true;

        protected AbstractBuilder(String name) {
            super(name);
            this.onApplication = null;
            this.onRemoval = null;
            this.onTickEvent = new TreeMap<>();
        }

        public Deltas getOnApplication() {
            return onApplication;
        }

        public AB setOnApplication(Deltas onApplication) {
            this.onApplication = onApplication;
            return getThis();
        }

        public AB withReversedApplication() {
            this.reverseApplication = true;
            return getThis();
        }

        public AB withoutReversedApplication() {
            this.reverseApplication = false;
            return getThis();
        }

        public Deltas getOnRemoval() {
            if (onRemoval != null) {
                return onRemoval;
            }
            return this.reverseApplication && this.onApplication != null && !TickType.INSTANT.equals(this.getTickType())
                    ? this.onApplication.reversal()
                    : null;
        }

        public AB setOnRemoval(Deltas onRemoval) {
            this.onRemoval = onRemoval;
            return getThis();
        }

        public Map<GameEventTester, Deltas> getOnTickEvent() {
            return onTickEvent;
        }

        public AB setOnTickEvent(Map<GameEventTester, Deltas> onTickEvent) {
            this.onTickEvent = onTickEvent != null ? onTickEvent : new TreeMap<>();
            return getThis();
        }

        public AB setDeltaForTester(GameEventTester tester, Deltas deltas) {
            if (tester == null || deltas == null) {
                return getThis();
            }
            this.onTickEvent.put(tester, deltas);
            return getThis();
        }
    }

    public static class Builder extends AbstractBuilder<Builder> {

        public Builder(String name) {
            super(name);
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public CreatureEffectSource build() {
            return new CreatureEffectSource(getThis());
        }

    }

    public static Builder getCreatureEffectBuilder(String name) {
        return new Builder(name);
    }

    protected CreatureEffectSource(AbstractBuilder<?> builder) {
        super(builder);
        this.onApplication = builder.getOnApplication();
        this.onRemoval = builder.getOnRemoval();
        this.onTickEvent = builder.getOnTickEvent();
    }

    public Deltas getOnApplication() {
        return onApplication;
    }

    public Deltas getOnRemoval() {
        return onRemoval;
    }

    public Map<GameEventTester, Deltas> getOnTickEvent() {
        return onTickEvent;
    }

    /**
     * Returns unmodifiable map entry of Entry<GameEventTester, Deltas> or null
     * 
     * @param event
     * @return
     */
    public Entry<GameEventTester, Deltas> getTesterEntryForEvent(GameEvent event) {
        if (event == null || this.onTickEvent == null) {
            return null;
        }
        for (final Entry<GameEventTester, Deltas> entry : Collections.unmodifiableSet(this.onTickEvent.entrySet())) {
            final GameEventTester tester = entry.getKey();
            if (tester == null || !tester.test(event)) {
                continue;
            }
            if (entry.getValue() != null) {
                return entry;
            }
        }
        return null;
    }

    public Deltas getDeltasForEvent(GameEvent event) {
        final Entry<GameEventTester, Deltas> entry = this.getTesterEntryForEvent(event);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    @Override
    public boolean isOffensive() {
        if (this.onApplication != null && this.onApplication.isOffensive()) {
            return true;
        }
        // onRemovals that are just reversals of nonoffensive onApplications are not
        // offensive
        // but we cannot distinguish that
        if (this.onTickEvent != null) {
            for (final Deltas tickDelta : this.onTickEvent.values()) {
                if (tickDelta != null && tickDelta.isOffensive()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int aiScore() {
        int score = 0;
        if (this.onApplication != null) {
            score += this.onApplication.aiScore();
        }
        // again, disregarding potential reversal of onApplication
        if (this.onTickEvent != null) {
            for (final Deltas tickDelta : this.onTickEvent.values()) {
                if (tickDelta != null) {
                    score += tickDelta.aiScore();
                }
            }
        }
        return score;
    }

    @Override
    public SeeEvent produceMessage(SeeEvent.ABuilder<?> seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeEvent.getBuilder().setExaminable(this);
        }
        if (this.onApplication != null && this.onApplication.damages.size() > 0) {
            for (final DamageDice dd : this.onApplication.damages) {
                seeOutMessage.addSeen(SeeCategory.DAMAGES, dd);
            }
        }
        return seeOutMessage.Build();
    }

    @Override
    public String printDescription() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.printDescription()).add("\r\n");
        if (this.onApplication != null) {
            final String applicationDescription = this.onApplication.printDescription();
            if (applicationDescription.length() > 0) {
                sj.add("On application:").add(applicationDescription);
            }
        }
        if (this.onTickEvent != null && this.onTickEvent.size() > 0) {
            for (final Entry<GameEventTester, Deltas> tickDeltas : this.onTickEvent.entrySet()) {
                final GameEventTester tester = tickDeltas.getKey();
                final Deltas deltas = tickDeltas.getValue();
                if (tester == null || deltas == null) {
                    continue;
                }
                final String tickDescription = deltas.printDescription();
                if (tickDescription.length() > 0) {
                    sj.add(tester.toString()).add(tickDescription);
                }
            }
        }
        if (this.onRemoval != null) {
            final String removalDescription = this.onRemoval.printDescription();
            if (removalDescription.length() > 0) {
                sj.add("On removal:").add(removalDescription);
            }
        }
        return sj.toString();
    }

}
