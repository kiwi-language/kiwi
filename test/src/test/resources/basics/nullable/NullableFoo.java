package nullable;

import java.util.ArrayList;
import java.util.List;

public class NullableFoo {

    private final List<String> list = new ArrayList<>();

    public void add(String s) {
        list.add(s);
    }

    public String get(int i) {
        return list.get(i);
    }

    public int getHash(int i) {
        return list.get(i).hashCode();
    }

}
