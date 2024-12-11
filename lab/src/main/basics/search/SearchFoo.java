package search;

import org.metavm.api.Entity;

@Entity(searchable = true)
public class SearchFoo {

    private String name;
    private int seq;

    public SearchFoo(String name, int seq) {
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
