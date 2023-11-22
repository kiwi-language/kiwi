package tech.metavm.object.instance.core;

import tech.metavm.object.instance.persistence.TimePO;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.WireTypes;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeInstance extends PrimitiveInstance {

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
        return InstanceUtils.booleanInstance(value < that.value);
    }

    public BooleanInstance isBeforeOrAt(TimeInstance that) {
        return InstanceUtils.booleanInstance(value <= that.value);
    }

    public BooleanInstance isAfter(TimeInstance that) {
        return InstanceUtils.booleanInstance(value > that.value);
    }

    public BooleanInstance isAfterOrAt(TimeInstance that) {
        return InstanceUtils.booleanInstance(value >= that.value);
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
    public TimePO toColumnValue() {
        return new TimePO(value);
    }

    @Override
    public String getTitle() {
        return DF.format(new Date(value));
    }

    @Override
    public void accept(InstanceVisitor visitor) {
        visitor.visitTimeInstance(this);
    }
}
