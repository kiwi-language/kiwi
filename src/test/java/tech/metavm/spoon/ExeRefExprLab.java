package tech.metavm.spoon;

import java.util.function.BiFunction;

public class ExeRefExprLab {

    public void test() {
        foo(ExeRefExprLab::qux);
    }

    public void foo(BiFunction<ExeRefExprLab, String, Object> function) {

    }

    public Object qux(String str) {
        return str;
    }
}
