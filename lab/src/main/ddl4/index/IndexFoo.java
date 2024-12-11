package index;

import org.metavm.api.Index;
import javax.annotation.Nullable;

public class IndexFoo {

    public static final Index<Integer, IndexFoo> seqIndex = new Index<>(true, IndexFoo::getSeq);

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

    public static @Nullable IndexFoo findBySeq(int seq) {
        return seqIndex.getFirst(seq);
    }

}
