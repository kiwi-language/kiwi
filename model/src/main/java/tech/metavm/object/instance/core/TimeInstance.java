package tech.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.Instances;
import tech.metavm.util.WireTypes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeInstance extends PrimitiveInstance implements Comparable<TimeInstance> {

    public static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final long value;

    public TimeInstance(long value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    @Override
    public void writeTo(InstanceOutput output, boolean includeChildren) {
        output.writeLong(value);
    }

    public BooleanInstance isBefore(TimeInstance that) {
        return Instances.booleanInstance(value < that.value);
    }

    public BooleanInstance isBeforeOrAt(TimeInstance that) {
        return Instances.booleanInstance(value <= that.value);
    }

    public BooleanInstance isAfter(TimeInstance that) {
        return Instances.booleanInstance(value > that.value);
    }

    public BooleanInstance isAfterOrAt(TimeInstance that) {
        return Instances.booleanInstance(value >= that.value);
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public int getWireType() {
        return WireTypes.TIME;
    }

    @Override
    public String getTitle() {
        return DF.format(new Date(value));
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitTimeInstance(this);
    }

    @Override
    public int compareTo(@NotNull TimeInstance o) {
        return Long.compare(value, o.value);
    }
}
