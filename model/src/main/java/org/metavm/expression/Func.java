package org.metavm.expression;

import org.metavm.object.instance.core.NullValue;
import org.metavm.object.instance.core.StringReference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

import java.util.Arrays;
import java.util.List;

public enum Func {

    STARTS_WITH(1) {
        @Override
        public Type getReturnType(List<Type> argumentTypes) {
            return Types.getBooleanType();
        }

        @Override
        public Value evaluate(List<Value> args) {
            if (args.size() != 2)
                throw new IllegalArgumentException("Invalid number of arguments for starts_with function: " + args.size());
            var first = args.getFirst();
            var prefix = args.get(1);
            if(first instanceof NullValue)
                return Instances.zero();
            else if(first instanceof StringReference s1 && prefix instanceof StringReference s2)
                return Instances.booleanInstance(s1.getValue().startsWith(s2.getValue()));
            throw new IllegalArgumentException("Invalid argument for starts_with function: " + first);
        }
    },
    CONTAINS(2) {
        @Override
        public Type getReturnType(List<Type> argumentTypes) {
            return Types.getBooleanType();
        }

        @Override
        public Value evaluate(List<Value> arguments) {
            if (arguments.size() != 2)
                throw new IllegalArgumentException("CONTAINS function requires exactly 2 arguments.");
            var first = arguments.getFirst();
            var prefix = arguments.get(1);
            if(first instanceof NullValue)
                return Instances.zero();
            if(first instanceof StringReference s1 && prefix instanceof StringReference s2)
                return Instances.booleanInstance(s1.getValue().contains(s2.getValue()));
            else
                throw new IllegalArgumentException("Invalid argument for contains function: " + first);
        }
    },
    ;

    private final int code;


    Func(int code) {
        this.code = code;
    }

    public static Func fromName(String name) {
        return Arrays.stream(values())
                .filter(value -> value.name().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new RuntimeException("No func found for name: " + name));
    }

    public static Func fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

    public abstract Type getReturnType(List<Type> argumentTypes);

    public abstract Value evaluate(List<Value> arguments);

    public int code() {
        return code;
    }

}
