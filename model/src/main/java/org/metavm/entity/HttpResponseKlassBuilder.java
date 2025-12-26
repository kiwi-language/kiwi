package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;

public class HttpResponseKlassBuilder implements StdKlassBuilder {

    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("01fee90100"), "HttpResponse", "org.metavm.api.entity.HttpResponse")
                .kind(ClassKind.INTERFACE)
                .source(ClassSource.BUILTIN)
                .tag(447)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.metavm.api.entity.HttpResponse.class, klass);
        {
            var method = MethodBuilder.newBuilder(klass, "addCookie")
                    .id(Id.parse("01fee9010a"))
                    .returnType(Types.getVoidType())
                    .isNative(true)
                    .build();
            method.addParameter(new Parameter(Id.parse("01fee9010c"), "name", registry.getType(java.lang.String.class), method));
            method.addParameter(new Parameter(Id.parse("01fee9010e"), "value", registry.getType(java.lang.String.class), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "addHeader")
                    .id(Id.parse("01fee90104"))
                    .returnType(Types.getVoidType())
                    .isNative(true)
                    .build();
            method.addParameter(new Parameter(Id.parse("01fee90106"), "name", registry.getType(java.lang.String.class), method));
            method.addParameter(new Parameter(Id.parse("01fee90108"), "value", registry.getType(java.lang.String.class), method));
        }
        {
            MethodBuilder.newBuilder(klass, "getCookies")
                    .id(Id.parse("01fee90110"))
                    .returnType(Types.getArrayType(registry.getType(org.metavm.api.entity.HttpCookie.class)))
                    .isNative(true)
                    .build();
        }
        {
            MethodBuilder.newBuilder(klass, "getHeaders")
                    .id(Id.parse("01fee90102"))
                    .returnType(Types.getArrayType(registry.getType(org.metavm.api.entity.HttpHeader.class)))
                    .isNative(true)
                    .build();
        }
        klass.setStage(ResolutionStage.DECLARATION);
        klass.emitCode();
        return klass;
    }

    @Override
    public Class<?> getJavaClass() {
        return org.metavm.api.entity.HttpResponse.class;
    }

}
