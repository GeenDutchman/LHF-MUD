package com.lhf.game.creature.statblock;

import com.lhf.game.enums.Attributes;

public class AttributeBlock {
    public static class attributeStripe {
        private Integer score;
        private Integer scoreBonus;
        private Integer modBonus;

        public attributeStripe(Integer score) {
            this.score = score;
            this.scoreBonus = 0;
            this.modBonus = 0;
        }

        public attributeStripe(Integer score, Integer scoreBonus, Integer modBonus) {
            this.score = score;
            this.scoreBonus = scoreBonus;
            this.modBonus = modBonus;
        }

        public attributeStripe(attributeStripe other) {
            this.score = other.score;
            this.scoreBonus = other.scoreBonus;
            this.modBonus = other.modBonus;
        }

        public Integer getScore() {
            return this.score;
        }

        public Integer setScore(Integer newScore) {
            this.score = newScore;
            return this.score;
        }

        public Integer getScoreBonus() {
            return this.scoreBonus;
        }

        public Integer setScoreBonus(Integer newBonus) {
            this.scoreBonus = newBonus;
            return this.scoreBonus;
        }

        public Integer getModBonus() {
            return this.modBonus;
        }

        public Integer setModBonus(Integer newBonus) {
            this.modBonus = newBonus;
            return this.modBonus;
        }

        public Integer getTotalScore() {
            return this.score + this.scoreBonus;
        }

        public Integer getMod() {
            return ((this.getTotalScore() - 10) / 2) + this.modBonus; // Integer division, round down
        }
    }

    // STR, DEX, CON, INT, WIS, CHA
    private attributeStripe strength, dexterity, constitution, intelligence, wisdom, charisma;

    public AttributeBlock() {
        this.strength = new AttributeBlock.attributeStripe(10);
        this.dexterity = new AttributeBlock.attributeStripe(10);
        this.constitution = new AttributeBlock.attributeStripe(10);
        this.intelligence = new AttributeBlock.attributeStripe(10);
        this.wisdom = new AttributeBlock.attributeStripe(10);
        this.charisma = new AttributeBlock.attributeStripe(10);
    }

    public AttributeBlock(AttributeBlock other) {
        this.strength = new attributeStripe(other.strength);
        this.dexterity = new attributeStripe(other.dexterity);
        this.constitution = new attributeStripe(other.constitution);
        this.intelligence = new attributeStripe(other.intelligence);
        this.wisdom = new attributeStripe(other.wisdom);
        this.charisma = new attributeStripe(other.charisma);
    }

    public AttributeBlock(attributeStripe strength, attributeStripe dexterity,
            attributeStripe constitution, attributeStripe intelligence, attributeStripe wisdom,
            attributeStripe charisma) {
        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
        this.constitution = constitution;
        this.wisdom = wisdom;
        this.charisma = charisma;
    }

    public AttributeBlock(Integer strength, Integer dexterity, Integer constitution, Integer intelligence,
            Integer wisdom, Integer charisma) {
        this.strength = new AttributeBlock.attributeStripe(strength);
        this.dexterity = new AttributeBlock.attributeStripe(dexterity);
        this.constitution = new AttributeBlock.attributeStripe(constitution);
        this.intelligence = new AttributeBlock.attributeStripe(intelligence);
        this.wisdom = new AttributeBlock.attributeStripe(wisdom);
        this.charisma = new AttributeBlock.attributeStripe(charisma);
    }

    public int getScore(Attributes attr) {
        if (attr == null) {
            return 0;
        }
        switch (attr) {
            case CHA:
                return this.charisma.getTotalScore();
            case CON:
                return this.constitution.getTotalScore();
            case DEX:
                return this.dexterity.getTotalScore();
            case INT:
                return this.intelligence.getTotalScore();
            case STR:
                return this.strength.getTotalScore();
            case WIS:
                return this.wisdom.getTotalScore();
            default:
                return 0;
        }
    }

    public Integer setScore(Attributes attr, Integer newScore) {
        if (attr == null) {
            return 0;
        }
        switch (attr) {
            case CHA:
                return this.charisma.setScore(newScore);
            case CON:
                return this.constitution.setScore(newScore);
            case DEX:
                return this.dexterity.setScore(newScore);
            case INT:
                return this.intelligence.setScore(newScore);
            case STR:
                return this.strength.setScore(newScore);
            case WIS:
                return this.wisdom.setScore(newScore);
            default:
                return 0;
        }
    }

    public int getScoreBonus(Attributes attr) {
        if (attr == null) {
            return 0;
        }
        switch (attr) {
            case CHA:
                return this.charisma.getScoreBonus();
            case CON:
                return this.constitution.getScoreBonus();
            case DEX:
                return this.dexterity.getScoreBonus();
            case INT:
                return this.intelligence.getScoreBonus();
            case STR:
                return this.strength.getScoreBonus();
            case WIS:
                return this.wisdom.getScoreBonus();
            default:
                return 0;
        }
    }

    public Integer setScoreBonus(Attributes attr, Integer newBonus) {
        if (attr == null) {
            return 0;
        }
        switch (attr) {
            case CHA:
                return this.charisma.setScoreBonus(newBonus);
            case CON:
                return this.constitution.setScoreBonus(newBonus);
            case DEX:
                return this.dexterity.setScoreBonus(newBonus);
            case INT:
                return this.intelligence.setScoreBonus(newBonus);
            case STR:
                return this.strength.setScoreBonus(newBonus);
            case WIS:
                return this.wisdom.setScoreBonus(newBonus);
            default:
                return 0;
        }
    }

    public int getMod(Attributes attr) {
        if (attr == null) {
            return 0;
        }
        switch (attr) {
            case CHA:
                return this.charisma.getMod();
            case CON:
                return this.constitution.getMod();
            case DEX:
                return this.dexterity.getMod();
            case INT:
                return this.intelligence.getMod();
            case STR:
                return this.strength.getMod();
            case WIS:
                return this.wisdom.getMod();
            default:
                return 0;
        }
    }

    public Integer setModBonus(Attributes attr, Integer newBonus) {
        if (attr == null) {
            return 0;
        }
        switch (attr) {
            case CHA:
                return this.charisma.setModBonus(newBonus);
            case CON:
                return this.constitution.setModBonus(newBonus);
            case DEX:
                return this.dexterity.setModBonus(newBonus);
            case INT:
                return this.intelligence.setModBonus(newBonus);
            case STR:
                return this.strength.setModBonus(newBonus);
            case WIS:
                return this.wisdom.setModBonus(newBonus);
            default:
                return 0;
        }
    }

    public int getModBonus(Attributes attr) {
        if (attr == null) {
            return 0;
        }
        switch (attr) {
            case CHA:
                return this.charisma.getModBonus();
            case CON:
                return this.constitution.getModBonus();
            case DEX:
                return this.dexterity.getModBonus();
            case INT:
                return this.intelligence.getModBonus();
            case STR:
                return this.strength.getModBonus();
            case WIS:
                return this.wisdom.getModBonus();
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Attributes attr : Attributes.values()) {
            sb.append(attr).append(":").append(this.getScore(attr))
                    .append(" (").append(this.getMod(attr)).append(")\n");
        }
        return sb.toString();
    }
}
