package org.manul.entity;

import org.manul.flow.MethodBuilder;
import org.manul.http.HttpCookieImpl;
import org.manul.object.instance.core.Id;
import org.manul.object.type.*;
import org.manul.util.Instances;

import java.util.List;

public class HttpCookieImplKlassBuilder implements StdKlassBuilder {
    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("01eeed0100"), "HttpCookieImpl", "org.manul.http.HttpCookieImpl")
                .source(ClassSource.BUILTIN)
                .tag(695)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.manul.http.HttpCookieImpl.class, klass);
        klass.setInterfaces(List.of((ClassType) registry.getType(org.manul.api.entity.HttpCookie.class)));
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
