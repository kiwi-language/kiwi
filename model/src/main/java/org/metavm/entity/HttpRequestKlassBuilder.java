package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;

public class HttpRequestKlassBuilder implements StdKlassBuilder {

    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("01fce90100"), "HttpRequest", "org.metavm.api.entity.HttpRequest")
                .kind(ClassKind.INTERFACE)
                .source(ClassSource.BUILTIN)
                .tag(446)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.metavm.api.entity.HttpRequest.class, klass);
        {
            MethodBuilder.newBuilder(klass, "getMethod")
                    .id(Id.parse("01fce90102"))
                    .returnType(registry.getType(java.lang.String.class))
                    .isNative(true)
                    .build();
        }
        {
            MethodBuilder.newBuilder(klass, "getRequestURI")
                    .id(Id.parse("01fce90108"))
                    .returnType(registry.getType(java.lang.String.class))
                    .isNative(true)
                    .build();
        }
        {
            var method = MethodBuilder.newBuilder(klass, "getCookie")
                    .id(Id.parse("01fce9010a"))
                    .returnType(Types.getNullableType(registry.getType(java.lang.String.class)))
                    .isNative(true)
                    .build();
            method.addParameter(new Parameter(Id.parse("01fce9010c"), "name", registry.getType(java.lang.String.class), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "getHeader")
                    .id(Id.parse("01fce90104"))
                    .returnType(Types.getNullableType(registry.getType(java.lang.String.class)))
                    .isNative(true)
                    .build();
            method.addParameter(new Parameter(Id.parse("01fce90106"), "name", registry.getType(java.lang.String.class), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "setCurrentUser")
                    .id(Id.parse("01fce90110"))
                    .returnType(Types.getVoidType())
                    .isNative(true)
                    .build();
            method.addParameter(new Parameter(Id.parse("01fce90112"), "currentUser", registry.getType(java.lang.Object.class), method));
        }
        {
            MethodBuilder.newBuilder(klass, "getCurrentUser")
                    .id(Id.parse("01fce9010e"))
                    .returnType(registry.getType(java.lang.Object.class))
                    .isNative(true)
                    .build();
        }
        klass.setStage(ResolutionStage.DECLARATION);
        klass.emitCode();
        return klass;
    }

    @Override
    public Class<?> getJavaClass() {
        return org.metavm.api.entity.HttpRequest.class;
    }

}
