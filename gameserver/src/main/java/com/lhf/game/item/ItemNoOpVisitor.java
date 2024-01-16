package com.lhf.game.item;

import com.lhf.game.item.concrete.Item;

public class ItemNoOpVisitor implements ItemVisitor {

    @Override
    public void visit(InteractObject interactObject) {
        // deliberate no-operation
    }

    @Override
    public void visit(Item note) {
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
    public void visit(EquipableHiddenEffect equipableHiddenEffect) {
        // deliberate no-operation
    }

}
