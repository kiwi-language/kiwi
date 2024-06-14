package org.metavm.entity;

import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.NumberInstance;
import org.metavm.object.instance.core.TimeInstance;
import org.metavm.util.BytesUtils;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.Objects;

public enum IndexOperator {

    EQ(1, "=") {
        @Override
        public boolean evaluate(Object value1, Object value2) {
            return Objects.equals(value1, value2);
        }

        @Override
        public boolean evaluate(byte[] value1, byte[] value2) {
            return Arrays.equals(value1, value2);
        }

        @Override
        public boolean evaluate(Instance instance1, Instance instance2) {
            return instance1.equals(instance2);
        }
    },
    GT(2, ">") {

        @Override
        public boolean evaluate(Object value1, Object value2) {
            //noinspection unchecked,rawtypes
            return ((Comparable) value1).compareTo(value2) > 0;
        }

        @Override
        public boolean evaluate(byte[] value1, byte[] value2) {
            return BytesUtils.compareBytes(value1, value2) > 0;
        }

        @Override
        public boolean evaluate(Instance instance1, Instance instance2) {
            return compare(instance1, instance2) > 0;
        }
    },
    GE(3, ">=") {

        @Override
        public boolean evaluate(Object value1, Object value2) {
            //noinspection unchecked,rawtypes
            return ((Comparable) value1).compareTo(value2) >= 0;
        }

        @Override
        public boolean evaluate(byte[] value1, byte[] value2) {
            return BytesUtils.compareBytes(value1, value2) >= 0;
        }

        @Override
        public boolean evaluate(Instance instance1, Instance instance2) {
            return IndexOperator.compare(instance1, instance2) >= 0;
        }

    },

    LT(4, "<") {

        @Override
        public boolean evaluate(Object value1, Object value2) {
            //noinspection unchecked,rawtypes
            return ((Comparable) value1).compareTo(value2) < 0;
        }

        @Override
        public boolean evaluate(byte[] value1, byte[] value2) {
            return BytesUtils.compareBytes(value1, value2) < 0;
        }

        @Override
        public boolean evaluate(Instance instance1, Instance instance2) {
            return IndexOperator.compare(instance1, instance2) < 0;
        }

    },
    LE(5, "<=") {

        @Override
        public boolean evaluate(Object value1, Object value2) {
            //noinspection unchecked,rawtypes
            return ((Comparable) value1).compareTo(value2) <= 0;
        }

        @Override
        public boolean evaluate(byte[] value1, byte[] value2) {
            return BytesUtils.compareBytes(value1, value2) <= 0;
        }

        @Override
        public boolean evaluate(Instance instance1, Instance instance2) {
            return IndexOperator.compare(instance1, instance2) <= 0;
        }

    };

    private static int compare(Instance instance1, Instance instance2) {
        if (instance1 instanceof TimeInstance t1 && instance2 instanceof TimeInstance t2)
            return t1.compareTo(t2);
        else if (instance1 instanceof NumberInstance n1 && instance2 instanceof NumberInstance n2)
            return n1.compareTo(n2);
        else
            throw new InternalException(String.format("Can not compare instance %s with %s", instance1, instance2));
    }

    private final int code;
    private final String op;

    IndexOperator(int code, String op) {
        this.code = code;
        this.op = op;
    }

    public static IndexOperator getByCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

    public int code() {
        return code;
    }

    public String op() {
        return op;
    }

    public abstract boolean evaluate(Object value1, Object value2);

    public abstract boolean evaluate(byte[] value1, byte[] value2);

    public abstract boolean evaluate(Instance instance1, Instance instance2);

}
