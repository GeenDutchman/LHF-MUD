package com.lhf.game.serialization;

import java.io.IOException;
import java.util.logging.Level;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lhf.game.item.IItem;
import com.lhf.game.item.IItem.ItemID;

public class CachedIItemTypeAdapter extends AbstractTypeAdapter<IItem> {
    @Override
    public void write(JsonWriter out, IItem value) throws IOException {
        delegateWrite(out, value.getItemID(), ItemID.class);
    }

    @Override
    public IItem read(JsonReader in) throws IOException {
        final ItemID id = delegateRead(in, ItemID.class);
        if (id == null) {
            this.logger.log(Level.CONFIG, "null encountered while reading item");
            return null;
        }
        return this.cache.get().getItemSaverVisitor().getItemsMap().getOrDefault(id, null);
    }
}
