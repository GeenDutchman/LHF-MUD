package com.lhf.game.serialization;

import java.io.IOException;
import java.util.logging.Level;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.ICreature.ICreatureID;

public class CachedICreatureTypeAdapter extends AbstractTypeAdapter<ICreature> {
    @Override
    public void write(JsonWriter out, ICreature value) throws IOException {
        delegateWrite(out, value.getCreatureID(), ICreatureID.class);
    }

    @Override
    public ICreature read(JsonReader in) throws IOException {
        final ICreatureID id = delegateRead(in, ICreatureID.class);
        if (id == null) {
            this.logger.log(Level.CONFIG, "null encountered while reading creature");
            return null;
        }
        return this.cache.get().getICreaturesMap().getOrDefault(id, null);
    }
}
