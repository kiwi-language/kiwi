package index;

import org.metavm.api.Index;

public class IndexFoo {

    public static final Index<String, IndexFoo> nameIndex = new Index<>(true, IndexFoo::getName);

    private String name;
    private int seq;

    public IndexFoo(String name, int seq) {
        this.name = name;
        this.seq = seq;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
