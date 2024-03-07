package com.lhf.game.lewd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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

    @Override
    public Consumer<Area> onLewdAreaChanges(VrijPartij party) {
        if (party == null) {
            return null;
        }
        return new Consumer<Area>() {

            @Override
            public void accept(Area arg0) {
                if (arg0 == null) {
                    return;
                }
                for (final LewdProduct product : LewdProductList.this.products) {
                    if (product == null || product == LewdProductList.this) {
                        continue;
                    }
                    final Consumer<Area> next = product.onLewdAreaChanges(party);
                    if (next != null) {
                        next.accept(arg0);
                    }
                }
            }

        };
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
