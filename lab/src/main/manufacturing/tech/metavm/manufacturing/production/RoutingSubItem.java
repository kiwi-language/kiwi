package tech.metavm.manufacturing.production;

import tech.metavm.entity.EntityStruct;

@EntityStruct("RoutingSubItem")
public class RoutingSubItem {
    private int sequence;
    private long numerator;

    public RoutingSubItem(int sequence, long numerator) {
        this.sequence = sequence;
        this.numerator = numerator;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public long getNumerator() {
        return numerator;
    }

    public void setNumerator(long numerator) {
        this.numerator = numerator;
    }
}
