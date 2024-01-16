package com.lhf.game.item.concrete;

import com.lhf.game.item.Item;
import com.lhf.game.item.ItemVisitor;

public class NotableFixture extends Item {

    public NotableFixture(String name, String content) {
        super(name, content);
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NotableFixture makeCopy() {
        return new NotableFixture(this.getName(), this.descriptionString);
    }

}
