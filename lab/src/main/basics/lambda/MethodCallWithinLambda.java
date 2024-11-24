package lambda;

import org.metavm.api.lang.Lang;

import java.util.List;

public class MethodCallWithinLambda {

    public void print(List<Object> list) {
        //noinspection Convert2MethodRef
        list.forEach(e -> print(e));
    }

    private void print(Object o) {
        Lang.print(o);
    }

    public static boolean test() {
        new MethodCallWithinLambda().print(List.of("a", "b", "c"));
        return true;
    }

}
