package com.lhf.game.map;

import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.lhf.game.creature.ICreature;

public class Doorway {
    protected final String className;

    public Doorway() {
        this.className = this.getClass().getName();
    }

    public boolean testTraversal(ICreature creature, Directions direction, Area source, Area dest) {
        return true;
    }

    public String getClassName() {
        return className;
    }

    public static RuntimeTypeAdapterFactory<Doorway> runTimeAdapter() {
        final RuntimeTypeAdapterFactory<Doorway> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
                .of(Doorway.class, "className", true).recognizeSubtypes();
        return runtimeTypeAdapterFactory;
    }

}
