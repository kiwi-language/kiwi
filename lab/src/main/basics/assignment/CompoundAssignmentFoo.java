package assignment;

public class CompoundAssignmentFoo {

    private int size;

    public CompoundAssignmentFoo(int size) {
        this.size = size;
    }

    public int decrementSize(int delta) {
        return size -= delta;
    }

}
