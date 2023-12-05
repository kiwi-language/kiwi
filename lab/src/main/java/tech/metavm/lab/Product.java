package tech.metavm.lab;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

@EntityType("商品")
public class Product {

    @EntityField("标题")
    private String title;

    @EntityField("数量")
    private int amount;

    @EntityField("价格")
    private double price;

    @EntityField("是否下架")
    private boolean offShelf = false;

    @EntityField("销量")
    private long sales = 0;

    @ChildEntity("规格列表")
    private List<SKU> skus = new ArrayList<>();

    public Product(String title, int amount, double price) {
        this.title = title;
        this.amount = amount;
        this.price = price;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public int getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public boolean isOffShelf() {
        return offShelf;
    }

    public void setOffShelf(boolean offShelf) {
        this.offShelf = offShelf;
    }


    public long getSales() {
        return sales;
    }


}
