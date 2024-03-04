package tech.metavm.autograph.mocks;

public class AstSwitchCoverFoo {

    public int testCovered(Kind kind) {
        int ret;
        switch (kind) {
            case STRING -> ret = 1;
            case LONG -> ret = 2;
            case INTEGER -> ret = 3;
            case null -> ret = 4;
        }
        return ret;
    }

    public void testUncovered(Kind kind) {
        switch (kind) {
            case STRING -> System.out.println("string");
            case LONG -> System.out.println("long");
            case INTEGER -> System.out.println("integer");
        }
    }

    public void testSwitchExpression(Kind kind) {
        int ret = switch (kind) {
            case STRING -> 1;
            case LONG -> 2;
            case INTEGER -> 3;
        };
        System.out.println(ret);
    }

    public void testColonSwitchExpression(Kind kind) {
        int ret = switch (kind) {
            case STRING : yield 1;
            case LONG: yield 0;
            case INTEGER: yield 0;
        };
        System.out.println(ret);
    }

    public enum Kind {
        STRING,
        LONG,
        INTEGER,
    }

}
