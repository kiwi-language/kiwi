package tech.metavm;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

public class Lab {

    public static final Pattern ptn = Pattern.compile("对象'.+\\-(.+)'不存在");

    public static void main(String[] args) throws IOException {
        var enumSet = EnumSet.noneOf(Lab.Options.class);
        enumSet.add(Options.op1);
        System.out.println(enumSet);
    }

    private enum Options {

        op1,
        op2;

    }

    public void test(List<? extends Number> list) {
        helper(list);
    }

    public <T> void helper(List<T> t) {
        t.add(t.get(0));
    }

}
