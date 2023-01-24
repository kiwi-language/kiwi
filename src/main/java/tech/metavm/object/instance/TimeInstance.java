package tech.metavm.object.instance;

import tech.metavm.object.instance.persistence.TimePO;
import tech.metavm.object.meta.PrimitiveType;
import tech.metavm.util.IdentitySet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeInstance extends PrimitiveInstance {

    public static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final long value;

    public TimeInstance(Date date, PrimitiveType type) {
        this(date.getTime(), type);
    }

    public TimeInstance(long value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    @Override
    public Object toColumnValue(long tenantId, IdentitySet<Instance> visited) {
        return new TimePO(value);
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DateInstance " + value + ":" + getType().getName();
    }

    @Override
    public String getTitle() {
        return DF.format(new Date(value));
    }
}
