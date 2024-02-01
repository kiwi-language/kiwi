package tech.metavm.lab;

import tech.metavm.entity.*;

import java.util.ArrayList;
import java.util.List;

@EntityType("商品")
public class Product {

    @EntityField("标题")
    private String title;

    @ChildEntity("规格列表")
    private final ChildList<SKU> skus = new ChildList<>();

    public Product(String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public List<SKU> getSkus() {
        return skus;
    }

    public void setSkus(List<SKU> skus) {

    }

    @EntityFlow("获取价格")
    public double getPrice() {
        if(skus.isEmpty())
            return 0.0;
        double totalPrice = 0.0;
        for (SKU sku : skus) {
            totalPrice += sku.getPrice();
        }
        return totalPrice / skus.size();
    }

}
