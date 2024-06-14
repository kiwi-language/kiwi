package org.metavm.mocks;

import org.metavm.entity.Entity;
import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;

@EntityType
public class Product extends Entity {
    @EntityField(asTitle = true)
    private String title;
    private long inventory;
    private double price;

    public Product(String title, long inventory, double price) {
        this.title = title;
        this.inventory = inventory;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getInventory() {
        return inventory;
    }

    public void setInventory(long inventory) {
        this.inventory = inventory;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
