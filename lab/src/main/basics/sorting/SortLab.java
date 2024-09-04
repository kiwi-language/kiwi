package sorting;

import org.metavm.api.ChildEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortLab {

    @ChildEntity
    private final List<ComparableFoo> foos;

    public SortLab(List<ComparableFoo> foos) {
        this.foos = new ArrayList<>(foos);
        Collections.sort(this.foos);
    }

    public List<ComparableFoo> getFoos() {
        return foos;
    }

    public void reverseFoos() {
        Collections.reverse(this.foos);
    }

    public void sortFoos() {
        this.foos.sort(ComparableFoo::compareTo);
    }

    public void sortFoos1() {
        this.foos.sort(null);
    }

}
