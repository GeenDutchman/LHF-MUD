package com.lhf.messages.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.StringJoiner;
import java.util.TreeMap;

import com.lhf.Examinable;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.Taggable;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.TickType;
import com.lhf.messages.GameEventType;

public class SeeEvent extends GameEvent {
    public enum SeeCategory {
        DIRECTION, CREATURE, PLAYER, NPC, MONSTER, ROOM_ITEM, TAKEABLE, EFFECTS, EQUIPMENT_SLOTS, PROFICIENCIES, STATS,
        ATTRIBUTE_SCORE, ATTRIBUTE_BONUS, DAMAGES, OTHER, INVISIBLE_CREATURE, INVISIBLE_ROOM_ITEM, INVISIBLE_TAKEABLE;

        public static SeeCategory getSeeCategory(String value) {
            for (SeeCategory category : values()) {
                if (category.toString().equalsIgnoreCase(value)) {
                    return category;
                }
            }
            return null;
        }

        public static boolean isSeeCategory(String value) {
            return SeeCategory.getSeeCategory(value) != null;
        }
    }

    private final static TickType tickType = TickType.ACTION;
    private final Examinable examinable;
    private final NavigableMap<String, List<Taggable>> seenCategorized;
    private final List<EntityEffectSource> effects;
    private final String extraInfo;
    private final String deniedReason;

    public abstract static class ABuilder<T extends ABuilder<T>> extends GameEvent.Builder<T> {
        private Examinable examinable;
        private NavigableMap<String, List<Taggable>> seenCategorized = new TreeMap<>();
        private List<EntityEffectSource> effects = new ArrayList<>();
        private StringJoiner extraInfo = new StringJoiner("\r\n").setEmptyValue("");
        private String deniedReason;

        protected ABuilder() {
            super(GameEventType.SEE);
        }

        protected ABuilder(GameEventType type) {
            super(type);
        }

        public T setExaminable(Examinable examinable) {
            this.examinable = examinable;
            return this.getThis();
        }

        public T addExtraInfo(String extraInfo) {
            this.extraInfo.add(extraInfo);
            return this.getThis();
        }

        public T addSeen(String category, Taggable thing) {
            if (this.seenCategorized == null) {
                this.seenCategorized = new TreeMap<>();
            }
            this.seenCategorized.putIfAbsent(category, new ArrayList<>());
            this.seenCategorized.get(category).add(thing);
            return this.getThis();
        }

        public T addSeen(SeeCategory category, Taggable thing) {
            this.addSeen(category.name(), thing);
            return this.getThis();
        }

        public T addEffector(EntityEffectSource effect) {
            this.effects.add(effect);
            return this.getThis();
        }

        public T setDeniedReason(String deniedReason) {
            this.deniedReason = deniedReason;
            return this.getThis();
        }

        public Examinable getExaminable() {
            return examinable;
        }

        public NavigableMap<String, List<Taggable>> getSeenCategorized() {
            return Collections.unmodifiableNavigableMap(seenCategorized);
        }

        public List<EntityEffectSource> getEffects() {
            return Collections.unmodifiableList(effects);
        }

        public String getExtraInfo() {
            return extraInfo.toString();
        }

        public String getDeniedReason() {
            return deniedReason;
        }

        @Override
        public abstract SeeEvent Build();

    }

    public static class Builder extends ABuilder<Builder> {
        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public SeeEvent Build() {
            return new SeeEvent(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public SeeEvent(ABuilder<?> builder) {
        super(builder);
        this.examinable = builder.getExaminable();
        this.extraInfo = builder.getExtraInfo();
        this.deniedReason = builder.getDeniedReason();
        this.seenCategorized = builder.getSeenCategorized();
        this.effects = builder.getEffects();
    }

    public Examinable getExaminable() {
        return this.examinable;
    }

    public List<Taggable> getTaggedCategory(String category) {
        return Collections.unmodifiableList(this.seenCategorized.get(category));
    }

    public List<Taggable> getTaggedCategory(SeeCategory category) {
        return Collections.unmodifiableList(this.getTaggedCategory(category.name()));
    }

    public boolean isDenied() {
        return this.deniedReason != null && this.deniedReason.length() != 0;
    }

    public List<EntityEffectSource> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    @Override
    public TickType getTickType() {
        return tickType;
    }

    @Override
    public void buildOutput(RichOutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.isDenied()) {
            builder.appendString(deniedReason);
            return;
        }
        if (this.examinable == null) {
            builder.appendString("You cannot see that.");
            return;
        }
        builder.appendExaminable(examinable, null, "\r\n");
        if (this.extraInfo != null && !this.extraInfo.isBlank()) {
            builder.appendString(this.extraInfo, null, "\r\n");
        }

        for (String category : this.seenCategorized.keySet()) {
            List<Taggable> taggedList = this.seenCategorized.get(category);
            if (taggedList == null || taggedList.isEmpty()) {
                continue;
            }
            RichOutputBuilder subBuilder = null;
            SeeCategory categorized = SeeCategory.getSeeCategory(category);
            if (categorized == null) {
                categorized = SeeCategory.OTHER;
                subBuilder = builder.produceSubBuilder(category != null ? category : SeeCategory.OTHER.toString());
            } else {
                subBuilder = builder.produceSubBuilder(categorized.toString());
            }
            switch (categorized) {
            case DIRECTION:
                subBuilder.appendString("Available Directions:");
                break;
            case CREATURE:
                subBuilder.appendString("Creatures that you can see:");
                break;
            case PLAYER:
                subBuilder.appendString("Players that you can see:");
                break;
            case NPC:
                subBuilder.appendString("Non Player Characters that you can see:");
                break;
            case MONSTER:
                subBuilder.appendString("Monsters that you can see:");
                break;
            case ROOM_ITEM:
                subBuilder.appendString("Objects that you can see:");
                break;
            case TAKEABLE:
                subBuilder.appendString("Items that you can see:");
                break;
            case EFFECTS:
                subBuilder.appendString("Effects that you know of:");
                break;
            case EQUIPMENT_SLOTS:
                subBuilder.appendString("Equipment slots it will use:");
                break;
            case PROFICIENCIES:
                subBuilder.appendString("Proficiencies you will need for proper use:");
                break;
            case STATS:
                subBuilder.appendString("Stats that will change:");
                break;
            case DAMAGES:
                subBuilder.appendString("Causes damage like:");
                break;
            case ATTRIBUTE_SCORE:
                subBuilder.appendString("Changes to attribute scores:");
                break;
            case ATTRIBUTE_BONUS:
                subBuilder.appendString("Changes to attribute bonuses:");
                break;
            case INVISIBLE_CREATURE:
                subBuilder.appendString("Invisible creatures that you can see:");
                break;
            case INVISIBLE_ROOM_ITEM:
                subBuilder.appendString("Invisible objects that you can see:");
                break;
            case INVISIBLE_TAKEABLE:
                subBuilder.appendString("Invisible items that you can see:");
                break;
            case OTHER:
            default:
                if (SeeCategory.OTHER.toString().equalsIgnoreCase(category)) {
                    subBuilder.appendString("Other things that you can see:");
                } else {
                    subBuilder.appendString(category).appendString("that you can see:");
                }
                break;
            }
            subBuilder.appendTaggables(taggedList, ", ", "\r\n", "\r\n", "None");
        }

        if (this.effects != null && !this.effects.isEmpty()) {
            builder.appendString("Effects that you can fully see:");
            for (EntityEffectSource effectSource : this.effects) {
                builder.appendExaminable(effectSource, "\r\n", null);
            }
        }
    }

}
