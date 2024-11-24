package localclass;

public class LocalClassNameConflictFoo {

    public static Object foo(int v) {
        class Local {
            int value = v;
        }
        return new Local().value;
    }

    class Local {
    }

    class $1 {}

    public static Object test() {
        return foo(1);
    }


}
