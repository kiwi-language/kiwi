package operators;

public class ShiftAssignmentFoo {

    private long value;

    public ShiftAssignmentFoo(long value) {
        this.value = value;
    }

    public long unsignedRightShiftAssign(int shift) {
        value >>>= shift;
        return value;
    }

    public long rightShiftAssign(int shift) {
        value >>= shift;
        return value;
    }

    public long leftShiftAssign(int shift) {
        value <<= shift;
        return value;
    }

}
