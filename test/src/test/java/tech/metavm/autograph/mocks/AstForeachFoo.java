package tech.metavm.autograph.mocks;

import tech.metavm.entity.ChildEntity;

import java.util.ArrayList;
import java.util.List;

public class AstForeachFoo {

    @ChildEntity("价格列表")
    private final List<Double> prices = new ArrayList<>();

    public double getAveragePrice() {
        if(prices.isEmpty())
            return 0.0;
        double totalPrice = 0.0;
        for (var price : prices) {
            totalPrice += price;
        }
        return totalPrice / prices.size();
    }

}
