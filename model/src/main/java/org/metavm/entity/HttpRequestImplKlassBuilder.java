package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.http.HttpRequestImpl;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.Instances;

import java.util.List;

public class HttpRequestImplKlassBuilder implements StdKlassBuilder {
    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("01b0ee0100"), "HttpRequestImpl", "org.metavm.http.HttpRequestImpl")
                .kind(ClassKind.VALUE)
                .source(ClassSource.BUILTIN)
                .tag(740)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.metavm.http.HttpRequestImpl.class, klass);
        klass.setInterfaces(List.of((ClassType) registry.getType(org.metavm.api.entity.HttpRequest.class)));
        {
            MethodBuilder.newBuilder(klass, "getMethod")
                    .id(Id.parse("01b0ee0102"))
                    .returnType(registry.getType(java.lang.String.class))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var r = (HttpRequestImpl) self;
                        return Instances.stringInstance(r.getMethod());

                    })
                    .build();
        }
        {
            MethodBuilder.newBuilder(klass, "getRequestURI")
                    .id(Id.parse("01b0ee0108"))
                    .returnType(registry.getType(java.lang.String.class))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var r = (HttpRequestImpl) self;
                        return Instances.stringInstance(r.getRequestURI());
                    })
                    .build();
        }
        {
            var method = MethodBuilder.newBuilder(klass, "getCookie")
                    .id(Id.parse("01b0ee010a"))
                    .returnType(Types.getNullableType(registry.getType(java.lang.String.class)))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var r = (HttpRequestImpl) self;
                        var name = args.getFirst();
                        var n = name.stringValue();
                        var c = r.getCookie(n);
                        return c != null ? Instances.stringInstance(c) : Instances.nullInstance();
                    })
                    .build();
            method.addParameter(new Parameter(Id.parse("01b0ee010c"), "name", registry.getType(java.lang.String.class), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "getHeader")
                    .id(Id.parse("01b0ee0104"))
                    .returnType(Types.getNullableType(registry.getType(java.lang.String.class)))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var name = args.getFirst();
                        var r = (HttpRequestImpl) self;
                        var n = name.stringValue();
                        var h = r.getHeader(n);
                        return h != null ? Instances.stringInstance(h) : Instances.nullInstance();
                    })
                    .build();
            method.addParameter(new Parameter(Id.parse("01b0ee0106"), "name", registry.getType(java.lang.String.class), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "setCurrentUser")
                    .id(Id.parse("01b0ee0110"))
                    .returnType(Types.getVoidType())
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var currentUser = args.getFirst();
                        var r = (HttpRequestImpl) self;
                        r.setCurrentUser(currentUser);
                        return Instances.nullInstance();
                    })
                    .build();
            method.addParameter(new Parameter(Id.parse("01b0ee0112"), "currentUser", registry.getType(java.lang.Object.class), method));
        }
        {
            MethodBuilder.newBuilder(klass, "getCurrentUser")
                    .id(Id.parse("01b0ee010e"))
                    .returnType(registry.getType(java.lang.Object.class))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var r = (HttpRequestImpl) self;
                        return (Value) r.getCurrentUser();
                    })
                    .build();
        }
        klass.setStage(ResolutionStage.DECLARATION);
        klass.emitCode();
        return klass;

    }

    @Override
    public Class<?> getJavaClass() {
        return HttpRequestImpl.class;
    }
}
