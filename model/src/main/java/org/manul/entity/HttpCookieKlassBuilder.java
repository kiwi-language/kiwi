package org.manul.entity;

import org.manul.flow.MethodBuilder;
import org.manul.object.instance.core.Id;
import org.manul.object.type.*;

public class HttpCookieKlassBuilder implements StdKlassBuilder {

    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("0182ea0100"), "HttpCookie", "org.manul.api.entity.HttpCookie")
                .kind(ClassKind.INTERFACE)
                .source(ClassSource.BUILTIN)
                .tag(449)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.manul.api.entity.HttpCookie.class, klass);
        {
            MethodBuilder.newBuilder(klass, "name")
                    .id(Id.parse("0182ea0102"))
                    .returnType(registry.getType(java.lang.String.class))
                    .isNative(true)
                    .build();
        }
        {
            MethodBuilder.newBuilder(klass, "value")
                    .id(Id.parse("0182ea0104"))
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
        return org.manul.api.entity.HttpCookie.class;
    }

}
