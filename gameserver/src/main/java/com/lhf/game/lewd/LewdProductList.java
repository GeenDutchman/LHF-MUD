package com.lhf.game.lewd;

import java.util.ArrayList;
import java.util.List;

import com.lhf.game.lewd.LewdManager.LewdProduct;
import com.lhf.game.map.Room;

public class LewdProductList implements LewdProduct {
    protected List<LewdProduct> products;

    public LewdProductList() {
        products = new ArrayList<>();
    }

    public LewdProductList(List<LewdProduct> products) {
        this.products = products;
    }

    public LewdProductList addProduct(LewdProduct product) {
        if (product == this) {
            return this;
        }
        this.products.add(product);
        return this;
    }

    public void onLewd(Room room, VrijPartij party) {
        for (LewdProduct product : this.products) {
            if (product == null || product == this) {
                continue;
            }
            product.onLewd(room, party);
        }
    }
}
