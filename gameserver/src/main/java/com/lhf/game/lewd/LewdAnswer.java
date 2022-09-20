package com.lhf.game.lewd;

public enum LewdAnswer {
    DENIED, ASKED, ACCEPTED;

    public static LewdAnswer merge(LewdAnswer a, LewdAnswer b) {
        if (a == null && b == null) {
            return ASKED;
        } else if (a == null && b != null) {
            return b;
        } else if (a != null && b == null) {
            return a;
        } else if (a == b) {
            return a;
        } else if (a == DENIED || b == DENIED) {
            return DENIED;
        } else if (a == ACCEPTED || b == ACCEPTED) {
            return ACCEPTED;
        }
        return ASKED;
    }
}