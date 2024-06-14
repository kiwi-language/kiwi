package org.metavm.autograph.mocks;

import org.metavm.entity.ChildEntity;

import java.util.ArrayList;
import java.util.List;

public class AstForeachFoo {

    @ChildEntity
    private final List<Double> prices = new ArrayList<>();

    public double getAveragePrice() {
        if (prices.isEmpty())
            return 0.0;
        double totalPrice = 0.0;
        for (var price : prices) {
            totalPrice += price;
        }
        return totalPrice / prices.size();
    }

    public double loop2(List<List<Double>> prices) {
        if (prices.isEmpty())
            return 0.0;
        double totalPrice = 0.0;
        for (List<Double> prices2 : prices) {
            for (var price : prices2) {
                totalPrice += price;
            }
        }
        return totalPrice / prices.size();
    }

    public double loop3(List<List<Double>> prices) {
        if (prices.isEmpty())
            return 0.0;
        double totalPrice = 0.0;
        int i_1 = 0;
        var list_1 = prices;
        while (i_1 < list_1.size()) {
            var prices2 = list_1.get(i_1++);
            int i_2 = 0;
            var list_2 = prices2;
            while (i_2 < list_2.size()) {
                var price = list_2.get(i_2++);
                totalPrice += price;
            }
        }
        return totalPrice / prices.size();
    }

}
