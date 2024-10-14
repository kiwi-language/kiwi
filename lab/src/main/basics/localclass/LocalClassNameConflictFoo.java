package localclass;

public class LocalClassNameConflictFoo {

    public static Object foo() {
        class Local {
        }
        return new Local();
    }

    class Local {
    }

    class $1 {}

    public static Object test() {
        return foo();
    }


}
