package index;

import org.metavm.api.EntityIndex;
import org.metavm.api.Index;
import org.metavm.api.ValueType;
import org.metavm.api.lang.Indices;

import javax.annotation.Nullable;

public class IndexSelectFoo {

    private String name;

    public IndexSelectFoo(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static @Nullable IndexSelectFoo findByName(String name) {
        return Indices.selectFirst(new NameIndex(name));
    }

    @EntityIndex
    NameIndex nameIndex() {
        return new NameIndex(name);
    }

}

@ValueType
record NameIndex(String name) implements Index<IndexSelectFoo> {}
