package com.lhf.game.map;

import java.util.function.Consumer;

public interface AreaVisitor extends Consumer<Area> {
    public void visit(Room room);

    public void visit(DMRoom room);

    @Override
    default void accept(Area arg0) {
        if (arg0 != null) {
            arg0.acceptAreaVisitor(this);
        }
    }

    public interface AreaVisitorAcceptor {
        public void acceptAreaVisitor(AreaVisitor visitor);
    }
}
