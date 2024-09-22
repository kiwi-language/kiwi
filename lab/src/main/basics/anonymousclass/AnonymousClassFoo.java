package anonymousclass;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.ChildList;

import java.util.ArrayList;
import java.util.Iterator;

public record AnonymousClassFoo(@ChildEntity ChildList<Entry> entries) {

    public Iterable<String> keysSnapshot() {
        var a0 = new ArrayList<>(entries);
        return new Iterable<>() {

            @NotNull
            @Override
            public Iterator<String> iterator() {
                return new Iterator<>() {

                    private final Iterator<Entry> i = a0.iterator();

                    @Override
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    @Override
                    public String next() {
                        return i.next().key();
                    }
                };
            }
        };
    }

    @SuppressWarnings("StringConcatenationInLoop")
    public String concatKeys() {
        var it = keysSnapshot().iterator();
        var r = "";
        while (it.hasNext()) {
            if(!r.isEmpty())
                r = r + ",";
            r = r + it.next();
        }
        return r;
    }

    public record Entry(String key, Object value) {
    }

}
