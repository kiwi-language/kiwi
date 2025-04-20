package org.metavm.manufacturing.production;

import org.metavm.api.EntityStruct;

@EntityStruct
public class RoutingProcessItem {
    private int sequence;
    private long numerator;

    public RoutingProcessItem(int sequence, long numerator) {
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
