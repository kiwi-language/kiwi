package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.http.HttpCookieImpl;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;
import org.metavm.util.Instances;

import java.util.List;

public class HttpCookieImplKlassBuilder implements StdKlassBuilder {
    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("01eeed0100"), "HttpCookieImpl", "org.metavm.http.HttpCookieImpl")
                .source(ClassSource.BUILTIN)
                .tag(695)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.metavm.http.HttpCookieImpl.class, klass);
        klass.setInterfaces(List.of((ClassType) registry.getType(org.metavm.api.entity.HttpCookie.class)));
        {
            MethodBuilder.newBuilder(klass, "name")
                    .id(Id.parse("01eeed0102"))
                    .returnType(registry.getType(java.lang.String.class))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var c = (HttpCookieImpl) self;
                        return Instances.stringInstance(c.name());
                    })
                    .build();
        }
        {
            MethodBuilder.newBuilder(klass, "value")
                    .id(Id.parse("01eeed0104"))
                    .returnType(registry.getType(java.lang.String.class))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var c = (HttpCookieImpl) self;
                        return Instances.stringInstance(c.value());
                    })
                    .build();
        }
        klass.setStage(ResolutionStage.DECLARATION);
        klass.emitCode();
        return klass;

    }

    @Override
    public Class<?> getJavaClass() {
        return HttpCookieImpl.class;
    }
}
