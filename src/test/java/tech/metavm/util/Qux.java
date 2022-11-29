package tech.metavm.util;

import tech.metavm.entity.ValueType;

@ValueType("量子X")
public class Qux {

    private final double price;
    private final long amount;

    public Qux(double price, long amount) {
        this.price = price;
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public long getAmount() {
        return amount;
    }
}
