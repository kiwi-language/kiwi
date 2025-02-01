package org.metavm.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PrimitiveNativeGenerator {

    public static final String dir = "/Users/leen/workspace/object/model/src/main/java/org/metavm/entity/natives";

    public static final List<Class<?>> classes = List.of(
            byte.class, short.class, int.class, long.class, float.class, double.class, char.class, boolean.class
    );

//    public static final List<Class<?>> classes = List.of(
//            int.class, char.class
//    );

    public static final String template = """
            package org.metavm.entity.natives;

            import org.metavm.entity.StdField;
            import org.metavm.entity.StdKlass;
            import org.metavm.object.instance.core.ClassInstance;
            import org.metavm.object.instance.core.{U}Value;
            import org.metavm.object.instance.core.Value;
            import org.metavm.object.type.Klass;
            import org.metavm.object.type.PrimitiveType;
            import org.metavm.util.Instances;

            import java.util.Map;
 
            // Generated code. Do not modify. @see @PrimitiveNativeGenerator
            public class {U1}NativeStub extends NativeBase {
                
                private final ClassInstance instance;

                public {U1}NativeStub(ClassInstance instance) {
                    this.instance = instance;
                }

                public Value compareTo(Value that, CallContext callContext) {
                    return Instances.intInstance(
                            {U1}.compare(getValue(instance), getValue(that))
                    );
                }

                public Value equals(Value that, CallContext callContext) {
                    return Instances.intInstance(getValue(instance) == getValue(that));
                }

                public Value hashCode(CallContext callContext) {
                    return Instances.intInstance({U1}.hashCode(getValue(instance)));
                }

                public Value toString(CallContext callContext) {
                    return Instances.stringInstance({U1}.toString(getValue(instance)));
                }

                public static Value compare(Klass klass, Value x, Value y, CallContext callContext) {
                    var i = (({U}Value) x).value;
                    var j = (({U}Value) y).value;
                    return Instances.intInstance({U1}.compare(i, j));
                }
                
                public static Value {V}(Klass klass, Value value, CallContext callContext) {
                    return valueOf(({U}Value) {X});
                }

                public static Value valueOf({U}Value value) {
                    var {L}Type = StdKlass.{L1}{D}.type();
                    var valueField = StdField.{L1}Value.get();
                    var data = Map.of(valueField, value);
                    return ClassInstance.create(data, {L}Type).getReference();
                }

                private static {L} getValue(Value value) {
                    return getValue(value.resolveObject());
                }

                private static {L} getValue(ClassInstance instance) {
                    var i = ({U}Value) instance.getField(StdField.{L1}Value.get());
                    return i.value;
                }
                {Numbers}
            }
            """;

    public static final String nonNumberTemplate = """
                
                public Value {L}Value(CallContext callContext) {
                    return instance.getField(StdField.{L1}Value.get()).toStackValue();
                }
            """;

    public static final String numberTemplate = """
            
                public Value byteValue(CallContext callContext) {
                    return Instances.intInstance((byte) getValue(instance));
                }
                
                public Value shortValue(CallContext callContext) {
                    return Instances.intInstance((short) getValue(instance));
                }
                
                public Value intValue(CallContext callContext) {
                    return Instances.intInstance((int) getValue(instance));
                }
                
                public Value longValue(CallContext callContext) {
                    return Instances.longInstance((long) getValue(instance));
                }
                
                public Value floatValue(CallContext callContext) {
                    return Instances.floatInstance((float) getValue(instance));
                }
                
                public Value doubleValue(CallContext callContext) {
                    return Instances.doubleInstance((double) getValue(instance));
                }
           
            """;

    public static void main(String[] args) throws IOException {
        for (Class<?> c : classes) {
            var l = c.getName();
            var u = NamingUtils.firstCharToUpperCase(l);
            var v = c == char.class ? "valueOf" : "valueOf__" + l;
            String x;
            if (c == byte.class || c == short.class || c == char.class || c == boolean.class)
                x = "PrimitiveType." + l + "Type.fromStackValue(value)";
            else
                x = "value";
            String d;
            if (c == byte.class || c == short.class || c == long.class || c == boolean.class || c == float.class || c == double.class)
                d = "_";
            else
                d = "";
            String l1;
            if (c == char.class)
                l1 = "character";
            else if (c == int.class)
                l1 = "integer";
            else
                l1 = l;
            var u1 = NamingUtils.firstCharToUpperCase(l1);
            var s = template;
            if (c != char.class && c != boolean.class)
                s = s.replace("{Numbers}", numberTemplate);
            else
                s = s.replace("{Numbers}", nonNumberTemplate);
            s = s.replace("{L}", l).replace("{U}", u).replace("{V}", v)
                    .replace("{X}", x).replace("{U1}", u1).replace("{L1}", l1)
                    .replace("{D}", d);

            var file = dir + "/" + u1 + "NativeStub.java";
            try(var writer = new FileWriter(file)) {
                writer.write(s);
            }

//            System.out.println("""
//                    public static Value wrapped{U}Instance({L} v) {
//                        return {U1}Native.valueOf({L}Instance(v));
//                    }
//                    """.replace("{U1}", u1)
//                    .replace("{L1}", l1)
//                    .replace("{U}", u)
//                    .replace("{L}", l)
//            );

            System.out.println("""
                        public static {L} toJava{U}(Value value) {
                            var v = ({U}Value) value.resolveObject().getField(StdField.{L1}Value.get());
                            return v.value;
                        }
                        """.replace("{L1}", l1)
                            .replace("{U}", u)
                            .replace("{L}", l)
                    );
        }

    }


}
