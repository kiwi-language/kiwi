package org.metavm.object.instance.core;

import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeValue extends PrimitiveValue {

    public static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final long value;

    public TimeValue(long value) {
        this.value = value;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.TIME);
        output.writeLong(value);
    }

    public Value before(TimeValue that) {
        return Instances.intInstance(value < that.value);
    }

    public Value after(TimeValue that) {
        return Instances.intInstance(value > that.value);
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public PrimitiveType getValueType() {
        return PrimitiveType.timeType;
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
