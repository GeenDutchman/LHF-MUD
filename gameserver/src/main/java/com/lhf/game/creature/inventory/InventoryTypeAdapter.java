package com.lhf.game.creature.inventory;

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lhf.game.item.IItem;
import com.lhf.game.item.IItem.ItemID;
import com.lhf.game.serialization.AbstractTypeAdapter;

public class InventoryTypeAdapter extends AbstractTypeAdapter<Inventory> {
    @Override
    public Inventory read(JsonReader in) throws IOException {
        // TODO Auto-generated method stub
        return super.read(in);
    }

    @Override
    public void write(JsonWriter out, Inventory value) throws IOException {
        out.beginArray();
        for (final IItem item : value.getItems()) {
            if (item == null) {
                out.nullValue();
                continue;
            }
            delegateWrite(out, item.getItemID(), ItemID.class);
        }
        out.endArray();
    }
}
