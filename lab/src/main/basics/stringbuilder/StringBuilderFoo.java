package stringbuilder;

import java.util.List;

public class StringBuilderFoo {

    public static String build(List<Object> list) {
        var sb = new StringBuilder();
        for (Object o : list) {
            if(!sb.isEmpty())
                sb.append(' ');
            sb.append(o.toString());
        }
        return sb.toString();
    }

}
