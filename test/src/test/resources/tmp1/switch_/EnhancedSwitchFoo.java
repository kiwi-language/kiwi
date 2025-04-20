package switch_;

public class EnhancedSwitchFoo {

    public static int testExpression(int v) {
        return switch(v) {
            case 0 -> 0;
            case 1,2,3 -> 1;
            case 4 -> 2;
            default -> -1;
        };
    }

    public static int testSwitchStatement(int v) {
        int r;
        switch (v) {
            case 0 -> r = 0;
            case 1,2,3 -> r = 1;
            case 4 -> r = 2;
            default -> {
                throw new IllegalStateException("Invalid value: " + v);
            }
        }
        return r;
    }

}