package com.lhf.game.enums;

import com.lhf.Taggable;

public enum HealthBuckets implements Taggable {
    DEAD(0f), NEAR_DEATH(0.05f), CRITICALLY_INJURED(0.1f), HEAVILY_INJURED(0.5f), INJURED(0.75f), LIGHTLY_INJURED(0.9f),
    HEALTHY(1.0f);

    private final float value;

    private HealthBuckets(float value) {
        this.value = value;
    }

    public static HealthBuckets fromPercent(double percent) {
        if (percent <= DEAD.value) {
            return DEAD;
        } else if (percent <= NEAR_DEATH.value) {
            return NEAR_DEATH;
        } else if (percent <= CRITICALLY_INJURED.value) {
            return CRITICALLY_INJURED;
        } else if (percent <= HEAVILY_INJURED.value) {
            return HEAVILY_INJURED;
        } else if (percent <= INJURED.value) {
            return INJURED;
        } else if (percent <= LIGHTLY_INJURED.value) {
            return LIGHTLY_INJURED;
        } else {
            return HEALTHY;
        }
    }

    public static HealthBuckets calculate(int currentHealth, int totalHealth) {
        if (totalHealth == 0) {
            return HealthBuckets.DEAD;
        }
        Double percent = currentHealth / (double) totalHealth;

        return HealthBuckets.fromPercent(percent);
    }

    public float getValue() {
        return value;
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
