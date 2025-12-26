package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;

import java.util.List;

public class EnumKlassBuilder implements StdKlassBuilder {

    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("01f6e40100"), "Enum", "java.lang.Enum")
                .source(ClassSource.BUILTIN)
                .tag(34)
                .build();
        FieldBuilder.newBuilder("name", klass, registry.getType(String.class))
                .id(Id.parse("01f6e4012a"))
                .asTitle()
                .build();
        FieldBuilder.newBuilder("ordinal", klass, PrimitiveType.intType).id(Id.parse("01f6e4012c")).build();
        new TypeVariable(Id.parse("01f6e40102"), "E", klass);

        var init = MethodBuilder.newBuilder(klass, "Enum")
                .id(Id.parse("01f6e40104"))
                .isConstructor(true)
                .isNative(true)
                .nativeFunction((self, args, callContext) -> {
                    self.setFieldForce(StdField.enumName.get(), args.get(0));
                    self.setFieldForce(StdField.enumOrdinal.get(), args.get(1));
                    return self.getReference();
                })
                .build();
        init.setParameters(List.of(
                new Parameter(Id.parse("01f6e40106"), "name", StdKlass.string.type(), init),
                new Parameter(Id.parse("01f6e40108"), "ordinal", PrimitiveType.intType, init)
        ));

        return klass;
    }

    @Override
    public Class<?> getJavaClass() {
        return Enum.class;
    }
}
