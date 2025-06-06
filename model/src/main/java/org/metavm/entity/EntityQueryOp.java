package org.metavm.entity;

import org.metavm.object.instance.core.Value;

public enum EntityQueryOp {
    EQ {
        @Override
        public boolean evaluate(Value first, Value second) {
            return first.equals(second);
        }
    },
    NE {
        @Override
        public boolean evaluate(Value first, Value second) {
            return !first.equals(second);
        }
    },

    ;

    public abstract boolean evaluate(Value first, Value second);
}
