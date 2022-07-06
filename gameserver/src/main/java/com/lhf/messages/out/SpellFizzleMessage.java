package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.game.creature.Creature;

public class SpellFizzleMessage extends OutMessage {
    public enum SpellFizzleType {
        NOT_CASTER, BAD_POWER, NOT_SPELL, OTHER;
    }

    private SpellFizzleType type;
    private Creature attempter;
    private boolean addressAttempter;

    public SpellFizzleMessage(SpellFizzleType type, Creature attempter, boolean addressAttempter) {
        this.type = type;
        this.attempter = attempter;
        this.addressAttempter = addressAttempter;
    }

    @Override
    public String toString() {
        if (!this.addressAttempter) {
            StringJoiner sj = new StringJoiner(" ");
            sj.add(this.attempter.getColorTaggedName());
            sj.add("mumbles and tries to cast a spell...nothing spectacular happens.");
            return sj.toString();
        }
        switch (this.type) {
            case NOT_CASTER:
                return "You are not a caster type, so you cannot cast spells.";
            case BAD_POWER:
                return "You have insufficient power to cast that spell.";
            case NOT_SPELL:
                return "That is not a spell that you can cast.";
            case OTHER:
            default:
                return "Weird, that spell should have done something.";
        }
    }

    public SpellFizzleType getType() {
        return type;
    }

    public Creature getAttempter() {
        return attempter;
    }

}
