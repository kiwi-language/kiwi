package tech.metavm;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class Lab {

    public static final Pattern ptn = Pattern.compile("对象'.+\\-(.+)'不存在");

    public static void main(String[] args) throws IOException {
        var str = "对象'字段-058aa8d6b9078a03'不存在";
        var m = ptn.matcher(str);
        System.out.println(m.matches());
        System.out.println(m.group(1));

    }

    public void test(List<? extends Number> list) {
        helper(list);
    }

    public <T> void helper(List<T> t) {
        t.add(t.get(0));
    }

}
