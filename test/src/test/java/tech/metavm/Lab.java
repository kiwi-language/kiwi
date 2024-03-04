package tech.metavm;

public class Lab {

    public int testCovered(Kind kind) {
        int ret;
        switch (kind) {
            case STRING -> ret = 1;
            case LONG -> ret = 2;
            case INTEGER -> ret = 3;
            case null -> ret = 4;
            default -> throw new IllegalStateException();
        }
        return ret;
    }

    public void testUncovered(Kind kind) {
        switch (kind) {
            case STRING -> System.out.println("string");
            case LONG -> System.out.println("long");
            case INTEGER -> System.out.println("integer");
            default -> {
            }
        }
    }

    public void testSwitchExpression(Kind kind) {
        int switchResult;
        switch (kind) {
            case STRING -> switchResult = 1;
            case LONG -> switchResult = 2;
            case INTEGER -> switchResult = 3;
            case null -> throw new IllegalStateException();
            default -> throw new IllegalStateException();
        }
        int ret = switchResult;
        System.out.println(ret);
    }

    public void testColonSwitchExpression(Kind kind) {
        int switchResult;
        switch (kind) {
            case STRING -> {
                switchResult = 1;
            }
            case LONG -> {
                switchResult = 0;
            }
            case INTEGER -> {
                switchResult = 0;
            }
            case null -> {
                throw new IllegalStateException();
            }
            default -> throw new IllegalStateException();
        }
        int ret = switchResult;
        System.out.println(ret);
    }


    public enum Kind {
        STRING,
        LONG,
        INTEGER,
    }

}
