package org.manul.entity;

import org.manul.flow.MethodBuilder;
import org.manul.http.HttpCookieImpl;
import org.manul.http.HttpHeaderImpl;
import org.manul.object.instance.core.Id;
import org.manul.object.type.*;
import org.manul.util.Instances;

import java.util.List;

public class HttpHeaderImplKlassBuilder implements StdKlassBuilder {


    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("01baec0100"), "HttpHeaderImpl", "org.manul.http.HttpHeaderImpl")
                .source(ClassSource.BUILTIN)
                .tag(743)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.manul.http.HttpHeaderImpl.class, klass);
        klass.setInterfaces(List.of((ClassType) registry.getType(org.manul.api.entity.HttpHeader.class)));
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
        return org.manul.http.HttpHeaderImpl.class;
    }


}
