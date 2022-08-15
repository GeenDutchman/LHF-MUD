package com.lhf.messages.out;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import com.lhf.Examinable;
import com.lhf.Taggable;
import com.lhf.game.EntityEffector;
import com.lhf.messages.OutMessageType;

public class SeeOutMessage extends OutMessage {
    public enum SeeCategory {
        DIRECTION, CREATURE, PLAYER, NPC, MONSTER, ROOM_ITEM, TAKEABLE, SPELL, STATS, ATTRIBUTE_SCORE, ATTRIBUTE_BONUS,
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

    private Examinable examinable;
    private Map<String, List<Taggable>> seenCategorized;
    private List<EntityEffector> effectors;
    private StringJoiner extraInfo;
    private String deniedReason;

    public SeeOutMessage(Examinable examinable) {
        super(OutMessageType.SEE);
        this.examinable = examinable;
        this.extraInfo = new StringJoiner("\r\n").setEmptyValue("");
        this.deniedReason = null;
        this.seenCategorized = new TreeMap<>();
        this.effectors = new ArrayList<>();
    }

    public SeeOutMessage(Examinable examinable, String extraInfo) {
        super(OutMessageType.SEE);
        this.examinable = examinable;
        this.extraInfo = new StringJoiner("\r\n").add(extraInfo.trim());
        this.deniedReason = null;
        this.seenCategorized = new TreeMap<>();
        this.effectors = new ArrayList<>();
    }

    public SeeOutMessage(String deniedReason) {
        super(OutMessageType.SEE);
        this.deniedReason = deniedReason.trim();
        this.examinable = null;
        this.extraInfo = new StringJoiner("\r\n").setEmptyValue("");
        this.seenCategorized = new TreeMap<>();
        this.effectors = new ArrayList<>();
    }

    public SeeOutMessage addExtraInfo(String extraInfo) {
        this.extraInfo.add(extraInfo);
        return this;
    }

    public SeeOutMessage addSeen(String category, Taggable thing) {
        if (this.seenCategorized == null) {
            this.seenCategorized = new TreeMap<>();
        }
        this.seenCategorized.putIfAbsent(category, new ArrayList<>());
        this.seenCategorized.get(category).add(thing);
        return this;
    }

    public SeeOutMessage addSeen(SeeCategory category, Taggable thing) {
        this.addSeen(category.name(), thing);
        return this;
    }

    public SeeOutMessage addEffector(EntityEffector effector) {
        this.effectors.add(effector);
        return this;
    }

    private StringJoiner listTaggables(StringJoiner sj) {
        for (String category : this.seenCategorized.keySet()) {
            List<Taggable> taggedlist = this.seenCategorized.get(category);
            if (taggedlist == null || taggedlist.size() <= 0) {
                continue;
            }
            SeeCategory categorized = SeeCategory.getSeeCategory(category);
            if (categorized == null) {
                sj.add(category);
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
                    case SPELL:
                        sj.add("Spells that you know of:");
                        break;
                    case STATS:
                        sj.add("Stats that will change:");
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
        // TODO: poor man's way, not parseable
        StringJoiner sj = new StringJoiner("\r\n");
        if (this.effectors != null && this.effectors.size() > 0) {
            sj.add("Effects that you can see:");
            for (EntityEffector entityEffector : this.effectors) {
                sj.add(entityEffector.toString());
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
            sj.add(((Taggable) this.examinable).getColorTaggedName());
        } else {
            sj.add(this.examinable.getName());
        }
        sj.add("\r\n");
        if (this.extraInfo != null && this.extraInfo.length() > 0) {
            sj.add(this.extraInfo.toString()).add("\r\n");
        }
        sj.add("<description>").add(this.examinable.printDescription()).add("</description>").add("\r\n");
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
        return this.seenCategorized.get(category);
    }

    public List<Taggable> getTaggedCategory(SeeCategory category) {
        return this.getTaggedCategory(category.name());
    }

    public boolean isDenied() {
        return this.deniedReason != null;
    }

    public List<EntityEffector> getEffectors() {
        return Collections.unmodifiableList(effectors);
    }
}
