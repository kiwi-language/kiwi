package index;

import org.metavm.api.IndexMap;

public class IndexFoo {

    public static final IndexMap<String, IndexFoo> nameIndex = new IndexMap<>(false, f -> f.name);

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
