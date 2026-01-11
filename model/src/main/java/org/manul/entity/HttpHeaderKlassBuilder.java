package org.manul.entity;

import org.manul.flow.MethodBuilder;
import org.manul.object.instance.core.Id;
import org.manul.object.type.*;

public class HttpHeaderKlassBuilder implements StdKlassBuilder {

    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("0180ea0100"), "HttpHeader", "org.manul.api.entity.HttpHeader")
                .kind(ClassKind.INTERFACE)
                .source(ClassSource.BUILTIN)
                .tag(448)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.manul.api.entity.HttpHeader.class, klass);
        {
            MethodBuilder.newBuilder(klass, "name")
                    .id(Id.parse("0180ea0102"))
                    .returnType(registry.getType(java.lang.String.class))
                    .isNative(true)
                    .build();
        }
        {
            MethodBuilder.newBuilder(klass, "value")
                    .id(Id.parse("0180ea0104"))
                    .returnType(registry.getType(java.lang.String.class))
                    .isNative(true)
                    .build();
        }
        klass.setStage(ResolutionStage.DECLARATION);
        klass.emitCode();
        return klass;
    }

    @Override
    public Class<?> getJavaClass() {
        return org.manul.api.entity.HttpHeader.class;
    }

}
