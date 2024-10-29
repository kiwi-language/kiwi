package index;

import org.metavm.api.EntityIndex;

public class IndexFoo {

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

    @EntityIndex
    public NameIndex nameIndex() {
        return new NameIndex(name);
    }

    public record NameIndex(String name) {
    }

}
