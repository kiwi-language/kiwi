package org.metavm.object.instance.core;

import org.metavm.object.type.PrimitiveType;
import org.metavm.util.InstanceOutput;
import org.metavm.util.Instances;
import org.metavm.util.WireTypes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeValue extends PrimitiveValue /*implements Comparable<TimeInstance>*/ {

    public static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final long value;

    public TimeValue(long value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(WireTypes.TIME);
        output.writeLong(value);
    }

    public BooleanValue before(TimeValue that) {
        return Instances.booleanInstance(value < that.value);
    }

    public BooleanValue beforeOrEqual(TimeValue that) {
        return Instances.booleanInstance(value <= that.value);
    }

    public BooleanValue after(TimeValue that) {
        return Instances.booleanInstance(value > that.value);
    }

    public BooleanValue afterOrEqual(TimeValue that) {
        return Instances.booleanInstance(value >= that.value);
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public String getTitle() {
        return DF.format(new Date(value));
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitTimeValue(this);
    }

//    @Override
//    public int compareTo(@NotNull TimeInstance o) {
//        return Long.compare(value, o.value);
//    }
}
