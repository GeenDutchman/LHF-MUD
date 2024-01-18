package com.lhf.game.creature.inventory;

import java.io.IOException;
import java.util.logging.Level;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lhf.game.item.IItem;
import com.lhf.game.item.IItem.ItemID;
import com.lhf.game.item.Takeable;
import com.lhf.game.serialization.AbstractTypeAdapter;

public class InventoryTypeAdapter extends AbstractTypeAdapter<Inventory> {
    @Override
    public Inventory read(JsonReader in) throws IOException {
        final Inventory inventory = new Inventory();
        in.beginObject();
        while (in.hasNext()) {
            String nextString = in.nextString();
            if (nextString == null) {
                this.logger.log(Level.CONFIG, "null encountered while reading Inventory");
                continue;
            }
            switch (nextString) {
                case "null":
                    this.logger.log(Level.CONFIG, "json null encountered while reading Inventory");
                    continue;
                default:
                    final ItemID id = this.rawRead(nextString, ItemID.class);
                    if (id == null) {
                        this.logger.log(Level.CONFIG, String.format("could not read item id from %s", nextString));
                        continue;
                    }
                    final Takeable toPlace = this.cache.get().getItemSaverVisitor().getTakeablesMap().get(id);
                    if (toPlace == null) {
                        this.logger.log(Level.CONFIG, String.format("could not retrieve takeable for id %s", id));
                        continue;
                    }
                    inventory.addItem(toPlace);
                    break;
            }
        }
        in.endObject();
        return inventory;
    }

    @Override
    public void write(JsonWriter out, Inventory value) throws IOException {
        out.beginArray();
        for (final IItem item : value.getItems()) {
            if (item == null) {
                this.logger.log(Level.CONFIG, "null encountered while serializing Inventory");
                out.nullValue();
                continue;
            }
            delegateWrite(out, item.getItemID(), ItemID.class);
        }
        out.endArray();
    }
}
