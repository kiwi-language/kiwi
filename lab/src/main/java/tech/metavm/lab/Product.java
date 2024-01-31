package tech.metavm.lab;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

@EntityType("商品")
public class Product {

    @EntityField("标题")
    private String title;

//    @EntityField("数量")
//    private int amount;
//
//    @EntityField("价格")
//    private double price;

    @ChildEntity("规格列表")
    private final ChildList<SKU> skus = new ChildList<>();

    public Product(String title/*, int amount, double price*/) {
        this.title = title;
//        this.amount = amount;
//        this.price = price;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public void setAmount(int amount) {
//        this.amount = amount;
//    }

//    public void setPrice(double price) {
//        this.price = price;
//    }

    public String getTitle() {
        return title;
    }

//    public int getAmount() {
//        return amount;
//    }

//    public double getPrice() {
//        return price;
//    }

    public ChildList<SKU> getSkus() {
        return skus;
    }
}
