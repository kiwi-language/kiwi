package trycatch;

public class TrySectionBreakFoo {

    public static int divCount = 0;
    public static int defaultCount = 0;
    public static int breakCount = 0;
    public static int loopCount = 0;

    public static int testReturn(int u, int v) {
        try {
            return  u / v;
        }
        catch (ArithmeticException e) {
            return Integer.MAX_VALUE;
        }
        finally {
            divCount++;
        }
    }

    public static int testYield(int value) {
        return switch (value) {
            case 1 -> 1;
            case 2 -> 2;
            default -> {
                try {
                    yield 100 / value;
                } catch (ArithmeticException e) {
                    yield -1;
                }
                finally {
                    defaultCount++;
                }
            }
        };
    }

    public static int testBreak(int value) {
        out: {
            try {
                if (value == 0)
                    break out;
                return 100 / value;
            }
            finally {
                breakCount++;
            }
        }
        return -1;
    }

    public static int testContinue(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            try {
                if (i % 5 == 0)
                    continue;
                sum += i;
            } finally {
                loopCount++;
            }
        }
        return sum;
    }

}
