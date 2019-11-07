package com.lhf.game.battle;

public abstract class BattleAction {
    private BattleActionType actionType;

    public BattleAction(BattleActionType type) {
        actionType = type;
    }

    public BattleActionType getActionType() {
        return actionType;
    }
}
