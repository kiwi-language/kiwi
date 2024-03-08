import java.util.List;

public class NestedForeachLoop {

    public long test(List<List<Double>> prices) {
        double total = 0.0;
        for (List<Double> price : prices) {
            for (Double v : price) {
                total += v;
            }
        }
        return (long) total;
    }

}
