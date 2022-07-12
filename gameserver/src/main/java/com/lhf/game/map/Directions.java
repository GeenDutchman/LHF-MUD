package com.lhf.game.map;

import com.lhf.Taggable;

public enum Directions implements Taggable {
    NORTH, SOUTH, EAST, WEST, UP, DOWN;

    public static Boolean isDirections(String value) {
        for (Directions dir : values()) {
            if (dir.toString().equalsIgnoreCase(value)
                    || (value.length() == 1 && dir.toString().substring(0, 1).equalsIgnoreCase(value))) {
                return true;
            }
        }
        return false;
    }

    public static Directions getDirections(String value) {
        for (Directions dir : values()) {
            if (dir.toString().equalsIgnoreCase(value)
                    || (value.length() == 1 && dir.toString().substring(0, 1).equalsIgnoreCase(value))) {
                return dir;
            }
        }
        return null;
    }

    @Override
    public String getStartTag() {
        return "<exit>";
    }

    @Override
    public String getEndTag() {
        return "</exit>";
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
