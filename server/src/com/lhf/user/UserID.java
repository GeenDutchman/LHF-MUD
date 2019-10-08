package com.lhf.user;

public class UserID {
    private int index;
    // This is purely a placeholder, we probably want something better in here
    // Maybe include username in here at some point?
    private static int total_count = 0;
    public UserID() {
        index = total_count++;
    }
}
