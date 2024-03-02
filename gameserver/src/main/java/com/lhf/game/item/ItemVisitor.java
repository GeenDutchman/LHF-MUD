package com.lhf.game.item;

import java.util.function.Consumer;

import com.lhf.game.item.concrete.Item;

public interface ItemVisitor extends Consumer<IItem> {
    public void visit(InteractObject interactObject);

    public void visit(Item note);

    public void visit(Takeable takeable);

    public void visit(Usable usable);

    public void visit(Equipable equipable);

    public void visit(EquipableHiddenEffect equipableHiddenEffect);

    public void visit(Weapon weapon);

    @Override
    public default void accept(IItem arg0) {
        if (arg0 != null) {
            arg0.acceptItemVisitor(this);
        }
    }
}
