package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.http.HttpCookieImpl;
import org.metavm.http.HttpHeaderImpl;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;
import org.metavm.util.Instances;

import java.util.List;

public class HttpHeaderImplKlassBuilder implements StdKlassBuilder {


    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("01baec0100"), "HttpHeaderImpl", "org.metavm.http.HttpHeaderImpl")
                .source(ClassSource.BUILTIN)
                .tag(743)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.metavm.http.HttpHeaderImpl.class, klass);
        klass.setInterfaces(List.of((ClassType) registry.getType(org.metavm.api.entity.HttpHeader.class)));
        {
            MethodBuilder.newBuilder(klass, "name")
                    .id(Id.parse("01baec0102"))
                    .returnType(registry.getType(java.lang.String.class))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var h = (HttpCookieImpl) self;
                        return Instances.stringInstance(h.name());
                    })
                    .build();
        }
        {
            MethodBuilder.newBuilder(klass, "value")
                    .id(Id.parse("01baec0104"))
                    .returnType(registry.getType(java.lang.String.class))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var h = (HttpHeaderImpl) self;
                        return Instances.stringInstance(h.value());
                    })
                    .build();
        }
        klass.setStage(ResolutionStage.DECLARATION);
        klass.emitCode();
        return klass;
    }

    @Override
    public Class<?> getJavaClass() {
        return org.metavm.http.HttpHeaderImpl.class;
    }


}
