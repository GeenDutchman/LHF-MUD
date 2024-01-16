package com.lhf.game.item.concrete;

import com.lhf.game.item.AItem;
import com.lhf.game.item.ItemVisitor;

public class Item extends AItem {

    public Item() {
        super("Item", "An Item");
    }

    public Item(String name) {
        super(name);
    }

    public Item(String name, String content) {
        super(name, content);
    }

    public Item(Item other) {
        super(other.getName(), other.descriptionString);
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Item makeCopy() {
        return this;
    }

}
