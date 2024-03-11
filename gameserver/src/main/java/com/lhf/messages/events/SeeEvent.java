package com.lhf.messages.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.StringJoiner;
import java.util.TreeMap;

import com.lhf.Examinable;
import com.lhf.Taggable;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.TickType;
import com.lhf.messages.GameEventType;

public class SeeEvent extends GameEvent {
    public enum SeeCategory {
        DIRECTION, CREATURE, PLAYER, NPC, MONSTER, ROOM_ITEM, TAKEABLE, EFFECTS, EQUIPMENT_SLOTS, PROFICIENCIES, STATS,
        ATTRIBUTE_SCORE, ATTRIBUTE_BONUS,
        DAMAGES, OTHER, INVISIBLE_CREATURE,
        INVISIBLE_ROOM_ITEM, INVISIBLE_TAKEABLE;

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

    private StringJoiner listTaggables(StringJoiner sj) {
        for (String category : this.seenCategorized.keySet()) {
            List<Taggable> taggedlist = this.seenCategorized.get(category);
            if (taggedlist == null || taggedlist.size() <= 0) {
                continue;
            }
            SeeCategory categorized = SeeCategory.getSeeCategory(category);
            if (categorized == null) {
                if (category == null) {
                    sj.add("Other things that you can see:");
                } else {
                    sj.add(category);
                }
            } else {
                switch (categorized) {
                    case DIRECTION:
                        sj.add("Available Directions:");
                        break;
                    case CREATURE:
                        sj.add("Creatures that you can see:");
                        break;
                    case PLAYER:
                        sj.add("Players that you can see:");
                        break;
                    case NPC:
                        sj.add("Non Player Characters that you can see:");
                        break;
                    case MONSTER:
                        sj.add("Monsters that you can see:");
                        break;
                    case ROOM_ITEM:
                        sj.add("Objects that you can see:");
                        break;
                    case TAKEABLE:
                        sj.add("Items that you can see:");
                        break;
                    case EFFECTS:
                        sj.add("Effects that you know of:");
                        break;
                    case EQUIPMENT_SLOTS:
                        sj.add("Equipment slots it will use:");
                        break;
                    case PROFICIENCIES:
                        sj.add("Proficiencies you will need for proper use:");
                        break;
                    case STATS:
                        sj.add("Stats that will change:");
                        break;
                    case DAMAGES:
                        sj.add("Causes damage like:");
                        break;
                    case ATTRIBUTE_SCORE:
                        sj.add("Changes to attribute scores:");
                        break;
                    case ATTRIBUTE_BONUS:
                        sj.add("Changes to attribute bonuses:");
                        break;
                    case INVISIBLE_CREATURE:
                        sj.add("Invisible creatures that you can see:");
                        break;
                    case INVISIBLE_ROOM_ITEM:
                        sj.add("Invisible objects that you can see:");
                        break;
                    case INVISIBLE_TAKEABLE:
                        sj.add("Invisible items that you can see:");
                        break;
                    case OTHER:
                    default:
                        sj.add("Other things that you can see:");
                        break;
                }
            }
            sj.add("\r\n");
            for (Taggable taggable : taggedlist) {
                sj.add(taggable.getColorTaggedName());
            }
            sj.add("\r\n");
        }
        return sj;
    }

    private String listEffectors() {
        StringJoiner sj = new StringJoiner("\r\n");
        if (this.effects != null && this.effects.size() > 0) {
            sj.add("Effects that you can see:");
            for (EntityEffectSource entityEffect : this.effects) {
                sj.add(entityEffect.toString());
            }
        }
        return sj.toString();
    }

    @Override
    public String toString() {
        if (this.isDenied()) {
            return this.deniedReason;
        }
        if (this.examinable == null) {
            return "You cannot see that.";
        }
        StringJoiner sj = new StringJoiner(" ");
        if (this.examinable instanceof Taggable) {
            sj.add("Name:").add(((Taggable) this.examinable).getColorTaggedName());
        } else {
            sj.add("Name:").add(this.examinable.getName());
        }
        sj.add("\r\n");
        if (this.extraInfo != null && this.extraInfo.length() > 0) {
            sj.add(this.extraInfo.toString()).add("\r\n");
        }
        final String descriptor = this.examinable.printDescription();
        if (descriptor != null && !descriptor.isBlank()) {
            sj.add("<description>").add(descriptor).add("</description>").add("\r\n");
        }
        sj = this.listTaggables(sj);
        String listedEffects = this.listEffectors();
        if (!listedEffects.isBlank()) {
            sj.add("\r\n").add(listedEffects);
        }
        return sj.toString();
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
    public String print() {
        return this.toString();
    }
}
