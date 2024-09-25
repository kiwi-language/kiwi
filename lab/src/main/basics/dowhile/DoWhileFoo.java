package dowhile;

public class DoWhileFoo {

    public static int sum(int from, int to) {
        var sum = 0;
        var v = from;
        do {
            sum += v;
            v++;
        } while (v <= to);
        return sum;
    }

}
