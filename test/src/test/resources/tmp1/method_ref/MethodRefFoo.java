package method_ref;

import java.util.Map;

public class MethodRefFoo {

    public void test(Map<MethodRefFoo, Object> map) {
        map.forEach(MethodRefFoo::action);
        map.forEach(MethodRefFoo::action2);
        map.forEach(this::action3);
    }
    private static void action(MethodRefFoo key, Object value) {}

    private void action2(Object value) {

    }

    private void action3(MethodRefFoo key, Object value) {}

}