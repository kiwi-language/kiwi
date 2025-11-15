package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.IntValue;
import org.metavm.object.instance.core.StringInstance;
import org.metavm.object.type.*;
import org.metavm.util.Instances;

import java.util.List;
import java.util.Objects;

public class StringKlassBuilder implements StdKlassBuilder {

    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("01bae40100"), "String", "java.lang.String")
                .kind(ClassKind.VALUE)
                .source(ClassSource.BUILTIN)
                .build();
        klass.setType(new StringType(klass));

        MethodBuilder.newBuilder(klass, "isEmpty")
                .id(Id.parse("01bae401b602"))
                .returnType(PrimitiveType.booleanType)
                .isNative(true)
                .nativeFunction((self, args, callContext) -> {
                    var s = (StringInstance) self;
                    return Instances.intInstance(s.getValue().isEmpty());
                })
                .build();

        var equals = MethodBuilder.newBuilder(klass, "equals")
                .id(Id.parse("01bae40160"))
                .returnType(PrimitiveType.booleanType)
                .isNative(true)
                .nativeFunction((self, args, callContext) -> {
                    var value = ((StringInstance) self).getValue();
                    var thatValue = Instances.toJavaString(args.getFirst());
                    return Instances.intInstance(value.equals(thatValue));
                })
                .build();
        equals.addParameter(new Parameter(Id.parse("01bae40162"), "o", Types.getNullableAnyType(), equals));

        MethodBuilder.newBuilder(klass, "hashCode")
                .id(Id.parse("01bae40168"))
                .returnType(PrimitiveType.intType)
                .isNative(true)
                .nativeFunction((self, args, callContext) -> {
                    var s = (StringInstance) self;
                    return Instances.intInstance(s.getValue().hashCode());
                })
                .build();

        MethodBuilder.newBuilder(klass, "length")
                .id(Id.parse("01bae40164"))
                .returnType(PrimitiveType.intType)
                .isNative(true)
                .nativeFunction((self, args, callContext) -> {
                    var s = (StringInstance) self;
                    return Instances.intInstance(s.getValue().length());
                })
                .build();

        var subString = MethodBuilder.newBuilder(klass, "substring")
                .id(Id.parse("01bae401b202"))
                .returnType(klass.getType())
                .isNative(true)
                .nativeFunction((self, args, callContext) -> {
                    var value = ((StringInstance) self).getValue();
                    var beginIndex = ((IntValue) args.getFirst()).getValue();
                    return Instances.stringInstance(value.substring(beginIndex));
                })
                .build();
        subString.addParameter(new Parameter(Id.parse("01bae401b402"), "beginIndex", PrimitiveType.intType, subString));

        var subString1 = MethodBuilder.newBuilder(klass, "substring")
                .id(Id.parse("01bae401ac02"))
                .returnType(klass.getType())
                .isNative(true)
                .nativeFunction((self, args, callContext) -> {
                    var value = ((StringInstance) self).getValue();
                    var beginIndex = ((IntValue) args.get(0)).getValue();
                    var endIndex = ((IntValue) args.get(1)).getValue();
                    return Instances.stringInstance(value.substring(beginIndex, endIndex));
                })
                .build();
        subString1.setParameters(List.of(
                new Parameter(Id.parse("01bae401ae02"), "beginIndex", PrimitiveType.intType, subString1),
                new Parameter(Id.parse("01bae401b002"), "endIndex", PrimitiveType.intType, subString1)
        ));

        var compareTo = MethodBuilder.newBuilder(klass, "compareTo")
                .id(Id.parse("01bae40174"))
                .returnType(PrimitiveType.intType)
                .isNative(true)
                .nativeFunction((self, args, callContext) -> {
                    var value = ((StringInstance) self).getValue();
                    var other = Objects.requireNonNull(Instances.toJavaString(args.getFirst()));
                    return Instances.intInstance(value.compareTo(other));
                })
                .build();
        compareTo.addParameter(new Parameter(Id.parse("01bae40176"), "anotherString", klass.getType(), compareTo));

        var charAt = MethodBuilder.newBuilder(klass, "charAt")
                .id(Id.parse("01bae401c401"))
                .returnType(PrimitiveType.charType)
                .isNative(true)
                .nativeFunction((self, args, callContext) -> {
                    var value = ((StringInstance) self).getValue();
                    var index = ((IntValue) args.getFirst()).getValue();
                    return Instances.intInstance(value.charAt(index));
                })
                .build();
        charAt.addParameter(new Parameter(Id.parse("01bae401c601"), "index", PrimitiveType.intType, charAt));

        var startsWith = MethodBuilder.newBuilder(klass, "startsWith")
                .id(Id.parse("01bae4018e02"))
                .returnType(PrimitiveType.booleanType)
                .isNative(true)
                .nativeFunction((self, args, callContext) -> {
                    var value = ((StringInstance) self).getValue();
                    var prefix = Objects.requireNonNull(Instances.toJavaString(args.getFirst()));
                    return Instances.intInstance(value.startsWith(prefix));
                })
                .build();
        startsWith.addParameter(new Parameter(Id.parse("01bae4019002"), "prefix", klass.getType(), startsWith));

        var endsWith = MethodBuilder.newBuilder(klass, "endsWith")
                .id(Id.parse("01bae401aa03"))
                .returnType(PrimitiveType.booleanType)
                .isNative(true)
                .nativeFunction((self, args, callContext) -> {
                    var value = ((StringInstance) self).getValue();
                    var suffix = Objects.requireNonNull(Instances.toJavaString(args.getFirst()));
                    return Instances.intInstance(value.endsWith(suffix));
                })
                .build();
        endsWith.addParameter(new Parameter(Id.parse("01bae401ac03"), "suffix", klass.getType(), endsWith));

        return klass;
    }

    @Override
    public Class<?> getJavaClass() {
        return String.class;
    }
}