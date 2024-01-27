package com.lhf.game.lewd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(products);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof LewdProductList))
            return false;
        LewdProductList other = (LewdProductList) obj;
        return Objects.equals(products, other.products);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LewdProductList [products=").append(products).append("]");
        return builder.toString();
    }

}
