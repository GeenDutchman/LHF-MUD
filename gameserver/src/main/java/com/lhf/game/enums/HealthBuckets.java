package com.lhf.game.enums;

import com.lhf.Taggable;

public enum HealthBuckets implements Taggable {
    HEALTHY, LIGHTLY_INJURED, INJURED, HEAVILY_INJURED, CRITICALLY_INJURED, NEAR_DEATH, DEAD;

    public static HealthBuckets calcualte(int currentHealth, int totalHealth) {
        Double percent = currentHealth / (double) totalHealth;

        if (percent <= 0) {
            return DEAD;
        } else if (percent <= .05) {
            return NEAR_DEATH;
        } else if (percent <= .1) {
            return CRITICALLY_INJURED;
        } else if (percent <= .5) {
            return HEAVILY_INJURED;
        } else if (percent <= .75) {
            return INJURED;
        } else if (percent <= .9) {
            return LIGHTLY_INJURED;
        } else {
            return HEALTHY;
        }
    }

    @Override
    public String getStartTag() {
        return "<health>";
    }

    @Override
    public String getEndTag() {
        return "</health>";
    }

    @Override
    public String toString() {
        return this.name().toLowerCase().replace('_', ' ');
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.toString() + this.getEndTag();
    }
}
