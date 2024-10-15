package loops;

public class ModifyVariableInWhileCondition {

    public static int test(int index) {
        int i = index;
        while (--i > 100);
        return i;
    }

}
