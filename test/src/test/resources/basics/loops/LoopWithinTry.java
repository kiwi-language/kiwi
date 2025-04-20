package loops;

public class LoopWithinTry {

    public static int sum(int n) {
        int i = 1;
        int sum = 0;
        try {
            for (; i <= n; i++) {
                sum += i;
            }
            return sum;
        }
        catch (Throwable e) {
            return 0;
        }
    }

}
