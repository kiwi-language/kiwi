package org.metavm.entity;

import org.metavm.object.instance.core.StringReference;
import org.metavm.object.instance.core.Value;

import java.util.Objects;

public enum EntityQueryOp {
    EQ {
        @Override
        public boolean evaluate(Value first, Value second) {
            if (Objects.equals(second, first)) return true;
            else if (first instanceof StringReference s1 && second instanceof StringReference s2) {
                return s1.getValue().toLowerCase().contains(s2.getValue().toLowerCase());
            } else if (first.getValueType().isArray()) {
                var array = first.resolveArray();
                return array.contains(second);
            }
            else return false;
        }
    },
    NE {
        @Override
        public boolean evaluate(Value first, Value second) {
            return !EQ.evaluate(first, second);
        }
    },

    ;

    public abstract boolean evaluate(Value first, Value second);
}
