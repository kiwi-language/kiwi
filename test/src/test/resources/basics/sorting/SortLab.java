package sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortLab {

    private final List<ComparableFoo> foos;

    public SortLab(List<ComparableFoo> foos) {
        this.foos = new ArrayList<>(foos);
        Collections.sort(this.foos);
    }

    public List<ComparableFoo> getFoos() {
        return foos;
    }

    public void sortFoos() {
        this.foos.sort(ComparableFoo::compareTo);
    }

}
