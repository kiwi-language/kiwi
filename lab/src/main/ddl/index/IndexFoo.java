package index;

import org.metavm.api.Index;

public class IndexFoo {

    public static final Index<String, IndexFoo> nameIndex = new Index<>(true, IndexFoo::getName);

    private String name;

    public IndexFoo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
