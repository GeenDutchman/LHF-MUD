package com.lhf.game.item.interfaces;

public interface Takeable {
    String getName();

    boolean checkName(String name);

    boolean CheckNameRegex(String possName, Integer minimumLength);
}
