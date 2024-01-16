package com.lhf.game.item;

import com.lhf.game.item.concrete.Item;

public interface ItemVisitor {
    public void visit(InteractObject interactObject);

    public void visit(Item note);

    public void visit(Takeable takeable);

    public void visit(Usable usable);

    public void visit(Equipable equipable);

    public void visit(EquipableHiddenEffect equipableHiddenEffect);

    public void visit(Weapon weapon);
}
