package index;

import org.metavm.api.Index;

import javax.annotation.Nullable;

public class IndexSelectFoo {

    public static final Index<String, IndexSelectFoo> nameIndex = new Index<>(false, f -> f.name);

    private String name;

    public IndexSelectFoo(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static @Nullable IndexSelectFoo findByName(String name) {
        return nameIndex.getFirst(name);
    }

}

