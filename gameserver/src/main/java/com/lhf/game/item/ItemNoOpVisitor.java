package com.lhf.game.item;

import com.lhf.game.item.concrete.NotableFixture;

public class ItemNoOpVisitor implements ItemVisitor {

    @Override
    public void visit(InteractObject interactObject) {
        // deliberate no-operation
    }

    @Override
    public void visit(NotableFixture note) {
        // deliberate no-operation
    }

    @Override
    public void visit(Takeable takeable) {
        // deliberate no-operation
    }

    @Override
    public void visit(Usable usable) {
        // deliberate no-operation
    }

    @Override
    public void visit(Equipable equipable) {
        // deliberate no-operation
    }

    @Override
    public void visit(Weapon weapon) {
        // deliberate no-operation
    }

    @Override
    public void visit(StackableItem stackableItem) {
        // deliberate no-operation
    }

}
