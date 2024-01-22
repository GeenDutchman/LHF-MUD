package com.lhf.game.lewd;

import java.util.ArrayList;
import java.util.List;

import com.lhf.game.map.Area;

public class LewdProductList extends LewdProduct {
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

    public void onLewd(Area room, VrijPartij party) {
        for (LewdProduct product : this.products) {
            if (product == null || product == this) {
                continue;
            }
            product.onLewd(room, party);
        }
    }
}
