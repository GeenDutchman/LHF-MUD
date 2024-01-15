package com.lhf.game.item;

import com.lhf.game.item.concrete.NotableFixture;

public interface ItemVisitor {
    public void visit(InteractObject interactObject);

    public void visit(NotableFixture note);

    public void visit(Takeable takeable);

    public void visit(Usable usable);

    public void visit(Equipable equipable);

    public void visit(Weapon weapon);

    public void visit(StackableItem stackableItem);
}
