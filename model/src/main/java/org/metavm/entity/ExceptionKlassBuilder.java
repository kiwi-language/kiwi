package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;
import org.metavm.util.Instances;

public class ExceptionKlassBuilder implements StdKlassBuilder {
    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("0194e90100"), "Exception", "java.lang.Exception")
                .source(ClassSource.BUILTIN)
                .build();

        MethodBuilder.newBuilder(klass, "Exception")
                .id(Id.parse("0194e9011a"))
                .isConstructor(true)
                .isNative(true)
                .build();

        FieldBuilder.newBuilder("detailMessage", klass, Types.getNullableStringType())
                .id(Id.parse("01fee40140"))
                .build();

        FieldBuilder.newBuilder("cause", klass, Types.getNullableType(klass.getType()))
                .id(Id.parse("01fee40142"))
                .build();

        var init = MethodBuilder.newBuilder(klass, "Exception")
                .id(Id.parse("0194e90116"))
                .isConstructor(true)
                .isNative(true)
                .nativeFunction((self, args, callContext) -> {
                    self.initField(StdField.exceptionDetailMessage.get(), args.getFirst());
                    self.initField(StdField.exceptionCause.get(), Instances.nullInstance());
                    return self.getReference();
                })
                .build();
        init.addParameter(new Parameter(Id.parse("0194e90118"), "message", StdKlass.string.type(), init));

        return klass;
    }

    @Override
    public Class<?> getJavaClass() {
        return Exception.class;
    }
}
