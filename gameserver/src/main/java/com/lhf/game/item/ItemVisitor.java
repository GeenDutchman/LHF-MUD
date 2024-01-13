package com.lhf.game.item;

import com.lhf.game.item.concrete.Note;

public interface ItemVisitor {
    public void visit(InteractObject interactObject);

    public void visit(Note note);

    public void visit(Takeable takeable);

    public void visit(Usable usable);

    public void visit(Equipable equipable);

    public void visit(Weapon weapon);

    public void visit(StackableItem stackableItem);
}
