package index;

import org.metavm.api.Index;

import javax.annotation.Nullable;
import java.util.List;

public class Foo {

    private static final Index<String, Foo> nameIndex = new Index<>(true, Foo::getName);
    private static final Index<Integer, Foo> seqIndex = new Index<>(false, f -> f.seq);
    private static final Index<Bar, Foo> barIndex = new Index<>(false, Foo::getBar);
    public static final Index<Pair<String, Integer>, Foo> nameSeqIndex =
            new Index<>(false, f -> new Pair<>(f.name, f.seq));

    private String name;
    private int seq;
    private final Bar bar;

    public Foo(String name, int seq, Bar bar) {
        this.name = name;
        this.seq = seq;
        this.bar = bar;
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

    public Bar getBar() {
        return bar;
    }

    public static @Nullable Foo findByName(String name) {
        return nameIndex.getFirst(name);
    }

    public static long countBySeq(int min, int max) {
        return seqIndex.count(min, max);
    }

    public static List<Foo> queryBySeq(int min, int max) {
        return seqIndex.query(min, max);
    }

    public static @Nullable Foo findByBar(Bar bar) {
        return barIndex.getFirst(bar);
    }

    public static @Nullable Foo findByNameAndSeq(String name, int seq) {
        return nameSeqIndex.getFirst(new Pair<>(name, seq));
    }

}

