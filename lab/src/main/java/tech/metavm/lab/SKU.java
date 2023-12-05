package tech.metavm.lab;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("规格")
public class SKU {

    @EntityField("标题")
    private String title;

    @EntityField("数量")
    private int amount;

    @EntityField("价格")
    private double price;

    public SKU(String title, int amount, double price) {
        this.title = title;
        this.amount = amount;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
