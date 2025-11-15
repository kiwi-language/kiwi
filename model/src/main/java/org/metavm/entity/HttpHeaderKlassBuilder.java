package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;

public class HttpHeaderKlassBuilder implements StdKlassBuilder {

    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("0180ea0100"), "HttpHeader", "org.metavm.api.entity.HttpHeader")
                .kind(ClassKind.INTERFACE)
                .source(ClassSource.BUILTIN)
                .tag(448)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.metavm.api.entity.HttpHeader.class, klass);
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
        return org.metavm.api.entity.HttpHeader.class;
    }

}
