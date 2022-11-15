package com.lhf.game.lewd;

public enum LewdAnswer {
    INCLUDED, ASKED, DENIED, ACCEPTED;

    public static LewdAnswer merge(LewdAnswer a, LewdAnswer b) {
        if (a == null && b == null) {
            return INCLUDED;
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
        } else if (a == ASKED || b == ASKED) {
            return ASKED;
        }
        return INCLUDED;
    }
}